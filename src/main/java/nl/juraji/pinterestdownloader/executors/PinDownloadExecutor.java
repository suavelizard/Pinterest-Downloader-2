package nl.juraji.pinterestdownloader.executors;

import com.google.common.base.Strings;
import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class PinDownloadExecutor extends TaskExecutor<Void> {

    private final Board board;
    private final File targetDir;

    public PinDownloadExecutor(Task task, Board board, File imageStore) {
        super(task);
        this.board = board;
        this.targetDir = new File(imageStore, getFileSystemSafeName(board.getName()));
    }

    @Override
    protected Void execute() throws Exception {
        if (!board.isLocalFolder()) {
            if (!targetDir.exists()) {
                Files.createDirectories(targetDir.toPath());
            }

            final List<Pin> pins = board.getPins();

            List<Pin> pinsToDownload = pins.stream()
                    .filter(pin -> pin.getOriginalUrl() != null && pin.getFileOnDisk() == null)
                    .collect(Collectors.toList());

            getTask().setTask(I18n.get("worker.pinsDownloadWorker.downloadingPins", pinsToDownload.size()));
            getTask().setProgressMax(pinsToDownload.size());

            pinsToDownload.stream()
                    .parallel()
                    .peek(pin -> getTask().incrementProgress())
                    .forEach(this::downloadPin);
        }

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
            final File download = doDownload(reportedUrl, pinFileName);
            pin.setFileOnDisk(download);
        } catch (IOException e) {
            // Originals uri failed, try 736x uri
            logWarning("Failed downloading pin from " + reportedUrl + ", trying 736x uri...");
            String x736Uri = reportedUrl.replace("/originals/", "/736x/");
            try {
                final File download = doDownload(reportedUrl, pinFileName);
                pin.setFileOnDisk(download);
                pin.setOriginalUrl(x736Uri);
            } catch (IOException e1) {
                logWarning("Failed downloading pin from " + x736Uri + ", giving up!");
            }
        }
    }

    private File doDownload(String uri, String pinFileName) throws IOException {
        File targetFile = new File(targetDir, pinFileName);

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
