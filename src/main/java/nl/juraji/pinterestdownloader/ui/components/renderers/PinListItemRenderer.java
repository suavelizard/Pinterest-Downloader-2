package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.util.FileUtils;
import nl.juraji.pinterestdownloader.util.PinPreviewImageCache;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 5-5-2018.
 * Pinterest Downloader
 */
public class PinListItemRenderer implements ListCellRenderer<Pin> {
    private final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends Pin> list, Pin value, int index, boolean isSelected, boolean cellHasFocus) {
        final JLabel label = (JLabel) defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        label.setToolTipText(I18n.get("ui.duplicateScanner.duplicatePins.label.tooltip"));
        label.setText(I18n.get(
                "ui.duplicateScanner.duplicatePins.label.template",
                value.getFileOnDisk()==null?I18n.get("ui.duplicateScanner.duplicatePins.label.deletedPin"):value.getFileOnDisk().getAbsolutePath(),
                value.getPinId(),
                FileUtils.bytesInHumanReadable(value.getImageHash().getImageSizeBytes()),
                value.getImageHash().getImageWidth(),
                value.getImageHash().getImageHeight(),
                value.getImageHash().getContrast()
        ));

        final Icon previewImage = PinPreviewImageCache.getPreview(value);
        label.setIcon(previewImage);

        return label;
    }
}