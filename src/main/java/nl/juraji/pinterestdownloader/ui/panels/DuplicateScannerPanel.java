package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.DuplicatePinSetList;
import nl.juraji.pinterestdownloader.ui.components.PinsList;
import nl.juraji.pinterestdownloader.ui.components.PinsListContextMenu;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.PinPreviewImageCache;
import nl.juraji.pinterestdownloader.util.workers.WrappingWorker;
import nl.juraji.pinterestdownloader.workers.DuplicateScanWorker;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
public class DuplicateScannerPanel implements WindowPane {
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

    @PreDestroy
    private void onDestroy() {
        PinPreviewImageCache.destroy();
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
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
                                    .map(board -> new DuplicateScanWorker(board) {
                                        @Override
                                        protected void process(List<DuplicatePinSet> chunks) {
                                            duplicateSetList.addSets(chunks);
                                        }
                                    })
                                    .forEach(workers::add);
                        } else {
                            Board allPinsBoard = new Board();
                            allPinsBoard.setName(I18n.get("worker.duplicateScanWorker.allBoards"));
                            boards.stream()
                                    .map(Board::getPins)
                                    .forEach(pins -> allPinsBoard.getPins().addAll(pins));

                            workers.add(new DuplicateScanWorker(allPinsBoard) {
                                @Override
                                protected void process(List<DuplicatePinSet> chunks) {
                                    duplicateSetList.addSets(chunks);
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
