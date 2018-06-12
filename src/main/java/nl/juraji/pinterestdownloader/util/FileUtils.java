package nl.juraji.pinterestdownloader.util;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public static List<File> listFiles(File root, boolean recursive, String[] extensionFilter) {
        FileNameExtensionFilter filter = null;
        if (extensionFilter != null) {
            filter = new FileNameExtensionFilter("Extension Filter", extensionFilter);
        }

        return listFiles(root, recursive, filter);
    }

    public static List<File> listFiles(File root, boolean recursive, FileNameExtensionFilter extensionFilter) {
        if (root == null) {
            throw new IllegalArgumentException("root may not be null!");
        }
        if (!root.exists()) {
            throw new IllegalArgumentException("root does not exist!");
        }

        final List<File> files = new ArrayList<>();
        final File[] listFiles = root.listFiles();

        if (listFiles != null) {
            for (File listFile : listFiles) {
                if (listFile.isDirectory() && recursive) {
                    files.addAll(listFiles(listFile, true, extensionFilter));
                } else {
                    if (extensionFilter != null && extensionFilter.accept(listFile)) {
                        files.add(listFile);
                    }
                }
            }
        }

        return files;
    }
}
