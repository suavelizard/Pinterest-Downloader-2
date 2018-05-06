package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 6-5-2018.
 * Pinterest Downloader
 */
public abstract class DuplicatePinSetContentsListContextMenu extends JPopupMenu {

    private final Logger logger = Logger.getLogger(getClass().getName());

    public DuplicatePinSetContentsListContextMenu() {
        JMenuItem item;

        this.add(item = new JMenuItem(I18n.get("ui.duplicateScanner.pinContextMenu.browsePinterestPin")));
        item.addActionListener(e -> {
            final Pin pin = getSelectedPin();
            if (pin != null && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(pin.getUrl()));
                } catch (IOException | URISyntaxException e1) {
                    logger.log(Level.SEVERE, "Error opening browser", e);
                }
            }
        });

        this.add(item = new JMenuItem(I18n.get("ui.duplicateScanner.pinContextMenu.deleteLocally")));
        item.addActionListener(e -> {
            final Pin pin = getSelectedPin();
            if (pin != null) {
                int choice = JOptionPane.showConfirmDialog(this,
                        I18n.get("ui.duplicateScanner.pinContextMenu.deletePin.alert"),
                        I18n.get("ui.duplicateScanner.pinContextMenu.deletePin.alert.title", pin.getPinId()),
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    deletePin(pin);
                    repaintParent();
                }
            }
        });

        this.add(item = new JMenuItem(I18n.get("ui.duplicateScanner.pinContextMenu.openFile")));
        item.addActionListener(e -> {
            final Pin pin = getSelectedPin();
            if (pin != null && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(pin.getFileOnDisk());
                } catch (IOException e1) {
                    logger.log(Level.SEVERE, "Error opening file", e);
                }
            }
        });
    }

    public abstract Pin getSelectedPin();

    public abstract void deletePin(Pin pin);

    public abstract void repaintParent();
}
