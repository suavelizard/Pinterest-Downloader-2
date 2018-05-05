package nl.juraji.pinterestdownloader.util.workers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.renderers.DuplicatePinSet;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.hashes.PinHashComparator;
import nl.juraji.pinterestdownloader.util.workers.workerutils.Worker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicateScanWorker extends Worker<List<DuplicatePinSet>> {
    private final List<Pin> pins;

    public DuplicateScanWorker(ProgressIndicator indicator, List<Pin> pins) {
        super(indicator);
        this.pins = pins;
    }

    @Override
    protected List<DuplicatePinSet> doInBackground() {
        getIndicator().setTask(I18n.get("worker.duplicateScanWorker.taskName"));

        getIndicator().setAction(I18n.get("worker.duplicateScanWorker.scanning", this.pins.size()));
        getIndicator().setProgressBarMax(pins.size());

        PinHashComparator comparator = new PinHashComparator();
        ArrayList<Pin> compareList = new ArrayList<>(this.pins);

        return this.pins.stream()
                .peek(ign -> getIndicator().incrementProgressBar())
                .map(parentPin -> {
                    List<Pin> collect = compareList.stream()
                            .filter(p -> !parentPin.equals(p))
                            .filter(p -> comparator.compare(parentPin, p))
                            .sorted(Comparator.comparingLong(p -> ((Pin) p).getImageHash().getQualityRating()).reversed())
                            .peek(compareList::remove)
                            .collect(Collectors.toList());

                    if (collect.size() > 0) {
                        return new DuplicatePinSet(parentPin, collect);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
