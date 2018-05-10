package nl.juraji.pinterestdownloader.ui.components;

import javax.swing.*;

/**
 * Created by Juraji on 5-5-2018.
 * Pinterest Downloader
 */
public interface TabWindow {
    String getTitle();

    JPanel getContentPane();

    void activate();

    void deactivate();
}
