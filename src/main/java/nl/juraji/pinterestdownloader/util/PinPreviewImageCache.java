package nl.juraji.pinterestdownloader.util;

import nl.juraji.pinterestdownloader.model.Pin;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.awt.RenderingHints.*;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * Created by Juraji on 6-5-2018.
 * Pinterest Downloader
 */
public class PinPreviewImageCache {

    private static final int MAX_PREVIEW_SIZE = 100;
    private static final AtomicReference<PinPreviewImageCache> CACHE = new AtomicReference<>();

    private final Map<Long, ImageIcon> previewCache = new HashMap<>();

    public static ImageIcon getPreview(Pin pin) {
        PinPreviewImageCache cache = CACHE.get();

        if (cache == null) {
            cache = CACHE.updateAndGet(n -> new PinPreviewImageCache());
        }

        return cache.generatePreview(pin);
    }

    public static void destroy() {
        CACHE.set(null);
    }

    private ImageIcon generatePreview(Pin pin) {
        if (previewCache.containsKey(pin.getId())) {
            if (pin.getFileOnDisk() == null) {
                previewCache.put(pin.getId(), null);
            }

            return previewCache.get(pin.getId());
        } else {
            try {
                if (pin.getFileOnDisk() != null) {
                    BufferedImage image = ImageIO.read(pin.getFileOnDisk());
                    double imgWidth = image.getWidth();
                    double imgHeight = image.getHeight();
                    double ratio = Math.min((MAX_PREVIEW_SIZE / imgWidth), (MAX_PREVIEW_SIZE / imgHeight));

                    int tgtWidth = (int) (imgWidth * ratio);
                    int tgtHeight = (int) (imgHeight * ratio);
                    int xOffset = (MAX_PREVIEW_SIZE - tgtWidth) / 2;
                    int yOffset = (MAX_PREVIEW_SIZE - tgtHeight) / 2;

                    BufferedImage resizeImage = new BufferedImage(MAX_PREVIEW_SIZE, MAX_PREVIEW_SIZE, TYPE_INT_RGB);
                    Graphics2D graphics = resizeImage.createGraphics();
                    graphics.setComposite(AlphaComposite.Src);
                    graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
                    graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
                    graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                    graphics.drawImage(image, xOffset, yOffset, tgtWidth, tgtHeight, null);
                    graphics.dispose();

                    image.flush();

                    final ImageIcon imageIcon = new ImageIcon(resizeImage);
                    previewCache.put(pin.getId(), imageIcon);
                    return imageIcon;
                }

                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
