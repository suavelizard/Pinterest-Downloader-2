package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;

import java.util.List;

/**
 * Created by Juraji on 2-5-2018.
 * pinterestdownloader
 */
public class DuplicatePinSet {
    private static final int PIN_ID_TRIM_SIZE = 17;

    private final Pin parentPin;
    private final List<Pin> duplicates;
    private final String displayName;

    public DuplicatePinSet(Pin parentPin, List<Pin> duplicates) {
        this.parentPin = parentPin;
        this.duplicates = duplicates;

        final String pinId = parentPin.getPinId();
        String pinDisplayId = (pinId.length() > PIN_ID_TRIM_SIZE? pinId.substring(0, PIN_ID_TRIM_SIZE): pinId);
        this.displayName = I18n.get("ui.duplicateScanner.duplicatePinSet.displayName",
                pinDisplayId,
                duplicates.size());
    }

    public Pin getParentPin() {
        return parentPin;
    }

    public List<Pin> getDuplicates() {
        return duplicates;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
