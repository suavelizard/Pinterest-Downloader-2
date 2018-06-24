package nl.juraji.pinterestdownloader.ui;

import nl.juraji.pinterestdownloader.executors.PinValidityCheckExecutor;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.Icons;
import nl.juraji.pinterestdownloader.ui.components.TabWindow;
import nl.juraji.pinterestdownloader.ui.components.TasksList;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.ui.panels.DuplicateScannerPanel;
import nl.juraji.pinterestdownloader.ui.panels.RunBackupsPanel;
import nl.juraji.pinterestdownloader.ui.panels.SettingsPanel;
import nl.juraji.pinterestdownloader.util.ArrayListModel;
import nl.juraji.pinterestdownloader.util.FormUtils;
import nl.juraji.pinterestdownloader.util.WrappingWorker;
import nl.juraji.pinterestdownloader.util.webdrivers.WebDriverPool;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
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
@Singleton
public class MainWindowFrame extends JFrame {
    private JPanel contentPane;
    private JLabel devInfoLabel;
    private JTabbedPane tabContainer;
    private TasksList tasksList;
    private JProgressBar allTasksProgress;
    private JPanel tasksPane;

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
        tasksList.addListDataListener(new ListDataListener() {
            private final ArrayListModel<Task> model = (ArrayListModel<Task>) tasksList.getModel();

            @Override
            public void intervalAdded(ListDataEvent e) {
                tasksPane.setVisible(true);
                allTasksProgress.setMaximum(model.getSize());
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                final int c = (e.getIndex1() - e.getIndex0()) + 1;
                if ((model.size() - c) > 0) {
                    allTasksProgress.setValue(allTasksProgress.getValue() + c);
                } else {
                    allTasksProgress.setValue(0);
                    allTasksProgress.setMaximum(0);
                    tasksPane.setVisible(false);
                }
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
            }
        });

        runLocalCacheIntegrityCheck();
        setupDevInfoLabel();
        setupTabContainer();

        setVisible(true);
    }

    @PreDestroy
    private void preDestroy() {
        // Shutdown the webdriver pool utility
        WebDriverPool.shutdown();
    }

    private void setupTabContainer() {
        final AtomicInteger prevActiveTab = new AtomicInteger(-1);
        final List<TabWindow> tabWindows = new ArrayList<>();

        tabWindows.add(windowPanes.select(SettingsPanel.class).get());
        tabWindows.add(windowPanes.select(RunBackupsPanel.class).get());
        tabWindows.add(windowPanes.select(DuplicateScannerPanel.class).get());

        tabWindows.forEach(tabWindow -> tabContainer.add(tabWindow.getTitle(), tabWindow.getContentPane()));
        tabContainer.addChangeListener(e -> {
            final int selectedIndex = tabContainer.getSelectedIndex();
            final int prevIndex = prevActiveTab.getAndUpdate(i -> selectedIndex);
            if (prevIndex > -1) {
                tabWindows.get(prevIndex).deactivate();
            }
            tabWindows.get(selectedIndex).activate();
        });

        tabWindows.get(0).activate();
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
        final Task task = TasksList.newTask(I18n.get("ui.task.checkIntegrity"));

        new WrappingWorker() {
            @Override
            protected Void doInBackground() throws Exception {
                final PinValidityCheckExecutor executor = new PinValidityCheckExecutor(task, dao.get(Pin.class));
                final List<Pin> pins = executor.call();
                dao.save(pins);
                return null;
            }

            @Override
            protected void done() {
                task.complete();
                formLock.unlock();
            }
        }.execute();
    }
}
