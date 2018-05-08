package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.util.hashes.PinHashComparator;
import nl.juraji.pinterestdownloader.util.workers.IndicatingWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicateScanWorker extends IndicatingWorker<Void, DuplicatePinSet> {

    private final Board board;

    public DuplicateScanWorker(Board board) {
        super();
        this.board = board;
    }

    @Override
    protected Void doInBackground() {
        getIndicator().setTask(I18n.get("worker.duplicateScanWorker.taskName", board.getName()));

        final List<Pin> pins = board.getPins();
        getIndicator().setAction(I18n.get("worker.duplicateScanWorker.scanning", pins.size()));
        getIndicator().setProgressBarMax(pins.size());
        final PinHashComparator comparator = new PinHashComparator();
        final ArrayList<Pin> compareQueue = new ArrayList<>(pins);

        try {
            pins.stream()
                    .peek(ign -> getIndicator().incrementProgressBar())
                    .forEach(parentPin -> {
                        if (isCancelled()) {
                            throw new CanceledException();
                        }
                        List<Pin> collect = compareQueue.stream()
                                .filter(p -> !parentPin.equals(p))
                                .filter(p -> comparator.compare(parentPin, p))
                                .collect(Collectors.toList());

                        if (collect.size() > 0) {
                            compareQueue.remove(parentPin);
                            collect.forEach(compareQueue::remove);
                            publish(new DuplicatePinSet(board.getName(), parentPin, collect));
                        }
                    });
        } catch (CanceledException ignored) {
        }

        return null;
    }
}
