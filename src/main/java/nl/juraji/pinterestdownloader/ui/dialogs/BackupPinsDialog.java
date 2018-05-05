package nl.juraji.pinterestdownloader.ui.dialogs;

import nl.juraji.pinterestdownloader.cdi.annotations.Indicator;
import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.BoardsCheckboxList;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItem;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.workers.*;
import nl.juraji.pinterestdownloader.util.workers.workerutils.SwingWorkerDoneListener;
import nl.juraji.pinterestdownloader.util.workers.workerutils.Worker;
import nl.juraji.pinterestdownloader.util.workers.workerutils.WrappingWorker;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class BackupPinsDialog extends JDialog {

    private JPanel contentPane;
    private BoardsCheckboxList boardsList;
    private JButton fetchBoardsButton;
    private JButton backupBoardsButton;
    private JLabel boardCountLabel;
    private JButton selectNoneBoardsButton;
    private JButton selectAllBoardsButton;
    private JButton selectNewBoardsButton;
    private JButton deleteBoardsButton;
    private JButton selectIncompleteBoardsButton;
    private JCheckBox fullScanCheckBox;

    @Inject
    private Logger logger;

    @Inject
    private SettingsDao settingsDao;

    @Inject
    private BoardDao boardDao;

    @Inject
    @Indicator
    private Instance<ProgressIndicator> indicatorInst;

    public BackupPinsDialog(Frame owner) {
        super(owner, I18n.get("ui.backupPinsDialog.dialogTitle"), true);

        pack();
        setContentPane(contentPane);
    }

    @PostConstruct
    private void init() {
        boardsList.updateBoards(boardDao.get());
        boardCountLabel.setText(I18n.get("ui.backupPinsDialog.boardCount", boardsList.getModel().getSize()));
        boardsList.setOnBoardUpdate(boardDao::save);

        setupFetchBoardsButton();
        setupBackupBoardsButton();
        setupDeleteBoardsButton();
        setupSelectionButtons();
        setupDestroyPinterestWebWorker();
    }

    private void setupDestroyPinterestWebWorker() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Destroy the scraper, since we don't need it anymore
                PinterestScraperWorker.destroyDriver();
            }
        });
    }

    private void setupFetchBoardsButton() {
        fetchBoardsButton.addActionListener(evt -> {
            FormUtils.FormLock formLock = FormUtils.lockForm(this, false);

            FetchBoardsWorker scanner = new FetchBoardsWorker(indicatorInst.get(), settingsDao.getPinterestUsername(), settingsDao.getPinterestPassword(),
                    boardDao.get());
            scanner.execute();
            scanner.addPropertyChangeListener(new SwingWorkerDoneListener() {
                @Override
                protected void onWorkerDone() {
                    try {
                        List<Board> boards = scanner.get();
                        boardDao.save(boards);
                        boardsList.updateBoards(boards, true);
                        boardCountLabel.setText(I18n.get("ui.backupPinsDialog.boardCount", boardsList.getModel().getSize()));
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.WARNING, "Error fetching boards", e);
                    } finally {
                        formLock.unlock();
                    }
                }
            });
        });
    }

    private void setupBackupBoardsButton() {
        backupBoardsButton.addActionListener(evt -> {
            List<BoardCheckboxListItem> selectedItems = boardsList.getSelectedItems();

            int choice = JOptionPane.showConfirmDialog(this,
                    I18n.get("ui.backupPinsDialog.backupBoards.alert"),
                    I18n.get("ui.backupPinsDialog.backupBoards.alert.title", selectedItems.size()),
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                if (selectedItems.size() > 0) {
                    FormUtils.FormLock formLock = FormUtils.lockForm(this, false);

                    WrappingWorker worker = new WrappingWorker() {
                        @Override
                        protected Void doInBackground() {
                            String username = settingsDao.getPinterestUsername();
                            String password = settingsDao.getPinterestPassword();
                            File imageStore = settingsDao.getImageStore();
                            boolean isFullScan = fullScanCheckBox.isSelected();

                            for (BoardCheckboxListItem item : selectedItems) {
                                Board board = item.getBoard();

                                try {
                                    FetchPinsWorker scanner = new FetchPinsWorker(indicatorInst.get(), username, password, board, isFullScan);

                                    scanner.execute();
                                    List<Pin> pins = scanner.get();

                                    if (pins != null) {
                                        board.getPins().addAll(pins);

                                        PinsDownloadWorker downloadWorker = new PinsDownloadWorker(indicatorInst.get(), board, imageStore);
                                        downloadWorker.execute();
                                        // Run get() in order to block wrapping worker thread
                                        downloadWorker.get();

                                        PinImageTypeCheckWorker imageTypeCheckWorker = new PinImageTypeCheckWorker(indicatorInst.get(), pins);
                                        imageTypeCheckWorker.execute();
                                        // Run get() in order to block wrapping worker thread
                                        imageTypeCheckWorker.get();

                                        item.updatePinCounts();
                                        boardDao.save(board);
                                        publish();
                                    }
                                } catch (InterruptedException e) {
                                    logger.log(Level.WARNING, "Fetching pins was interupted", e);
                                    // Interruption means stop fetching boards
                                    break;
                                } catch (ExecutionException e) {
                                    logger.log(Level.WARNING, "Error fetching pins for " + board.getName(), e);
                                }
                            }

                            return null;
                        }

                        @Override
                        protected void process(List<Void> chunks) {
                            boardsList.repaint();
                        }

                        @Override
                        protected void done() {
                            formLock.unlock();
                        }
                    };

                    worker.execute();
                }
            }
        });
    }

    private void setupDeleteBoardsButton() {
        deleteBoardsButton.addActionListener(evt -> {
            List<BoardCheckboxListItem> selectedItems = boardsList.getSelectedItems();

            int choice = JOptionPane.showConfirmDialog(this,
                    I18n.get("ui.backupPinsDialog.deleteBoards.alert"),
                    I18n.get("ui.backupPinsDialog.deleteBoards.alert.title", selectedItems.size()),
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (selectedItems.size() > 0) {
                    FormUtils.FormLock formLock = FormUtils.lockForm(this, false);
                    Worker<Void> worker = new DeleteBoardsWorker(indicatorInst.get(), selectedItems);
                    worker.addPropertyChangeListener(new SwingWorkerDoneListener() {
                        @Override
                        protected void onWorkerDone() {
                            selectedItems.iterator().forEachRemaining(item ->
                                    boardDao.delete(item.getBoard()));

                            boardsList.updateBoards(boardDao.get());
                            formLock.unlock();
                        }
                    });

                    worker.execute();
                }
            }
        });
    }

    private void setupSelectionButtons() {
        selectNewBoardsButton.addActionListener(e -> boardsList.updateSelectionFor(item ->
                item.getBoard().getPins().size() == 0));
        selectIncompleteBoardsButton.addActionListener(e -> boardsList.updateSelectionFor(item ->
                item.getBoard().getPins().stream().anyMatch(pin -> pin.getFileOnDisk() == null)));
        selectAllBoardsButton.addActionListener(e -> boardsList.updateSelectionForAll(true));
        selectNoneBoardsButton.addActionListener(e -> boardsList.updateSelectionForAll(false));
    }

}
