package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSetRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicatePinSetList extends JList<DuplicatePinSet> {
    public DuplicatePinSetList() {
        setCellRenderer(new DuplicatePinSetRenderer(false));
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setModel(new DefaultListModel<>());
    }

    public void addSets(List<DuplicatePinSet> duplicatePinSets) {
        DefaultListModel<DuplicatePinSet> model = (DefaultListModel<DuplicatePinSet>) getModel();
        duplicatePinSets.forEach(model::addElement);
        repaint();
    }

    public void onSetSelected(Consumer<DuplicatePinSet> runnable) {
        this.addListSelectionListener(e -> runnable.accept(getModel().getElementAt(e.getFirstIndex())));
    }
}
