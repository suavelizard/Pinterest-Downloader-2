package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.PinImageHash;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.hashes.PinHashBuilder;
import nl.juraji.pinterestdownloader.util.workers.WorkerWithTask;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 12-6-2018.
 * Pinterest Downloader
 */
public class PinHasherWorker extends WorkerWithTask<Void, Void> {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final List<Pin> pins;

    public PinHasherWorker(Task task, List<Pin> pins) {
        super(task);
        this.pins = pins;
    }

    @Override
    protected Void doInBackground() {
        List<Pin> pinsToHash = pins.stream()
                .filter(pin -> pin.getImageHash() == null && pin.getFileOnDisk() != null)
                .collect(Collectors.toList());

        getTask().setTask(I18n.get("worker.pinHasherWorker.buildingHashes", pinsToHash.size()));
        getTask().setProgressMax(pinsToHash.size());

        pinsToHash.stream()
                .parallel()
                .peek(pin -> getTask().incrementProgress())
                .filter(pin -> pin.getFileOnDisk() != null)
                .forEach(this::buildAndSetPinImageHash);

        return null;
    }

    private void buildAndSetPinImageHash(Pin pin) {
        try {
            PinImageHash hash = new PinHashBuilder().build(pin);
            pin.setImageHash(hash);
        } catch (IOException e) {
            // If hashing fails there's nothing we can do, but the others should proceed
            logger.log(Level.SEVERE, "Failed building hash for " + pin.getFileOnDisk().getAbsolutePath(), e);
        }
    }
}
