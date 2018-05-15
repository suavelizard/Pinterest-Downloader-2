package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.*;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.workers.WrappingWorker;
import nl.juraji.pinterestdownloader.workers.DuplicateScanWorker;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
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

                WrappingWorker worker = new WrappingWorker() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        final List<Board> boards = boardDao.get(Board.class).stream()
                                .filter(board -> board.getPins().size() > 0)
                                .map(boardDao::initPinImageHashes)
                                .collect(Collectors.toList());
                        List<DuplicateScanWorker> workers = new ArrayList<>();

                        if (scanPerBoardCheckBox.isSelected()) {
                            boards.stream()
                                    .map(board -> {
                                        final Task scantask = TasksList.newTask();
                                        return new DuplicateScanWorker(scantask, board) {
                                            @Override
                                            protected void process(List<DuplicatePinSet> chunks) {
                                                duplicateSetList.addSets(chunks);
                                            }

                                            @Override
                                            protected void done() {
                                                scantask.complete();
                                            }
                                        };
                                    })
                                    .forEach(workers::add);
                        } else {
                            Board allPinsBoard = new Board();
                            allPinsBoard.setName(I18n.get("worker.duplicateScanWorker.allBoards"));
                            boards.stream()
                                    .map(Board::getPins)
                                    .forEach(pins -> allPinsBoard.getPins().addAll(pins));

                            final Task scantask = TasksList.newTask();
                            workers.add(new DuplicateScanWorker(scantask, allPinsBoard) {
                                @Override
                                protected void process(List<DuplicatePinSet> chunks) {
                                    duplicateSetList.addSets(chunks);
                                }

                                @Override
                                protected void done() {
                                    scantask.complete();
                                }
                            });
                        }

                        for (DuplicateScanWorker worker : workers) {
                            worker.execute();
                            worker.get();
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        formLock.unlock();
                    }
                };

                worker.execute();
            }
        });
    }
}
