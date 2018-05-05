package nl.juraji.pinterestdownloader.ui;

import nl.juraji.pinterestdownloader.cdi.annotations.Dialog;
import nl.juraji.pinterestdownloader.cdi.annotations.Indicator;
import nl.juraji.pinterestdownloader.cdi.producers.DialogProducer;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.PinDao;
import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.Icons;
import nl.juraji.pinterestdownloader.ui.components.PlaceholderTextField;
import nl.juraji.pinterestdownloader.ui.dialogs.BackupPinsDialog;
import nl.juraji.pinterestdownloader.ui.dialogs.DuplicateScanDialog;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.workers.workerutils.SwingWorkerDoneListener;
import nl.juraji.pinterestdownloader.util.workers.DbPinValidityCheckWorker;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 24-4-2018.
 * Pinterest Downloader
 */
@Default
public class MainWindowForm extends JFrame {

    private JPanel contentPane;

    private JTextField pinterestUsernameField;
    private JButton browseButton;
    private PlaceholderTextField imageOutputLocationField;

    private JPanel manageLibraryPanel;
    private JButton backupPinsButton;
    private JButton duplicateScanButton;
    private JButton settingsSaveButton;
    private JPasswordField pinterestPasswordField;

    @Inject
    private Logger logger;

    @Inject
    private SettingsDao settings;

    @Inject
    private PinDao pinDao;

    @Inject
    @Dialog(BackupPinsDialog.class)
    private Instance<JDialog> backupPinsDialog;


    @Inject
    @Dialog(DuplicateScanDialog.class)
    private Instance<JDialog> duplicateScanDialog;

    @Inject
    @Indicator
    private Instance<ProgressIndicator> indicatorInstance;

    public MainWindowForm() throws HeadlessException {
        super(I18n.get("ui.applicationName"));
        setName(DialogProducer.MAIN_FRAME_NAME);

        setContentPane(contentPane);
        setIconImage(Icons.getApplicationIcon());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setLocationRelativeTo(null);
    }

    @PostConstruct
    private void init() {
        setupSettingsForm();
        setupManageLibraryButtons();
        updateManageLibraryPanelVisibility();

        setVisible(true);

        runInitialization();
    }

    private void runInitialization() {
        FormUtils.FormLock formLock = FormUtils.lockForm(this, false);
        final DbPinValidityCheckWorker worker = new DbPinValidityCheckWorker(indicatorInstance.get(), pinDao.get());
        worker.execute();
        worker.addPropertyChangeListener(new SwingWorkerDoneListener() {
            @Override
            protected void onWorkerDone() {
                try {
                    List<Pin> updatedPins = worker.get();
                    pinDao.save(updatedPins);
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "Error validation pins to file associations!", e);
                } finally {
                    formLock.unlock();
                }
            }
        });
    }

    private void setupSettingsForm() {
        pinterestUsernameField.setText(settings.getPinterestUsername());
        pinterestPasswordField.setText(settings.getPinterestPassword());

        if (settings.getImageStore() != null) {
            imageOutputLocationField.setText(settings.getImageStore().getAbsolutePath());
        }

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);

            int result = fileChooser.showOpenDialog(browseButton);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                settings.setImageStore(selectedFile);
                imageOutputLocationField.setText(selectedFile.getAbsolutePath());
            }
        });

        settingsSaveButton.addActionListener(e -> {
            String username = pinterestUsernameField.getText();
            settings.setPinterestUsername(username);
            String password = new String(pinterestPasswordField.getPassword());
            settings.setPinterestPassword(password);

            settings.save();
            updateManageLibraryPanelVisibility();
        });
    }

    private void setupManageLibraryButtons() {
        backupPinsButton.addActionListener(e -> backupPinsDialog.get().setVisible(true));

        duplicateScanButton.addActionListener(e -> duplicateScanDialog.get().setVisible(true));
    }

    private void updateManageLibraryPanelVisibility() {
        manageLibraryPanel.setVisible(settings.validate());
    }
}
