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
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
    private JLabel boardCountLabel;
    private JButton selectionButton;
    private JButton addButton;
    private JButton backupSelectedBoardsButton;

    @Inject
    private Logger logger;

    @Inject
    private SettingsDao settingsDao;

    @Inject
    private BoardDao boardDao;

    @PostConstruct
    private void init() {
        boardsList.setOnBoardUpdate(boardDao::save);

        setupBackupBoardsButton();
        setupAddButton();
        setupSelectionButton();
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

    private void setupBackupBoardsButton() {
        final JPopupMenu menu = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JMenuItem(I18n.get("ui.runBackups.backupButton.incrementalBackup"));
        menuItem.addActionListener(e -> this.runBackup(FetchPinsWorkerMode.INCREMENTAL_UPDATE));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.backupButton.fullBackup"));
        menuItem.addActionListener(e -> this.runBackup(FetchPinsWorkerMode.FULL_UPDATE));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.backupButton.downloadMissing"));
        menuItem.addActionListener(e -> this.runBackup(FetchPinsWorkerMode.DOWNLOAD_ONLY));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.backupButton.deleteSelected"));
        menuItem.addActionListener(e -> {
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
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.backupButton.openTargetDirectory"));
        menuItem.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(settingsDao.getImageStore());
            } catch (IOException e1) {
                logger.log(Level.SEVERE, "Error opening image store", e);
            }
        });
        menu.add(menuItem);

        backupSelectedBoardsButton.addActionListener(e -> menu.show(backupSelectedBoardsButton, 0, 0));
    }

    private void setupAddButton() {
        final JPopupMenu menu = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JMenuItem(I18n.get("ui.runBackups.addButton.fetchBoards"));
        menuItem.addActionListener(e -> {
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
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.addButton.addLocalFolder"));
        menuItem.addActionListener(evt -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);

            int result = fileChooser.showOpenDialog(addButton);
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
        menu.add(menuItem);

        addButton.addActionListener(e -> menu.show(addButton, 0, 0));
    }

    private void setupSelectionButton() {
        final JPopupMenu menu = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JMenuItem(I18n.get("ui.runBackups.selectionButton.selectAll"));
        menuItem.addActionListener(e -> boardsList.updateSelectionForAll(true));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.selectionButton.selectNone"));
        menuItem.addActionListener(e -> boardsList.updateSelectionForAll(false));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.selectionButton.selectNewBoards"));
        menuItem.addActionListener(e -> boardsList.updateSelectionFor(item ->
                item.getBoard().getPins().size() == 0));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.selectionButton.selectIncompleteBoards"));
        menuItem.addActionListener(e -> boardsList.updateSelectionFor(item ->
                item.getBoard().getPins().stream().anyMatch(pin -> pin.getFileOnDisk() == null)));
        menu.add(menuItem);

        selectionButton.addActionListener(e -> menu.show(selectionButton, 0, 0));
    }

    private void runBackup(FetchPinsWorkerMode mode) {

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

                                                    if (!board.isLocalFolder()) {
                                                        PinsDownloadWorker downloadWorker = new PinsDownloadWorker(task, board, imageStore);
                                                        downloadWorker.execute();
                                                        // Run get() in order to block wrapping worker thread
                                                        downloadWorker.get();
                                                    }

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
    }
}
