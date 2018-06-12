package nl.juraji.pinterestdownloader.util.hashes;

import nl.juraji.pinterestdownloader.model.Contrast;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.PinImageHash;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import static java.awt.RenderingHints.*;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

/**
 * Created by Juraji on 27-4-2018.
 * Pinterest Downloader
 */
public final class PinHashBuilder {
    private static final int SAMPLE_SIZE = 100;
    private static final int HASH_OUTPUT_SIZE = SAMPLE_SIZE * 99;

    public PinImageHash build(Pin pin) throws IOException {
        try {
            BufferedImage image = ImageIO.read(pin.getFileOnDisk());
            long qualityRating = calculateQualityRating(image, pin.getFileOnDisk());

            BufferedImage resizeImage = resizeAndGrayScale(image);
            image.flush();

            BitSet hashBitSet = new BitSet(HASH_OUTPUT_SIZE);
            int averageRGB = generateHash(resizeImage, hashBitSet);
            resizeImage.flush();

            PinImageHash hash = new PinImageHash();
            hash.setContrast(Contrast.forRGB(averageRGB));
            hash.setHash(hashBitSet);
            hash.setQualityRating(qualityRating);
            hash.setImageWidth(image.getWidth());
            hash.setImageHeight(image.getHeight());
            hash.setImageSizeBytes(pin.getFileOnDisk().length());

            return hash;
        } catch (Throwable e) {
            throw new IOException("Image load failed: " + pin.getFileOnDisk().getAbsolutePath(), e);
        }
    }

    private long calculateQualityRating(BufferedImage image, File originalFile) {
        return (image.getWidth() * image.getHeight()) + originalFile.length();
    }

    private BufferedImage resizeAndGrayScale(BufferedImage image) {
        BufferedImage resizeImage = new BufferedImage(SAMPLE_SIZE, SAMPLE_SIZE, TYPE_BYTE_GRAY);
        Graphics2D graphics = resizeImage.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics.drawImage(image, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE, null);
        graphics.dispose();
        return resizeImage;
    }

    private int generateHash(BufferedImage image, BitSet set) {
        // The last column is ignored due to it
        // not having a next column for comparison
        int scanXCount = image.getWidth() - 1;
        int scanYCount = image.getHeight();
        int totalXY = scanXCount * scanYCount;
        long totalRGB = 0;
        int iter = 0;

        for (int y = 0; y < scanYCount; y++) {
            for (int x = 0; x < scanXCount; x++) {
                int rgbA = image.getRGB(x, y) & 255;
                int rgbB = image.getRGB(x + 1, y) & 255;
                set.set(iter, rgbA < rgbB);
                totalRGB += rgbA;
                ++iter;
            }
        }

        return (int) (totalRGB / totalXY);
    }
}
