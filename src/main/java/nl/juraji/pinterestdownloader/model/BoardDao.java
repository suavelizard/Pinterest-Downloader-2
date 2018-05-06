package nl.juraji.pinterestdownloader.model;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.enterprise.inject.Default;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
@Default
public class BoardDao extends Dao {

    public Board initPinImageHashes(Board board) {
        try (Session session = getSession()) {
            final Board merge = (Board) session.merge(board);

            merge.getPins().stream()
                    .filter(pin -> !Hibernate.isInitialized(pin.getImageHash()))
                    .forEach(pin -> Hibernate.initialize(pin.getImageHash()));

            return merge;
        }
    }
}
