package nl.juraji.pinterestdownloader.model;

import com.google.common.base.Strings;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 * <p>
 * Since there's always gonna be no more than one Settings object
 * this class will hold a reference to this entity and delegate it's methods.
 * This makes accessing and manipulating the settings easier
 */
@Default
@Singleton
public class SettingsDao extends Dao {
    private static final long SETTINGS_DEFAULT_ID = 1;
    private Settings settings;
    private List<Runnable> stateOKRunnables;

    public SettingsDao() {
        stateOKRunnables = new ArrayList<>();
    }

    @PostConstruct
    private void init() {
        settings = super.get(Settings.class, SETTINGS_DEFAULT_ID);

        if (settings == null) {
            settings = new Settings();
            settings.setId(SETTINGS_DEFAULT_ID);
            super.save(settings);
        }
    }

    @Override
    public <T> List<T> get(Class<T> entityClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(Class<T> entityClass, long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(Collection<?> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Object entity) {
        throw new UnsupportedOperationException();
    }

    public String getPinterestUsername() {
        return settings.getPinterestUsername();
    }

    public void setPinterestUsername(String pinterestUsername) {
        settings.setPinterestUsername(pinterestUsername);
    }

    public String getPinterestPassword() {
        return settings.getPinterestPassword();
    }

    public void setPinterestPassword(String pinterestPassword) {
        settings.setPinterestPassword(pinterestPassword);
    }

    public File getImageStore() {
        return settings.getImageStore();
    }

    public void setImageStore(File imageStore) {
        settings.setImageStore(imageStore);
    }

    public boolean validate() {
        return getImageStore() != null
                && !Strings.isNullOrEmpty(getPinterestUsername())
                && !Strings.isNullOrEmpty(getPinterestPassword());
    }

    public void save() {
        super.save(settings);
        if (validate()) {
            stateOKRunnables.forEach(Runnable::run);
        }
    }

    public void onStateOK(Runnable runnable) {
        if (validate()) {
            runnable.run();
        }
        this.stateOKRunnables.add(runnable);
    }
}
