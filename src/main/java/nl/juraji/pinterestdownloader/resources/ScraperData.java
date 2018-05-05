package nl.juraji.pinterestdownloader.resources;

import org.jetbrains.annotations.PropertyKey;
import org.openqa.selenium.By;

import java.util.ResourceBundle;

/**
 * Created by Juraji on 22-4-2018.
 * Pinterest Downloader
 */
public final class ScraperData {
    private static final String BUNDLE_NAME = "data.scraper-data";

    private ScraperData() {
    }

    public static By by(@PropertyKey(resourceBundle = BUNDLE_NAME) String key) {
        String type = key.substring(0, key.indexOf('.'));
        String string = get(key);
        switch (type) {
            case "xpath":
                return By.xpath(string);
            case "class":
                return By.className(string);
            default:
                throw new UnsupportedOperationException("Keys of type \"" + type + "\" are not supported");
        }
    }

    public static String get(@PropertyKey(resourceBundle = BUNDLE_NAME) String key) {
        return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
    }
}
