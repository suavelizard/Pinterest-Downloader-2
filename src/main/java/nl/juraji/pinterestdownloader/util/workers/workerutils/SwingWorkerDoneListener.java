package nl.juraji.pinterestdownloader.util.workers.workerutils;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public abstract class SwingWorkerDoneListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (SwingWorker.StateValue.DONE.equals(e.getNewValue())) {
            this.onWorkerDone();
        }
    }

    protected abstract void onWorkerDone();
}
