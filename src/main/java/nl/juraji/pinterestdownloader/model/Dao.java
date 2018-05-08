package nl.juraji.pinterestdownloader.model;

import org.hibernate.Session;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collection;
import java.util.List;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
public abstract class Dao {

    @Inject
    private EntityManagerFactory emf;

    protected Session getSession() {
        return emf.createEntityManager()
                .unwrap(Session.class)
                .getSession();
    }

    public <T> List<T> get(Class<T> entityClass) {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);
            query.select(query.from(entityClass));

            return session.createQuery(query).getResultList();
        }
    }

    public <T> T get(Class<T> entityClass, long id) {
        try (Session session = getSession()) {
            return session.get(entityClass, id);
        }
    }

    public void save(Object entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            Object mergedEntity = session.merge(entity);
            session.saveOrUpdate(mergedEntity);
            session.getTransaction().commit();
        }
    }

    public void save(Collection<?> entities) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            entities.stream()
                    .map(session::merge)
                    .forEach(session::saveOrUpdate);
            session.getTransaction().commit();
        }
    }

    public void delete(Object entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            Object mergedEntity = session.merge(entity);
            session.delete(mergedEntity);
            session.flush();
            session.getTransaction().commit();
        }
    }
}
