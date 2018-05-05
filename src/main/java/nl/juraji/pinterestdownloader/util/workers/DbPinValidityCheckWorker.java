package nl.juraji.pinterestdownloader.util.workers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.workers.workerutils.Worker;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public class DbPinValidityCheckWorker extends Worker<List<Pin>> {

    private final List<Pin> pins;

    public DbPinValidityCheckWorker(ProgressIndicator indicator, List<Pin> pins) {
        super(indicator);
        this.pins = pins;
    }

    @Override
    protected List<Pin> doInBackground() {
        getIndicator().setTask(I18n.get("worker.dbPinValidityCheckWorker.taskName"));
        getIndicator().setProgressBarMax(pins.size());
        getIndicator().setAction(I18n.get("worker.dbPinValidityCheckWorker.validatingLocalFiles"));
        getIndicator().setVisible(true);

        return pins.stream()
                .peek(pin -> getIndicator().incrementProgressBar())
                .filter(pin -> pin.getFileOnDisk() != null && !pin.getFileOnDisk().exists())
                .peek(pin -> pin.setFileOnDisk(null))
                .collect(Collectors.toList());
    }
}
