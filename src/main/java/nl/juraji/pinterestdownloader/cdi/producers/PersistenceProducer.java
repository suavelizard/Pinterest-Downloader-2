package nl.juraji.pinterestdownloader.cdi.producers;

import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
@Default
@Singleton
public class PersistenceProducer {
    private static final String DATA_STORE_DIRECTORY = "./store.mv.db";

    private EntityManagerFactory factory;

    @PostConstruct
    private void init() {
        Map<String, Object> config = new HashMap<>();
        config.put("hibernate.hbm2ddl.auto", isDatabaseFilePresent() ? "update" : "create");

        HibernatePersistenceProvider provider = new HibernatePersistenceProvider();
        factory = provider.createEntityManagerFactory("PinterestDownloader-H2-DS", config);
    }

    @PreDestroy
    private void destroy() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    @Produces
    private EntityManagerFactory getEntityManagerFactory() {
        return factory;
    }

    private boolean isDatabaseFilePresent() {
        return new File(DATA_STORE_DIRECTORY).exists();
    }
}
