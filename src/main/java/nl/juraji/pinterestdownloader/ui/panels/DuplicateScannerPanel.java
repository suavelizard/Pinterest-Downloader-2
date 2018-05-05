package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.PinDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.DuplicatePinSetList;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSetRenderer;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.workers.DuplicateScanWorker;
import nl.juraji.pinterestdownloader.util.workers.WrappingWorker;

import javax.annotation.PostConstruct;
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
    private JList<DuplicatePinSet> duplicateSetContentsList;

    @Inject
    private PinDao pinDao;

    @Inject
    private Instance<ProgressIndicator> indicatorInst;

    @PostConstruct
    private void init() {
        setupStartScanButton();
        setupDuplicateSetContentsList();
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    private void setupDuplicateSetContentsList() {
        DefaultListModel<DuplicatePinSet> listModel = new DefaultListModel<>();
        duplicateSetContentsList.setModel(new DefaultListModel<>());
        duplicateSetContentsList.setCellRenderer(new DuplicatePinSetRenderer(true));

        duplicateSetList.onSetSelected(duplicatePinSet -> {
            listModel.clear();
            listModel.addElement(duplicatePinSet);
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
                        List<Pin> allPins = pinDao.get();
                        pinDao.initPinImageHashes(allPins);

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
