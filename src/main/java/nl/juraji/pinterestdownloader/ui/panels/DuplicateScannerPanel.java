package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.DuplicatePinSetContentsList;
import nl.juraji.pinterestdownloader.ui.components.DuplicatePinSetList;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.PinPreviewImageCache;
import nl.juraji.pinterestdownloader.util.workers.WrappingWorker;
import nl.juraji.pinterestdownloader.workers.DuplicateScanWorker;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.swing.*;
import java.util.List;

/**
 * Created by Juraji on 1-5-2018.
 * Pinterest Downloader
 */
@Default
public class DuplicateScannerPanel implements WindowPane {
    private JPanel contentPane;
    private JButton startScanButton;
    private DuplicatePinSetList duplicateSetList;
    private DuplicatePinSetContentsList duplicateSetContentsList;
    private JButton deletePinsButton;

    @Inject
    private BoardDao boardDao;

    @Inject
    private Instance<ProgressIndicator> indicatorInst;

    @PostConstruct
    private void init() {
        setupStartScanButton();
        setupDeletePinsButton();
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
        duplicateSetList.addListSelectionListener(e ->
                duplicateSetContentsList.setDuplicatePinSet(
                        duplicateSetList.getModel().getElementAt(e.getFirstIndex())));
    }

    private void setupDeletePinsButton() {
        deletePinsButton.addActionListener(e -> {
            if (duplicateSetContentsList.hasItems()) {
                List<Pin> pins = duplicateSetContentsList.getSelectedValuesList();
                if (pins.size() > 0) {
                    int choice = JOptionPane.showConfirmDialog(contentPane,
                            I18n.get("ui.duplicateScanner.deletePins.alert"),
                            I18n.get("ui.duplicateScanner.deletePins.alert.title", pins.size()),
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        pins.forEach(pin -> {
                            final boolean deleted = pin.getFileOnDisk().delete();
                            if (deleted) {
                                final Board board = pin.getBoard();
                                board.getPins().remove(pin);
                                boardDao.save(board);
                                pin.setFileOnDisk(null);
                            }
                        });

                        duplicateSetContentsList.repaint();
                    }
                }
            }
        });
    }

    private void setupStartScanButton() {
        startScanButton.addActionListener(evt -> {
            int choice = JOptionPane.showConfirmDialog(contentPane,
                    I18n.get("ui.runBackups.scan.alert"),
                    I18n.get("ui.runBackups.scan.alert.title"),
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);

                WrappingWorker worker = new WrappingWorker() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        List<Pin> allPins = boardDao.getAllPins();
                        boardDao.initPinImageHashes(allPins);

                        DuplicateScanWorker worker = new DuplicateScanWorker(indicatorInst.get(), allPins);
                        worker.execute();
                        List<DuplicatePinSet> duplicatePinSets = worker.get();

                        duplicateSetList.addSets(duplicatePinSets);
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
