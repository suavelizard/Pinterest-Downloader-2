package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicatePinSetList extends JList<DuplicatePinSet> {
    public DuplicatePinSetList() {
        setCellRenderer(new DefaultListCellRenderer());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setModel(new DefaultListModel<>());
    }

    public void addSets(List<DuplicatePinSet> duplicatePinSets) {
        DefaultListModel<DuplicatePinSet> model = (DefaultListModel<DuplicatePinSet>) getModel();
        duplicatePinSets.forEach(model::addElement);
        repaint();
    }

    public void clear() {
        DefaultListModel<DuplicatePinSet> model = (DefaultListModel<DuplicatePinSet>) getModel();
        model.clear();
        repaint();
    }
}
