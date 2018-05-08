package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.util.workers.IndicatingWorker;

import java.util.List;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public class DbPinValidityCheckWorker extends IndicatingWorker<Void, Pin> {

    private final List<Pin> pins;

    public DbPinValidityCheckWorker(List<Pin> pins) {
        this.pins = pins;
    }

    @Override
    protected Void doInBackground() {
        getIndicator().setTask(I18n.get("worker.dbPinValidityCheckWorker.taskName"));
        getIndicator().setProgressBarMax(pins.size());
        getIndicator().setAction(I18n.get("worker.dbPinValidityCheckWorker.validatingLocalFiles"));
        getIndicator().setVisible(true);

        pins.stream()
                .peek(pin -> getIndicator().incrementProgressBar())
                .filter(pin -> pin.getFileOnDisk() != null && !pin.getFileOnDisk().exists())
                .peek(pin -> pin.setFileOnDisk(null))
                .peek(pin -> pin.setImageHash(null))
                .forEach(this::publish);

        return null;
    }
}
