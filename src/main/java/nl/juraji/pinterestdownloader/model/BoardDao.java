package nl.juraji.pinterestdownloader.model;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.enterprise.inject.Default;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
@Default
public class BoardDao extends Dao {

    public List<Board> getPinterestBoards() {
        try (Session session = getSession()) {
            final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<Board> query = criteriaBuilder.createQuery(Board.class);
            final Root<Board> root = query.from(Board.class);
            query.where(criteriaBuilder.equal(root.get("localFolder"), false));

            return session.createQuery(query).getResultList();
        }
    }

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
