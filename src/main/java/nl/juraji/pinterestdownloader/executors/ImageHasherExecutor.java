package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.PinImageHash;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.hashes.PinHashBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class ImageHasherExecutor extends TaskExecutor<Void> {

    private final Board board;

    public ImageHasherExecutor(Task task, Board board) {
        super(task);
        this.board = board;
    }

    @Override
    protected Void execute() {
        final List<Pin> pins = board.getPins().stream()
                .filter(pin -> pin.getImageHash() == null && pin.getFileOnDisk() != null)
                .collect(Collectors.toList());

        getTask().setTask(I18n.get("worker.pinHasherWorker.buildingHashes", pins.size()));
        getTask().setProgressMax(pins.size());

        pins.stream()
                .parallel()
                .peek(pin -> getTask().incrementProgress())
                .forEach(this::buildAndSetPinImageHash);

        return null;
    }

    private void buildAndSetPinImageHash(Pin pin) {
        try {
            PinImageHash hash = new PinHashBuilder().build(pin);
            pin.setImageHash(hash);
        } catch (IOException e) {
            // If hashing fails there's nothing we can do, but the others should proceed
            logError("Failed building hash for " + pin.getFileOnDisk().getAbsolutePath(), e);
        }
    }
}
