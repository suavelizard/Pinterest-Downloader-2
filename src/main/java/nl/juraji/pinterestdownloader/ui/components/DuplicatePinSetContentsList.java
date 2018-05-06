package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.components.renderers.PinListItemRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Juraji on 5-5-2018.
 * Pinterest Downloader
 */
public class DuplicatePinSetContentsList extends JList<Pin> {
    private JPopupMenu popupMenu;

    public DuplicatePinSetContentsList() {
        setCellRenderer(new PinListItemRenderer());
        setModel(new DefaultListModel<>());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setupContextMenuTrigger();
    }

    public void setDuplicatePinSet(DuplicatePinSet set) {
        final DefaultListModel<Pin> model = (DefaultListModel<Pin>) this.getModel();

        clearSelection();

        model.clear();
        set.getPins().forEach(model::addElement);

        repaint();
    }

    public boolean hasItems() {
        return getModel().getSize() > 0;
    }

    public void clear() {
        DefaultListModel<Pin> model = (DefaultListModel<Pin>) getModel();
        model.clear();
        repaint();
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    private void setupContextMenuTrigger() {
        DuplicatePinSetContentsList that = this;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                checkPopup(e);
            }

            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger() && popupMenu != null) {
                    setSelectedIndex(locationToIndex(e.getPoint()));
                    popupMenu.show(that, e.getX(), e.getY());
                }
            }
        });
    }
}
