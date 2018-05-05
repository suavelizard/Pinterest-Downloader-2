package nl.juraji.pinterestdownloader;

import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.MainWindowForm;
import org.jboss.weld.environment.se.Weld;

import javax.enterprise.inject.spi.CDI;
import javax.swing.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Juraji on 22-4-2018.
 * Pinterest Downloader
 */
public final class Container {
    private static final AtomicReference<Container> CONTAINER = new AtomicReference<>();
    private final Weld weld;
    private final boolean debugMode;

    private Container(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        debugMode = Arrays.stream(args).anyMatch(s -> s.equals("--debug"));

        // Initialize Weld
        weld = new Weld(I18n.get("ui.applicationName"));
        weld.initialize();

        // Bootstrap Main class
        CDI.current().select(MainWindowForm.class).get();
    }

    public static void main(String[] args) throws Exception {
        CONTAINER.set(new Container(args));
    }

    @SuppressWarnings("unused")
    public static void shutdown() {
        CONTAINER.get().weld.shutdown();
    }

    public static boolean isDebugMode() {
        return CONTAINER.get().debugMode;
    }
}
