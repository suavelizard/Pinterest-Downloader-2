package nl.juraji.pinterestdownloader.model;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.enterprise.inject.Default;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
@Default
public class BoardDao extends Dao {

    public List<Pin> getAllPins() {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Pin> query = criteriaBuilder.createQuery(Pin.class);
            query.select(query.from(Pin.class));

            return session.createQuery(query).getResultList();
        }
    }

    public void initPinImageHashes(List<Pin> pins) {
        try (Session session = getSession()) {
            pins.stream()
                    .filter(pin -> !Hibernate.isInitialized(pin.getImageHash()))
                    .map(session::merge)
                    .forEach(pin -> Hibernate.initialize(((Pin) pin).getImageHash()));
        }
    }
}
