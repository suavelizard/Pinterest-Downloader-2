package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.ui.components.PlaceholderTextField;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Juraji on 24-4-2018.
 * Pinterest Downloader
 */
@Default
public class SettingsPanel implements WindowPane {

    private JPanel contentPane;

    private JTextField pinterestUsernameField;
    private JButton browseButton;
    private PlaceholderTextField imageOutputLocationField;

    private JButton settingsSaveButton;
    private JPasswordField pinterestPasswordField;
    private JButton openOutputLocationButton;

    @Inject
    private SettingsDao settings;

    public SettingsPanel() throws HeadlessException {
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    @PostConstruct
    private void init() {
        setupSettingsForm();
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

        openOutputLocationButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(settings.getImageStore());
            } catch (IOException e1) {
                e1.printStackTrace();
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
