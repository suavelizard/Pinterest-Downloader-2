package nl.juraji.pinterestdownloader.resources;

import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * Created by Juraji on 22-4-2018.
 * Pinterest Downloader
 */
public final class I18n {
    private static final String BUNDLE_NAME = "data.i18n";

    private I18n() {
    }

    public static String get(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        String string = ResourceBundle.getBundle(BUNDLE_NAME).getString(key);

        if (params != null && params.length > 0) {
            for (Object param : params) {
                if(param instanceof String) {
                    string = string.replaceFirst("\\{}", (String) param);
                } else {
                    string = string.replaceFirst("\\{}", String.valueOf(param));
                }
            }
        }

        return string;
    }
}
