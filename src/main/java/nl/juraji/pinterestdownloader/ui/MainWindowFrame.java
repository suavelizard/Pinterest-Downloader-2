package nl.juraji.pinterestdownloader.ui;

import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.Icons;
import nl.juraji.pinterestdownloader.ui.components.TabWindow;
import nl.juraji.pinterestdownloader.ui.panels.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 5-5-2018.
 * Pinterest Downloader
 */
@Default
public class MainWindowFrame extends JFrame {
    private JPanel contentPane;
    private JLabel devInfoLabel;
    private JTabbedPane tabContainer;

    @Inject
    private Logger logger;

    @Inject
    private Instance<TabWindow> windowPanes;

    public MainWindowFrame() {
        super(I18n.get("ui.applicationName"));
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
        setupTabContainer();

        setVisible(true);
    }

    private void setupTabContainer() {
        final AtomicInteger prevActiveTab = new AtomicInteger(-1);
        final List<TabWindow> tabWindows = new ArrayList<>();

        tabWindows.add(windowPanes.select(SettingsPanel.class).get());
        tabWindows.add(windowPanes.select(RunBackupsPanel.class).get());
        tabWindows.add(windowPanes.select(DuplicateScannerPanel.class).get());
        tabWindows.add(windowPanes.select(BoardViewPanel.class).get());

        tabWindows.forEach(tabWindow -> tabContainer.add(tabWindow.getTitle(), tabWindow.getContentPane()));
        tabContainer.addChangeListener(e -> {
            final int selectedIndex = tabContainer.getSelectedIndex();
            final int prevIndex = prevActiveTab.getAndUpdate(i -> selectedIndex);
            if (prevIndex > -1) {
                tabWindows.get(prevIndex).deactivate();
            }
            tabWindows.get(selectedIndex).activate();
        });
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
