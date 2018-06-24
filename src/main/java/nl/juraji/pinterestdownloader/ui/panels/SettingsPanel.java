package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.SettingsDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.PlaceholderTextField;
import nl.juraji.pinterestdownloader.ui.components.TabWindow;
import nl.juraji.pinterestdownloader.util.DocumentChangeListener;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

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

    private JPasswordField pinterestPasswordField;
    private JCheckBox enableMultithreadingCheckBox;

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
        enableMultithreadingCheckBox.setSelected(settings.isEnableMultithreading());

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
                settings.save();
            }
        });

        pinterestUsernameField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            settings.setPinterestUsername(pinterestUsernameField.getText());
            settings.save();
        });

        pinterestPasswordField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            settings.setPinterestPassword(String.valueOf(pinterestPasswordField.getPassword()));
            settings.save();
        });

        enableMultithreadingCheckBox.addItemListener(e -> {
            settings.setEnableMultithreading(enableMultithreadingCheckBox.isSelected());
            settings.save();
        });
    }
}
