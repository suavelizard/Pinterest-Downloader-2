package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.ui.dialogs.Task;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 15-5-2018.
 * Pinterest Downloader
 */
public class TaskRenderer implements ListCellRenderer<Task> {

    private final Color currentItemColor = new Color(183, 255, 189);

    @Override
    public Component getListCellRendererComponent(JList<? extends Task> list, Task value, int index, boolean isSelected, boolean cellHasFocus) {
        if (index == 0) {
            value.getContentPane().setBackground(currentItemColor);
        }

        return value.getContentPane();
    }
}
