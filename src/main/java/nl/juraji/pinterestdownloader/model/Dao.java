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
@SuppressWarnings({"CdiManagedBeanInconsistencyInspection", "unchecked"})
public abstract class Dao<T> {

    private final Class entityCls;

    @Inject
    private EntityManagerFactory emf;

    protected Dao(Class entityCls) {
        this.entityCls = entityCls;
    }

    protected Session getSession() {
        return emf.createEntityManager()
                .unwrap(Session.class)
                .getSession();
    }

    public List<T> get() {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery query = criteriaBuilder.createQuery(entityCls);
            query.select(query.from(entityCls));

            return session.createQuery(query).getResultList();
        }

    }

    public T get(long id) {
        try (Session session = getSession()) {
            return (T) session.get(entityCls, id);
        }
    }

    public void save(T entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            Object mergedEntity = session.merge(entity);
            session.saveOrUpdate(mergedEntity);
            session.getTransaction().commit();
        }
    }

    public void save(Collection<T> entities) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            entities.stream()
                    .map(session::merge)
                    .forEach(session::saveOrUpdate);
            session.getTransaction().commit();
        }
    }

    public void delete(T entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            Object mergedEntity = session.merge(entity);
            session.delete(mergedEntity);
            session.getTransaction().commit();
        }
    }
}
