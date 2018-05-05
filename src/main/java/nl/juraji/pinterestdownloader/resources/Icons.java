package nl.juraji.pinterestdownloader.resources;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 24-4-2018.
 * Pinterest Downloader
 */
public final class Icons {

    private static final String ICON_URI_PATTERN = "/icons/{}.png";
    private static final String APPLICATION_ICON_NAME = "application-icon";

    private Icons() {
    }

    public static Image get(String name) {
        String uri = ICON_URI_PATTERN.replace("{}", name);
        ImageIcon imageIcon = new ImageIcon(Icons.class.getResource(uri));
        return imageIcon.getImage();
    }

    public static Image getApplicationIcon() {
        return get(APPLICATION_ICON_NAME);
    }
}
