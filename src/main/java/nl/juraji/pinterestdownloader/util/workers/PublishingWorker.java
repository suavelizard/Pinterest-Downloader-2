package nl.juraji.pinterestdownloader.util.workers;

import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;

import javax.swing.*;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public abstract class PublishingWorker<T> extends SwingWorker<Void, T> {
    private final ProgressIndicator indicator;

    public PublishingWorker(ProgressIndicator indicator) {
        this.indicator = indicator;
        indicator.resetProgressBar();
    }

    @Override
    protected void done() {
        if (indicator.isVisible()) {
            indicator.setVisible(false);
        }
    }

    protected ProgressIndicator getIndicator() {
        if (!indicator.isVisible()) {
            indicator.setVisible(true);
        }
        return indicator;
    }
}
