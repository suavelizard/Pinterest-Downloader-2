package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.workers.WorkerWithTask;

import java.util.List;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public class DbPinValidityCheckWorker extends WorkerWithTask<Void, Pin> {

    private final List<Pin> pins;

    public DbPinValidityCheckWorker(Task task, List<Pin> pins) {
        super(task);
        this.pins = pins;
    }

    @Override
    protected Void doInBackground() {
        getTask().setTask(I18n.get("worker.dbPinValidityCheckWorker.validatingLocalFiles"));
        getTask().setProgressMax(pins.size());

        pins.stream()
                .peek(pin -> getTask().incrementProgress())
                .filter(pin -> pin.getFileOnDisk() != null && !pin.getFileOnDisk().exists())
                .peek(pin -> pin.setFileOnDisk(null))
                .peek(pin -> pin.setImageHash(null))
                .forEach(this::publish);

        return null;
    }
}
