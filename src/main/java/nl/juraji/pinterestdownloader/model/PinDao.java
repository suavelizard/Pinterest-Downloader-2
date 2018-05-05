package nl.juraji.pinterestdownloader.model;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.enterprise.inject.Default;
import java.util.Collection;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
@Default
public class PinDao extends Dao<Pin> {

    protected PinDao() {
        super(Pin.class);
    }

    /**
     * Pin image hashes are fetch lazily, for performance reasons,
     * this method conducts initialization from within a session
     *
     * @param pins The pins of which to initialize the image hash objects
     */
    public void initPinImageHashes(Collection<Pin> pins) {
        try (Session session = getSession()) {
            pins.stream()
                    .filter(pin -> !Hibernate.isInitialized(pin.getImageHash()))
                    .map(session::merge)
                    .forEach(pin -> Hibernate.initialize(((Pin) pin).getImageHash()));
        }
    }
}
