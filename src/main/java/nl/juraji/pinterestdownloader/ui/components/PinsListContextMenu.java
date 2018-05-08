package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Dao;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 6-5-2018.
 * Pinterest Downloader
 */
public class PinsListContextMenu extends JPopupMenu {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final JList<Pin> owner;
    private final Dao dao;

    public PinsListContextMenu(JList<Pin> owner, Dao dao) {
        this.owner = owner;
        this.dao = dao;

        JMenuItem item;

        this.add(item = new JMenuItem(I18n.get("ui.duplicateScanner.pinContextMenu.browsePinterestPin")));
        item.addActionListener(this::browseAction);

        this.add(item = new JMenuItem(I18n.get("ui.duplicateScanner.pinContextMenu.deleteLocally")));
        item.addActionListener(this::deleteLocally);

        this.add(item = new JMenuItem(I18n.get("ui.duplicateScanner.pinContextMenu.openFile")));
        item.addActionListener(this::openFileAction);
    }

    @SuppressWarnings("unused")
    private void browseAction(ActionEvent event) {
        final Pin pin = owner.getSelectedValue();
        if (pin != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(pin.getUrl()));
            } catch (IOException | URISyntaxException e) {
                logger.log(Level.SEVERE, "Error opening browser", e);
            }
        }
    }

    @SuppressWarnings("unused")
    private void deleteLocally(ActionEvent event) {
        final Pin pin = owner.getSelectedValue();
        if (pin != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    I18n.get("ui.duplicateScanner.pinContextMenu.deletePin.alert"),
                    I18n.get("ui.duplicateScanner.pinContextMenu.deletePin.alert.title", pin.getPinId()),
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                boolean deleted = false;
                try {
                    deleted = pin.getFileOnDisk().delete();
                } catch (Exception ignored) {
                }

                if (deleted) {
                    pin.setFileOnDisk(null);
                    final Board board = dao.get(Board.class, pin.getBoard().getId());
                    board.getPins().remove(pin);
                    dao.save(board);
                }

                owner.repaint();
            }
        }
    }

    @SuppressWarnings("unused")
    private void openFileAction(ActionEvent event) {
        final Pin pin = owner.getSelectedValue();
        if (pin != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(pin.getFileOnDisk());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error opening file", e);
            }
        }
    }
}
