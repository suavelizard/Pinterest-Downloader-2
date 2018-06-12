package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.BoardsCheckboxList;
import nl.juraji.pinterestdownloader.ui.components.TabWindow;
import nl.juraji.pinterestdownloader.ui.components.TasksList;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItem;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.workers.WrappingWorker;
import nl.juraji.pinterestdownloader.workers.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
@Default
public class RunBackupsPanel implements TabWindow {

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
    private JButton addLocalFolderButton;

    @Inject
    private Logger logger;

    @Inject
    private SettingsDao settingsDao;

    @Inject
    private BoardDao boardDao;

    @PostConstruct
    private void init() {
        boardsList.setOnBoardUpdate(boardDao::save);

        setupFetchBoardsButton();
        setupAddLocalFolderButton();
        setupBackupBoardsButton();
        setupDeleteBoardsButton();
        setupSelectionButtons();
    }

    @Override
    public String getTitle() {
        return I18n.get("ui.runBackups.tabTitle");
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    @Override
    public void activate() {
        boardsList.updateBoards(boardDao.get(Board.class));
        boardCountLabel.setText(I18n.get("ui.runBackups.boardCount", boardsList.getModel().getSize()));
    }

    @Override
    public void deactivate() {
    }

    private void setupFetchBoardsButton() {
        fetchBoardsButton.addActionListener(evt -> {
            FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);

            final Task task = TasksList.newTask(I18n.get("ui.task.fetchBoards"));
            FetchBoardsWorker scanner = new FetchBoardsWorker(task, settingsDao.getPinterestUsername(), settingsDao.getPinterestPassword(), boardDao.get(Board.class)) {
                @Override
                protected void process(List<Board> chunks) {
                    boardDao.save(chunks);
                    boardsList.updateBoards(chunks, true);
                    boardCountLabel.setText(I18n.get("ui.runBackups.boardCount", boardsList.getModel().getSize()));
                }

                @Override
                protected void done() {
                    super.done();
                    task.complete();
                    formLock.unlock();
                    PinterestScraperWorker.destroyDriver();
                }
            };

            scanner.execute();
        });
    }

    private void setupAddLocalFolderButton() {
        addLocalFolderButton.addActionListener(evt -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);

            int result = fileChooser.showOpenDialog(addLocalFolderButton);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                Board localBoard = new Board();
                localBoard.setLocalFolder(true);
                localBoard.setName(selectedFile.getName());
                localBoard.setUrl(selectedFile.getAbsolutePath());

                boardDao.save(localBoard);
                boardsList.updateBoards(Collections.singletonList(localBoard), true);
            }
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
                        protected Void doInBackground() throws ExecutionException, InterruptedException {
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

                            final List<WrappingWorker> workers = selectedItems.stream()
                                    .map(item -> {
                                        final Task task = TasksList.newTask(I18n.get("ui.task.backupBoard", item.getBoard().getName()));
                                        return new WrappingWorker() {
                                            @Override
                                            protected Void doInBackground() {
                                                Board board = item.getBoard();

                                                try {
                                                    List<Pin> pins;

                                                    if (board.isLocalFolder()) {
                                                        LocalFolderScanWorker localFolderScanWorker = new LocalFolderScanWorker(task, board);

                                                        localFolderScanWorker.execute();
                                                        pins = localFolderScanWorker.get();

                                                        if (pins != null) {
                                                            board.getPins().addAll(pins);
                                                        }
                                                    } else if (!FetchPinsWorkerMode.DOWNLOAD_ONLY.equals(mode)) {
                                                        FetchPinsWorker scanner = new FetchPinsWorker(task, username, password, board, mode);

                                                        scanner.execute();
                                                        pins = scanner.get();

                                                        if (pins != null) {
                                                            board.getPins().addAll(pins);
                                                        }
                                                    } else {
                                                        pins = board.getPins();
                                                    }

                                                    if (pins != null) {
                                                        PinsDownloadWorker downloadWorker = new PinsDownloadWorker(task, board, imageStore);
                                                        downloadWorker.execute();
                                                        // Run get() in order to block wrapping worker thread
                                                        downloadWorker.get();

                                                        PinImageTypeCheckWorker imageTypeCheckWorker = new PinImageTypeCheckWorker(task, pins);
                                                        imageTypeCheckWorker.execute();
                                                        // Run get() in order to block wrapping worker thread
                                                        imageTypeCheckWorker.get();

                                                        PinHasherWorker pinHasherWorker = new PinHasherWorker(task, pins);
                                                        pinHasherWorker.execute();
                                                        // Run get() in order to block wrapping worker thread
                                                        pinHasherWorker.get();

                                                        item.updatePinCounts();
                                                        boardDao.save(board);
                                                        publish();
                                                    }
                                                } catch (InterruptedException e) {
                                                    logger.log(Level.WARNING, "Fetching pins was interupted", e);
                                                    // Interruption means stop fetching boards
                                                } catch (ExecutionException e) {
                                                    logger.log(Level.WARNING, "Error fetching pins for " + board.getName(), e);
                                                } finally {
                                                    task.complete();
                                                }

                                                return null;
                                            }

                                            @Override
                                            protected void process(List<Void> chunks) {
                                                boardsList.repaint();
                                            }
                                        };
                                    })
                                    .collect(Collectors.toList());

                            for (WrappingWorker worker : workers) {
                                worker.execute();
                                worker.get();
                            }

                            return null;
                        }

                        @Override
                        protected void done() {
                            PinterestScraperWorker.destroyDriver();
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
                    selectedItems.stream()
                            .map(BoardCheckboxListItem::getBoard)
                            .forEach(boardDao::delete);

                    boardsList.updateBoards(boardDao.get(Board.class));
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
