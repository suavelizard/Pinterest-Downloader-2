package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.components.renderers.PinListItemRenderer;
import nl.juraji.pinterestdownloader.util.ArrayListModel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by Juraji on 5-5-2018.
 * Pinterest Downloader
 */
public class PinsList extends JList<Pin> {
    private JPopupMenu popupMenu;

    public PinsList() {
        setCellRenderer(new PinListItemRenderer());
        setModel(new ArrayListModel<>());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setupContextMenuTrigger();
    }

    public void setDuplicatePinSet(DuplicatePinSet set) {
        this.setPins(set.getPins());
    }

    public void setPins(List<Pin> pins) {
        final ArrayListModel<Pin> model = (ArrayListModel<Pin>) this.getModel();

        clearSelection();

        model.clear();
        model.addAll(pins);
    }

    public void clear() {
        ((ArrayListModel<Pin>) getModel()).clear();
        repaint();
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    private void setupContextMenuTrigger() {
        final PinsList that = this;
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
