package nl.juraji.pinterestdownloader.model;

import javax.persistence.*;
import java.io.File;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
@Entity
public class Settings {

    @Id
    private Long id;

    @Column
    private String pinterestUsername;

    @Column
    private String pinterestPassword;

    @Column(length = 1024)
    private File imageStore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPinterestUsername() {
        return pinterestUsername;
    }

    public void setPinterestUsername(String pinterestUsername) {
        this.pinterestUsername = pinterestUsername;
    }

    public String getPinterestPassword() {
        return pinterestPassword;
    }

    public void setPinterestPassword(String pinterestPassword) {
        this.pinterestPassword = pinterestPassword;
    }

    public File getImageStore() {
        return imageStore;
    }

    public void setImageStore(File imageStore) {
        this.imageStore = imageStore;
    }
}
