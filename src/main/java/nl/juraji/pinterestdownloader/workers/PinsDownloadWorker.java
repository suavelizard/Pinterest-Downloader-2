package nl.juraji.pinterestdownloader.workers;

import com.google.common.base.Strings;
import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.workers.WorkerWithTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 27-4-2018.
 * Pinterest Downloader
 */
public class PinsDownloadWorker extends WorkerWithTask<Void, Void> {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Board board;
    private final File outputDirectory;

    public PinsDownloadWorker(Task task, Board board, File outputDirectory) {
        super(task);
        this.board = board;
        this.outputDirectory = new File(outputDirectory, getFileSystemSafeName(board.getName()));
    }

    @Override
    protected Void doInBackground() throws IOException {
        if (!outputDirectory.exists()) {
            Files.createDirectories(outputDirectory.toPath());
        }

        List<Pin> pins = board.getPins();

        List<Pin> pinsToDownload = pins.stream()
                .filter(pin -> pin.getOriginalUrl() != null && pin.getFileOnDisk() == null)
                .collect(Collectors.toList());

        getTask().setTask(I18n.get("worker.pinsDownloadWorker.downloadingPins", pinsToDownload.size()));
        getTask().setProgressMax(pinsToDownload.size());
        pinsToDownload.stream()
                .parallel()
                .peek(pin -> getTask().incrementProgress())
                .forEach(this::downloadPin);

        return null;
    }

    private void downloadPin(Pin pin) {
        String reportedUrl = pin.getOriginalUrl();
        String fileExtension = reportedUrl.substring(reportedUrl.lastIndexOf('.'));
        String pinFileName;

        if (Strings.isNullOrEmpty(pin.getDescription())) {
            pinFileName = pin.getPinId() + fileExtension;
        } else {
            pinFileName = pin.getPinId() + "_" + getFileSystemSafeName(pin.getDescription()) + fileExtension;
        }

        try {
            String originalsUrl = translatePinUrlToOriginalsUrl(reportedUrl);
            pin.setFileOnDisk(doDownload(originalsUrl, pinFileName));
            pin.setOriginalUrl(originalsUrl);
        } catch (IOException e) {
            // Originals uri failed, try 564x uri
            String xUrl = translatePinUrlTo564xUrl(reportedUrl);
            try {
                pin.setFileOnDisk(doDownload(xUrl, pinFileName));
                pin.setOriginalUrl(xUrl);
            } catch (IOException e1) {
                // Image unavailable!
                logger.log(Level.SEVERE, "Failed downloading pin from " + reportedUrl, e);
            }
        }
    }

    private String translatePinUrlTo564xUrl(String reportedUrl) {
        String pattern = "/[0-9]{3}x/";
        String replacement = "/564x/";
        return reportedUrl.replaceFirst(pattern, replacement);
    }

    private String translatePinUrlToOriginalsUrl(String originalUrl) {
        String pattern = "/[0-9]{3}x/";
        String replacement = "/originals/";
        return originalUrl.replaceFirst(pattern, replacement);
    }

    private File doDownload(String uri, String pinFileName) throws IOException {
        File targetFile = new File(outputDirectory, pinFileName);

        // Only perform download if file doesn't already exist
        if (!targetFile.exists()) {
            try (InputStream input = new URL(uri).openStream()) {
                Files.copy(input, targetFile.toPath());
                return targetFile;
            }
        }

        return targetFile;
    }

    private String getFileSystemSafeName(String name) {
        String result = name.replaceAll("[^0-9a-zA-Z-.,]", "_");
        if (result.length() > 64) result = result.substring(0, 63);
        return result.trim();
    }
}
