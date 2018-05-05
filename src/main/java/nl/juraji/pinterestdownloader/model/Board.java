package nl.juraji.pinterestdownloader.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juraji on 22-4-2018.
 * Pinterest Downloader
 */
@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(length = 1024)
    private String url;

    // Fetched eagerly, since a board is never used without its pins
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Pin> pins;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Pin> getPins() {
        if (pins == null) {
            pins = new ArrayList<>();
        }

        return pins;
    }
}
