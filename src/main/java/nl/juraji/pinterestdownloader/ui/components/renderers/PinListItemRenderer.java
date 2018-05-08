package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.util.FileUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 5-5-2018.
 * Pinterest Downloader
 */
public class PinListItemRenderer implements ListCellRenderer<Pin> {
    private final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends Pin> list, Pin pin, int index, boolean isSelected, boolean cellHasFocus) {
        final JLabel label = (JLabel) defaultListCellRenderer.getListCellRendererComponent(list, pin, index, isSelected, cellHasFocus);

        final String imgPath = getPath(pin);
        final int imageWidth = pin.getImageHash().getImageWidth();
        final int imageHeight = pin.getImageHash().getImageHeight();
        double ratio = (100.0 / imageWidth);
        int tgtWidth = (int) (imageWidth * ratio);
        int tgtHeight = (int) (imageHeight * ratio);

        String wrapper = I18n.get("ui.duplicateScanner.duplicatePins.label.template.wrapper",
                imgPath,
                tgtWidth,
                tgtHeight,
                pin.getBoard().getName(),
                imgPath,
                pin.getPinId(),
                FileUtils.bytesInHumanReadable(pin.getImageHash().getImageSizeBytes()),
                imageWidth,
                imageHeight,
                pin.getImageHash().getContrast());


        label.setText(wrapper);
        label.setInheritsPopupMenu(true);

        return label;
    }

    private String getPath(Pin pin) {
        if (pin.getFileOnDisk() == null) {
            return I18n.get("ui.duplicateScanner.duplicatePins.label.deletedPin");
        } else {
            return pin.getFileOnDisk().getAbsolutePath()
                    .replaceAll("\\\\", "\\\\\\\\");
        }
    }
}
