package nl.juraji.pinterestdownloader.ui.components.renderers;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicatePinSetRenderer implements ListCellRenderer<DuplicatePinSet> {
    private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
    private final boolean renderPinInfo;

    public DuplicatePinSetRenderer(boolean renderPinInfo) {
        this.renderPinInfo = renderPinInfo;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends DuplicatePinSet> list, DuplicatePinSet value, int index, boolean isSelected, boolean cellHasFocus) {
        if (renderPinInfo) {
            // Todo: https://stackoverflow.com/a/14616065
            return buildPinInfoLabel(value);
        } else {
            return defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private Component buildPinInfoLabel(DuplicatePinSet value) {
        return new JLabel(value.toString());
    }
}
