package nl.juraji.pinterestdownloader.util.workers;

import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;

import javax.swing.*;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public abstract class IndicatingWorker<T, V> extends SwingWorker<T, V> {
    private final ProgressIndicator indicator;

    public IndicatingWorker() {
        this.indicator = new ProgressIndicator();
        indicator.resetProgressBar();

    }

    @Override
    protected void done() {
        indicator.dispose();
    }

    protected ProgressIndicator getIndicator() {
        if (!indicator.isVisible()) {
            indicator.setVisible(true);
        }
        return indicator;
    }

    public static class CanceledException extends RuntimeException{
    }
}
