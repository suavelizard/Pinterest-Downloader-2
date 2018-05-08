package nl.juraji.pinterestdownloader.ui;

import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.Icons;
import nl.juraji.pinterestdownloader.ui.panels.DuplicateScannerPanel;
import nl.juraji.pinterestdownloader.ui.panels.RunBackupsPanel;
import nl.juraji.pinterestdownloader.ui.panels.SettingsPanel;
import nl.juraji.pinterestdownloader.ui.panels.WindowPane;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.workers.DbPinValidityCheckWorker;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 5-5-2018.
 * Pinterest Downloader
 */
@Default
public class MainWindowFrame extends JFrame {
    private JPanel contentPane;
    private JPanel windowContainer;

    private JButton settingsButton;
    private JButton runBackupsButton;
    private JButton duplicateScannerButton;
    private JLabel devInfoLabel;

    private AtomicReference<WindowPane> currentPaneRef;

    @Inject
    private Logger logger;

    @Inject
    private Instance<WindowPane> windowPanes;

    public MainWindowFrame() {
        super(I18n.get("ui.applicationName"));

        currentPaneRef = new AtomicReference<>();

        setContentPane(contentPane);
        setIconImage(Icons.getApplicationIcon());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setLocationRelativeTo(null);
    }

    @PostConstruct
    private void init() {
        runLocalCacheIntegrityCheck();
        setupDevInfoLabel();
        setupTabButtons();

        setVisible(true);
        windowContainer.setVisible(true);

        switchPanel(windowPanes.select(SettingsPanel.class));
    }

    private void setupTabButtons() {
        settingsButton.addActionListener(e -> switchPanel(windowPanes.select(SettingsPanel.class)));
        runBackupsButton.addActionListener(e -> switchPanel(windowPanes.select(RunBackupsPanel.class)));
        duplicateScannerButton.addActionListener(e -> switchPanel(windowPanes.select(DuplicateScannerPanel.class)));
    }

    private void switchPanel(Instance<? extends WindowPane> instance) {
        WindowPane currentPane = currentPaneRef.get();
        windowContainer.removeAll();

        if (currentPane != null) {
            windowPanes.destroy(currentPane);
        }

        WindowPane windowPane = instance.get();
        currentPaneRef.set(windowPane);

        windowContainer.add(windowPane.getContentPane());
        ((CardLayout) windowContainer.getLayout()).first(windowContainer);
    }

    private void setupDevInfoLabel() {
        devInfoLabel.setToolTipText(I18n.get("ui.main.devInfoBar.link"));
        devInfoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        devInfoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(I18n.get("ui.main.devInfoBar.link")));
                    }
                } catch (IOException | URISyntaxException e1) {
                    logger.log(Level.SEVERE, "Error opening browser", e);
                }
            }
        });
    }

    private void runLocalCacheIntegrityCheck() {
        BoardDao dao = CDI.current().select(BoardDao.class).get();

        FormUtils.FormLock formLock = FormUtils.lockForm(contentPane);
        final DbPinValidityCheckWorker worker = new DbPinValidityCheckWorker(dao.get(Pin.class)) {
            @Override
            protected void process(List<Pin> chunks) {
                dao.save(chunks);
            }

            @Override
            protected void done() {
                super.done();
                formLock.unlock();
            }
        };

        worker.execute();
    }
}
