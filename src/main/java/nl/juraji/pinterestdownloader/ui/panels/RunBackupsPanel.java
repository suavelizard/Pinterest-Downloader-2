package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.executors.*;
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
import nl.juraji.pinterestdownloader.util.WrappingWorker;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        menuItem.addActionListener(e -> this.runBackup(BackupMode.INCREMENTAL_UPDATE));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.backupButton.fullBackup"));
        menuItem.addActionListener(e -> this.runBackup(BackupMode.FULL_UPDATE));
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.backupButton.downloadMissing"));
        menuItem.addActionListener(e -> this.runBackup(BackupMode.DOWNLOAD_ONLY));
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
        menuItem.addActionListener(this::fetchBoardsAction);
        menu.add(menuItem);

        menuItem = new JMenuItem(I18n.get("ui.runBackups.addButton.addLocalFolder"));
        menuItem.addActionListener(this::addLocalFolderAction);
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
                    boardCountLabel.setText(I18n.get("ui.runBackups.boardCount", boardsList.getModel().getSize()));
                }
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

    private void runBackup(BackupMode mode) {

        List<BoardCheckboxListItem> selectedItems = boardsList.getSelectedItems();

        int choice = JOptionPane.showConfirmDialog(contentPane,
                I18n.get("ui.runBackups.backupBoards.alert"),
                I18n.get("ui.runBackups.backupBoards.alert.title", selectedItems.size()),
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (selectedItems.size() > 0) {
                final FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);

                new WrappingWorker() {
                    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

                    @Override
                    protected Void doInBackground() throws Exception {
                        final ArrayList<Future> backupTaskFutures = new ArrayList<>();

                        for (BoardCheckboxListItem selectedItem : selectedItems) {
                            final Board board = selectedItem.getBoard();
                            final Task task = TasksList.newTask(I18n.get("ui.task.backupBoard", board.getName()));

                            final Future<?> backupTaskFuture = executorService.submit(() -> {
                                try {

                                    // Step 1: Fetch pins
                                    List<Pin> fetchedPins = null;
                                    if (board.isLocalFolder()) {
                                        fetchedPins = new LocalFolderScanExecutor(task, board).call();
                                    } else if (!BackupMode.DOWNLOAD_ONLY.equals(mode)) {
                                        fetchedPins = new FetchPinsExecutor(task, settingsDao.getPinterestUsername(),
                                                settingsDao.getPinterestPassword(), board, mode).call();
                                    }

                                    if (fetchedPins != null) {
                                        if (!BackupMode.INCREMENTAL_UPDATE.equals(mode)) {
                                            board.getPins().clear();
                                        }
                                        board.getPins().addAll(fetchedPins);
                                    }

                                    // Step 2: Download missing pins
                                    // The executor will update the given board with remote and local urls
                                    new PinDownloadExecutor(task, board, settingsDao.getImageStore()).call();

                                    // Step 3: Perform Filetype checks
                                    // The executor will update the given board with local urls
                                    new FileTypeCorrectionExecutor(task, board).call();

                                    // Step 4: Generate image hashes
                                    new ImageHasherExecutor(task, board).call();

                                    // Update UI and save changes
                                    selectedItem.updatePinCounts();
                                    boardDao.save(board);
                                } catch (Exception e) {
                                    logger.log(Level.WARNING, "Error fetching pins for " + board.getName(), e);
                                } finally {
                                    task.complete();
                                }
                            });

                            backupTaskFutures.add(backupTaskFuture);
                        }

                        // Wait for all task futures to complete and return
                        for (Future backupTaskFuture : backupTaskFutures) {
                            backupTaskFuture.get();
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        executorService.shutdown();
                        formLock.unlock();
                        super.done();
                    }
                }.execute();
            }
        }
    }

    private void addLocalFolderAction(ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        int result = fileChooser.showOpenDialog((Component) evt.getSource());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Board localBoard = new Board();
            localBoard.setLocalFolder(true);
            localBoard.setName(selectedFile.getName());
            localBoard.setUrl(selectedFile.getAbsolutePath());

            boardDao.save(localBoard);
            boardsList.updateBoards(Collections.singletonList(localBoard), true);
        }
    }

    private void fetchBoardsAction(ActionEvent evt) {
        FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);

        final Task task = TasksList.newTask(I18n.get("ui.task.fetchBoards"));
        final FetchBoardsExecutor executor = new FetchBoardsExecutor(task, settingsDao.getPinterestUsername(),
                settingsDao.getPinterestPassword(), boardDao.getPinterestBoards());

        new WrappingWorker() {
            @Override
            protected Void doInBackground() throws Exception {
                final List<Board> newBoards = executor.call();
                boardDao.save(newBoards);
                boardsList.updateBoards(boardDao.get(Board.class), false);
                boardCountLabel.setText(I18n.get("ui.runBackups.boardCount", boardsList.getModel().getSize()));
                return null;
            }

            @Override
            protected void done() {
                task.complete();
                formLock.unlock();
            }
        }.execute();

    }
}
