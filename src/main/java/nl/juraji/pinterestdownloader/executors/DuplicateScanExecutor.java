package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.hashes.PinHashComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class DuplicateScanExecutor extends TaskExecutor<List<DuplicatePinSet>> {

    private final Board board;

    public DuplicateScanExecutor(Task task, Board board) {
        super(task);
        this.board = board;
    }

    @Override
    protected List<DuplicatePinSet> execute() {
        final List<Pin> pins = board.getPins();
        final PinHashComparator comparator = new PinHashComparator();
        final ArrayList<Pin> compareQueue = new ArrayList<>(pins);

        getTask().setTask(I18n.get("worker.duplicateScanWorker.scanning", pins.size()));
        getTask().setProgressMax(pins.size());

        return pins.stream()
                .peek(ign -> getTask().incrementProgress())
                .map(parentPin -> {
                    List<Pin> collect = compareQueue.stream()
                            .filter(p -> !parentPin.equals(p))
                            .filter(p -> comparator.compare(parentPin, p))
                            .collect(Collectors.toList());

                    if (collect.size() > 0) {
                        compareQueue.remove(parentPin);
                        collect.forEach(compareQueue::remove);
                        return new DuplicatePinSet(board.getName(), parentPin, collect);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
