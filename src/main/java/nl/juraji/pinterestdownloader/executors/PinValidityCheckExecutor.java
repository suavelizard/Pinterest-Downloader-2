package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class PinValidityCheckExecutor extends TaskExecutor<List<Pin>> {
    private final List<Pin> pins;

    public PinValidityCheckExecutor(Task task, List<Pin> pins) {
        super(task);
        this.pins = pins;
    }

    @Override
    protected List<Pin> execute() {
        getTask().setTask(I18n.get("worker.dbPinValidityCheckWorker.validatingLocalFiles"));
        getTask().setProgressMax(pins.size());

        return pins.stream()
                .peek(pin -> getTask().incrementProgress())
                .filter(pin -> pin.getFileOnDisk() != null && !pin.getFileOnDisk().exists())
                .peek(pin -> pin.setFileOnDisk(null))
                .peek(pin -> pin.setImageHash(null))
                .collect(Collectors.toList());
    }
}
