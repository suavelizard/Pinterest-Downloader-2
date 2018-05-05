package nl.juraji.pinterestdownloader.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 24-4-2018.
 * Pinterest Downloader
 */
public class PlaceholderTextField extends JTextField {
    private String placeholder = "";

    @SuppressWarnings("unused")
    public String getPlaceholder() {
        return placeholder;
    }

    @SuppressWarnings("unused")
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        if (placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getDisabledTextColor());
        g2d.drawString(
                placeholder,
                getInsets().left,
                g.getFontMetrics().getMaxAscent() + getInsets().top
        );
    }
}
