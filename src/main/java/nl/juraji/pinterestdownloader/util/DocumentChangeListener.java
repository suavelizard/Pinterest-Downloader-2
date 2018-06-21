package nl.juraji.pinterestdownloader.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created by Juraji on 21-6-2018.
 * Pinterest Downloader
 */
@FunctionalInterface
public interface DocumentChangeListener extends DocumentListener {

    @Override
    default void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }
}
