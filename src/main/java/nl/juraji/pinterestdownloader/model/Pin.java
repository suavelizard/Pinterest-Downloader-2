package nl.juraji.pinterestdownloader.model;

import javax.persistence.*;
import java.io.File;
import java.util.List;
import java.util.Objects;

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
    private File fileOnDisk;

    // Should be loaded via BoardDao::initPinImageHashes or if no-session issue is fixed
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PinImageHash imageHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Board board;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> sourceUrls;

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

    public void setSourceUrls(List<String> sourceUrls) {
        this.sourceUrls = sourceUrls;
    }

    public List<String> getSourceUrls() {
        return sourceUrls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pin pin = (Pin) o;
        return Objects.equals(getId(), pin.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUrl());
    }
}
