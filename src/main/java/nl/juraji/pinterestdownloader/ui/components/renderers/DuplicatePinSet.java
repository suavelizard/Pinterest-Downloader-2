package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicatePinSet {
    private static final int PIN_ID_TRIM_SIZE = 17;

    private final List<Pin> pins;
    private final String displayName;

    public DuplicatePinSet(String boardName, Pin parentPin, List<Pin> pins) {
        this.pins = Stream.concat(Stream.of(parentPin), pins.stream())
                .sorted(Comparator.comparingLong(p -> ((Pin) p).getImageHash().getQualityRating()).reversed())
                .collect(Collectors.toList());

        String parentPinDisplayId = this.formatPinId(parentPin.getPinId());
        this.displayName = I18n.get("ui.duplicateScanner.duplicatePinSet.displayName",
                boardName,
                parentPinDisplayId,
                pins.size());
        ;
    }

    public List<Pin> getPins() {
        return pins;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    private String formatPinId(String id) {
        if (id.length() > PIN_ID_TRIM_SIZE + 1) {
            return id.substring(0, PIN_ID_TRIM_SIZE);
        } else {
            return id;
        }
    }
}
