package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.DuplicatePinSetList;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.hashes.PinHashComparator;
import nl.juraji.pinterestdownloader.util.workers.PublishingWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    protected Void doInBackground() throws Exception {
        getIndicator().setTask(I18n.get("worker.duplicateScanWorker.taskName", board.getName()));

        final List<Pin> pins = board.getPins();
        getIndicator().setAction(I18n.get("worker.duplicateScanWorker.scanning", pins.size()));
        getIndicator().setProgressBarMax(pins.size());

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        final ExecutorService threadPool = Executors.newFixedThreadPool(availableProcessors);
        final List<List<Pin>> partitions = partition(pins, availableProcessors);


        final List<? extends Future<?>> futures = partitions.stream()
                .map(partition -> threadPool.submit(() -> this.scanPartition(partition, pins)))
                .collect(Collectors.toList());

        for (Future<?> future : futures) {
            future.get();
        }

        threadPool.shutdown();
        return null;
    }

    @Override
    public void process(List<DuplicatePinSet> chunks) {
        duplicatePinSetList.addSets(chunks);
    }

    private void scanPartition(List<Pin> partition, List<Pin> against) {
        final PinHashComparator comparator = new PinHashComparator();
        final ArrayList<Pin> compareQueue = new ArrayList<>(against);
        partition.stream()
                .peek(ign -> getIndicator().incrementProgressBar())
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
    }

    private List<List<Pin>> partition(List<Pin> list, int parts) {
        final int listSize = list.size();
        final List<List<Pin>> partitions = new ArrayList<>();

        if (listSize < parts) {
            partitions.add(list);
        } else {
            final int partitionSize = listSize / parts;
            int leftOver = listSize % partitionSize;
            int take;

            for (int i = 0; i < listSize; i += take) {
                if (leftOver > 0) {
                    leftOver--;
                    take = partitionSize + 1;
                } else {
                    take = partitionSize;
                }

                partitions.add(list.subList(i, Math.min(listSize, i + take)));
            }
        }

        return partitions;
    }
}
