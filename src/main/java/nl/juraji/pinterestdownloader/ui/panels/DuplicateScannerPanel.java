package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.executors.DuplicateScanExecutor;
import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.*;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.WrappingWorker;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 1-5-2018.
 * Pinterest Downloader
 */
@Default
public class DuplicateScannerPanel implements TabWindow {
    private JPanel contentPane;
    private JButton startScanButton;
    private DuplicatePinSetList duplicateSetList;
    private PinsList duplicateSetContentsList;
    private JCheckBox scanPerBoardCheckBox;

    @Inject
    private SettingsDao settings;

    @Inject
    private BoardDao boardDao;

    @PostConstruct
    private void init() {
        setupStartScanButton();
        setupDuplicateSetContentsList();
    }

    @Override
    public String getTitle() {
        return I18n.get("ui.duplicateScanner.tabTitle");
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

    private void setupDuplicateSetContentsList() {
        duplicateSetList.addListSelectionListener(e -> {
            final DuplicatePinSet set = duplicateSetList.getSelectedValue();
            if (set != null) {
                duplicateSetContentsList.setDuplicatePinSet(set);
            }
        });

        duplicateSetContentsList.setPopupMenu(new PinsListContextMenu(duplicateSetContentsList, boardDao));
    }

    private void setupStartScanButton() {
        startScanButton.addActionListener(evt -> {
            int choice = JOptionPane.showConfirmDialog(contentPane,
                    I18n.get("ui.duplicateScanner.scan.alert"),
                    I18n.get("ui.duplicateScanner.scan.alert.title"),
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);
                duplicateSetList.clear();
                duplicateSetContentsList.clear();

                new WrappingWorker() {
                    private ExecutorService executorService;

                    @Override
                    protected Void doInBackground() throws Exception {
                        if(settings.isEnableMultithreading()) {
                            executorService = Executors.newFixedThreadPool(4);
                        } else {
                            executorService = Executors.newSingleThreadExecutor();
                        }

                        final List<Board> boards = boardDao.get(Board.class).stream()
                                .filter(board -> board.getPins().size() > 0)
                                .map(boardDao::initPinImageHashes)
                                .collect(Collectors.toList());

                        final ArrayList<Future> backupTaskFutures = new ArrayList<>();

                        if (scanPerBoardCheckBox.isSelected()) {
                            boards.stream()
                                    .map(this::performScanRunnable)
                                    .map(executorService::submit)
                                    .forEach(backupTaskFutures::add);
                        } else {
                            final Board allPinsBoard = new Board();
                            allPinsBoard.setName(I18n.get("worker.duplicateScanWorker.allBoards"));
                            boards.forEach(board -> allPinsBoard.getPins().addAll(board.getPins()));

                            final Future future = executorService.submit(this.performScanRunnable(allPinsBoard));
                            backupTaskFutures.add(future);
                        }

                        for (Future backupTaskFuture : backupTaskFutures) {
                            // Join all future threads in order to block the wrapping worker thread
                            backupTaskFuture.get();
                        }

                        return null;
                    }

                    private Runnable performScanRunnable(Board board) {
                        return () -> {
                            final Task scantask = TasksList.newTask(I18n.get("ui.task.duplicateScan", board.getName()));
                            try {
                                final DuplicateScanExecutor executor = new DuplicateScanExecutor(scantask, board);
                                duplicateSetList.addSets(executor.call());
                            } catch (Exception e) {
                                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed scanning duplicates", e);
                            } finally {
                                scantask.complete();
                            }
                        };
                    }

                    @Override
                    protected void done() {
                        if (executorService != null) {
                            executorService.shutdown();
                        }
                        formLock.unlock();
                    }
                }.execute();
            }
        });
    }
}
