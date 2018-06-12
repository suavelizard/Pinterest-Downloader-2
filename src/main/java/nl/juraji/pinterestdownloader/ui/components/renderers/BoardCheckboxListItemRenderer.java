package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.resources.I18n;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

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

        java.util.List<String> statusses = new ArrayList<>();

        if (value.getBoard().isLocalFolder()) {
            statusses.add(I18n.get("ui.runBackups.boardsListItem.label.status.localFolder"));
        }

        if (value.getAvailablePinCount() > 0) {
            statusses.add(I18n.get("ui.runBackups.boardsListItem.label.status.pinCount", value.getAvailablePinCount()));

            if (value.getDownloadedPinCount() == value.getAvailablePinCount()) {
                statusses.add(I18n.get("ui.runBackups.boardsListItem.label.status.allDownloaded"));
            } else {
                long missingPinsCount = value.getAvailablePinCount() - value.getDownloadedPinCount();
                statusses.add(I18n.get("ui.runBackups.boardsListItem.label.status.pinsMissing", missingPinsCount));
            }
        } else {
            statusses.add(I18n.get("ui.runBackups.boardsListItem.label.status.pinCountUnknown"));
        }

        String statusText = statusses.stream()
                .reduce((l, r) -> l + ", " + r)
                .orElse("status unknown");

        setText(I18n.get("ui.runBackups.boardsListItem.label",
                value.getBoard().getName(),
                statusText));

        return this;
    }
}
