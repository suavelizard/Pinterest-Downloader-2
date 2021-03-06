package nl.juraji.pinterestdownloader.util;

/**
 * Created by Juraji on 6-5-2018.
 * Pinterest Downloader
 */
public class TextUtils {

    public static String trim(String src, int length) {
        return trim(src, length, false);
    }

    public static String trim(String src, int length, boolean ellipsis) {
        if (src.length() > length) {
            int lastIndex = (ellipsis ? length - 3 : length) - 1;
            src = src.substring(0, lastIndex);

            if (ellipsis) {
                src += "\u2026";
            }
        }

        return src;
    }
}
