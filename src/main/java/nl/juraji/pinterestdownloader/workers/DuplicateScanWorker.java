package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.DuplicatePinSetList;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.hashes.PinHashComparator;
import nl.juraji.pinterestdownloader.util.workers.PublishingWorker;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicateScanWorker extends PublishingWorker<DuplicatePinSet> {

    private final Board board;
    private final DuplicatePinSetList duplicatePinSetList;

    public DuplicateScanWorker(ProgressIndicator indicator, Board board, DuplicatePinSetList duplicatePinSetList) {
        super(indicator);
        this.board = board;
        this.duplicatePinSetList = duplicatePinSetList;
    }

    @Override
    protected Void doInBackground() {
        getIndicator().setTask(I18n.get("worker.duplicateScanWorker.taskName", board.getName()));

        final List<Pin> pins = board.getPins();
        getIndicator().setAction(I18n.get("worker.duplicateScanWorker.scanning", pins.size()));
        getIndicator().setProgressBarMax(pins.size());

        PinHashComparator comparator = new PinHashComparator();
        final ConcurrentLinkedDeque<Pin> compareQueue = new ConcurrentLinkedDeque<>(pins);

        pins.stream()
                .peek(ign -> getIndicator().incrementProgressBar())
                .parallel()
                .forEach(parentPin -> {
                    List<Pin> collect = compareQueue.stream()
                            .filter(p -> !parentPin.equals(p))
                            .filter(p -> comparator.compare(parentPin, p))
                            .peek(compareQueue::remove)
                            .collect(Collectors.toList());

                    if (collect.size() > 0) {
                        compareQueue.remove(parentPin);
                        publish(new DuplicatePinSet(board.getName(), parentPin, collect));
                    }
                });

        return null;
    }

    @Override
    public void process(List<DuplicatePinSet> chunks) {
        duplicatePinSetList.addSets(chunks);
    }
}
