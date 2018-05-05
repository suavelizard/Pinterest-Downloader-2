package nl.juraji.pinterestdownloader.model;

import javax.persistence.*;
import java.io.File;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
@Entity
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String pinId;

    @Column(length = 2048)
    private String description;

    @Column(length = 1024)
    private String url;

    @Column(length = 1024)
    private String originalUrl;

    @Column(length = 1024)
    private File fileOnDisk;

    // Should be loaded via PinImageHashDao or if no-session issue is fixed
    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PinImageHash imageHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Board board;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPinId() {
        return pinId;
    }

    public void setPinId(String pinId) {
        this.pinId = pinId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public File getFileOnDisk() {
        return fileOnDisk;
    }

    public void setFileOnDisk(File pathOnDisk) {
        this.fileOnDisk = pathOnDisk;
    }

    public PinImageHash getImageHash() {
        return imageHash;
    }

    public void setImageHash(PinImageHash imageHash) {
        this.imageHash = imageHash;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pin
                && ((Pin) obj).getId().equals(this.getId());
    }
}
