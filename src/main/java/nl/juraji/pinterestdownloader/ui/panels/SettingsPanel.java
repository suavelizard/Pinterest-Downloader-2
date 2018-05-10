package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.TabWindow;
import nl.juraji.pinterestdownloader.ui.components.PlaceholderTextField;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 24-4-2018.
 * Pinterest Downloader
 */
@Default
public class SettingsPanel implements TabWindow {

    private JPanel contentPane;

    private JTextField pinterestUsernameField;
    private JButton browseButton;
    private PlaceholderTextField imageOutputLocationField;

    private JButton settingsSaveButton;
    private JPasswordField pinterestPasswordField;
    private JButton openOutputLocationButton;

    @Inject
    private Logger logger;

    @Inject
    private SettingsDao settings;

    public SettingsPanel() throws HeadlessException {
    }

    @Override
    public String getTitle() {
        return I18n.get("ui.settings.tabTitle");
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    @Override
    public void activate() {
        pinterestUsernameField.setText(settings.getPinterestUsername());
        pinterestPasswordField.setText(settings.getPinterestPassword());

        if (settings.getImageStore() != null) {
            imageOutputLocationField.setText(settings.getImageStore().getAbsolutePath());
        }
    }

    @Override
    public void deactivate() {
    }

    @PostConstruct
    private void init() {
        setupSettingsForm();
    }

    private void setupSettingsForm() {
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

        openOutputLocationButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(settings.getImageStore());
            } catch (IOException e1) {
                logger.log(Level.SEVERE, "Error opening image store", e);
            }
        });

        settingsSaveButton.addActionListener(e -> {
            String username = pinterestUsernameField.getText();
            settings.setPinterestUsername(username);
            String password = new String(pinterestPasswordField.getPassword());
            settings.setPinterestPassword(password);

            settings.save();
        });
    }
}
