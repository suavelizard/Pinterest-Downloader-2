package nl.juraji.pinterestdownloader.model;

import javax.persistence.*;

/**
 * Created by Juraji on 27-4-2018.
 * Pinterest Downloader
 */
@Entity
public class PinImageHash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private long qualityRating;

    @Column(length = 10000)
    private String hash;

    @Column
    private Contrast contrast;

    @Column
    private int imageWidth;

    @Column
    private int imageHeight;

    @Column
    private long imageSizeBytes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getQualityRating() {
        return qualityRating;
    }

    public void setQualityRating(long qualityRating) {
        this.qualityRating = qualityRating;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Contrast getContrast() {
        return contrast;
    }

    public void setContrast(Contrast contrast) {
        this.contrast = contrast;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageSizeBytes(long imageSizeBytes) {
        this.imageSizeBytes = imageSizeBytes;
    }

    public long getImageSizeBytes() {
        return imageSizeBytes;
    }
}
