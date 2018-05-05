package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.resources.I18n;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
public class BoardCheckboxListItemRenderer extends JCheckBox implements ListCellRenderer<BoardCheckboxListItem> {

    @Override
    public Component getListCellRendererComponent(JList<? extends BoardCheckboxListItem> list, BoardCheckboxListItem value, int index, boolean isSelected, boolean cellHasFocus) {
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setForeground(list.getForeground());
        setBackground(list.getBackground());
        setSelected(value.isSelected());

        if (value.getAvailablePinCount() > 0) {
            if (value.getDownloadedPinCount() == value.getAvailablePinCount()) {
                setText(I18n.get("ui.runBackups.boardsListItem.withPinsComplete",
                        value.getBoard().getName(),
                        value.getAvailablePinCount()
                ));
            } else {
                long missingPinsCount = value.getAvailablePinCount() - value.getDownloadedPinCount();
                setText(I18n.get("ui.runBackups.boardsListItem.withPinsMissing",
                        value.getBoard().getName(),
                        value.getAvailablePinCount(),
                        missingPinsCount
                ));
            }
        } else {
            setText(I18n.get("ui.runBackups.boardsListItem.pinCountUnknown", value.getBoard().getName()));
        }

        return this;
    }
}
