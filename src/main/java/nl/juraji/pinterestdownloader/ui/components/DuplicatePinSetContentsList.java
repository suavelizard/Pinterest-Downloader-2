package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Pin;
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
    public DuplicatePinSetContentsList() {
        setCellRenderer(new PinListItemRenderer());
        setModel(new DefaultListModel<>());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && Desktop.isDesktopSupported()) {
                    try {
                        setSelectedIndex(locationToIndex(evt.getPoint()));
                        final Pin pin = getSelectedValue();
                        Desktop.getDesktop().browse(new URI(pin.getUrl()));
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setDuplicatePinSet(DuplicatePinSet set) {
        final DefaultListModel<Pin> model = (DefaultListModel<Pin>) this.getModel();

        clearSelection();

        model.clear();
        model.addElement(set.getParentPin());
        set.getDuplicates().forEach(model::addElement);

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
}
