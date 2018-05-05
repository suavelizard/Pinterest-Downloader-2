package nl.juraji.pinterestdownloader.util;

/**
 * Created by Juraji on 1-5-2018.
 * pinterestdownloader
 */
public final class FileUtils {

    public static String bytesInHumanReadable(float bytes) {
        final String[] dictionary = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        final int stepSize = 1024;

        int index;

        for (index = 0; index < dictionary.length; index++) {
            if (bytes < stepSize) {
                break;
            }

            bytes = bytes / stepSize;
        }

        return String.format("%.1f", bytes) + " " + dictionary[index];
    }
}
