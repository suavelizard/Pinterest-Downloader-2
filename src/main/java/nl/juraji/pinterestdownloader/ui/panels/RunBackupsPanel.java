package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.BoardsCheckboxList;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItem;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.workers.WrappingWorker;
import nl.juraji.pinterestdownloader.workers.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
@Default
public class RunBackupsPanel implements WindowPane {

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
    private JCheckBox incrementalBackupCheckBox;
    private JCheckBox downloadMissingPinsCheckBox;

    @Inject
    private Logger logger;

    @Inject
    private SettingsDao settingsDao;

    @Inject
    private BoardDao boardDao;

    @PostConstruct
    private void init() {
        boardsList.updateBoards(boardDao.get(Board.class));
        boardCountLabel.setText(I18n.get("ui.runBackups.boardCount", boardsList.getModel().getSize()));
        boardsList.setOnBoardUpdate(boardDao::save);

        setupFetchBoardsButton();
        setupBackupBoardsButton();
        setupDeleteBoardsButton();
        setupSelectionButtons();
    }

    @PreDestroy
    private void preDestroy() {
        PinterestScraperWorker.destroyDriver();
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    private void setupFetchBoardsButton() {
        fetchBoardsButton.addActionListener(evt -> {
            FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);

            FetchBoardsWorker scanner = new FetchBoardsWorker(settingsDao.getPinterestUsername(), settingsDao.getPinterestPassword(), boardDao.get(Board.class)) {
                @Override
                protected void process(List<Board> chunks) {
                    boardDao.save(chunks);
                    boardsList.updateBoards(chunks, true);
                    boardCountLabel.setText(I18n.get("ui.runBackups.boardCount", boardsList.getModel().getSize()));
                }

                @Override
                protected void done() {
                    super.done();
                    formLock.unlock();
                }
            };

            scanner.execute();
        });
    }

    private void setupBackupBoardsButton() {
        downloadMissingPinsCheckBox.addActionListener(e -> {
            incrementalBackupCheckBox.setSelected(!downloadMissingPinsCheckBox.isSelected());
            incrementalBackupCheckBox.setEnabled(!downloadMissingPinsCheckBox.isSelected());
        });

        backupBoardsButton.addActionListener(evt -> {
            List<BoardCheckboxListItem> selectedItems = boardsList.getSelectedItems();

            int choice = JOptionPane.showConfirmDialog(contentPane,
                    I18n.get("ui.runBackups.backupBoards.alert"),
                    I18n.get("ui.runBackups.backupBoards.alert.title", selectedItems.size()),
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                if (selectedItems.size() > 0) {
                    FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);

                    WrappingWorker worker = new WrappingWorker() {
                        @Override
                        protected Void doInBackground() {
                            String username = settingsDao.getPinterestUsername();
                            String password = settingsDao.getPinterestPassword();
                            File imageStore = settingsDao.getImageStore();

                            FetchPinsWorkerMode mode;
                            if (downloadMissingPinsCheckBox.isSelected()) {
                                mode = FetchPinsWorkerMode.DOWNLOAD_ONLY;
                            } else if (incrementalBackupCheckBox.isSelected()) {
                                mode = FetchPinsWorkerMode.INCREMENTAL_UPDATE;
                            } else {
                                mode = FetchPinsWorkerMode.FULL_UPDATE;
                            }

                            for (BoardCheckboxListItem item : selectedItems) {
                                Board board = item.getBoard();


                                try {
                                    List<Pin> pins;

                                    if (!FetchPinsWorkerMode.DOWNLOAD_ONLY.equals(mode)) {
                                        FetchPinsWorker scanner = new FetchPinsWorker(username, password, board, mode);

                                        scanner.execute();
                                        pins = scanner.get();

                                        if (pins != null) {
                                            board.getPins().addAll(pins);
                                        }
                                    } else {
                                        pins = board.getPins();
                                    }

                                    if (pins != null) {
                                        PinsDownloadWorker downloadWorker = new PinsDownloadWorker(board, imageStore);
                                        downloadWorker.execute();
                                        // Run get() in order to block wrapping worker thread
                                        downloadWorker.get();

                                        PinImageTypeCheckWorker imageTypeCheckWorker = new PinImageTypeCheckWorker(pins);
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

            int choice = JOptionPane.showConfirmDialog(contentPane,
                    I18n.get("ui.runBackups.deleteBoards.alert"),
                    I18n.get("ui.runBackups.deleteBoards.alert.title", selectedItems.size()),
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (selectedItems.size() > 0) {
                    FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);

                    DeleteBoardsWorker worker = new DeleteBoardsWorker(selectedItems) {
                        @Override
                        protected void process(List<Board> chunks) {
                            chunks.forEach(boardDao::delete);
                        }

                        @Override
                        protected void done() {
                            super.done();
                            boardsList.updateBoards(boardDao.get(Board.class));
                            formLock.unlock();
                        }
                    };

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
