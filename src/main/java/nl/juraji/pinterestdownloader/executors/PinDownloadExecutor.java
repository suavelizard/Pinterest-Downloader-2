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
                    .filter(pin -> pin.getSourceUrls() != null && pin.getFileOnDisk() == null)
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
        final List<String> imgUrls = pin.getSourceUrls();
        boolean failed = true;

        for (String imgUrl : imgUrls) {
            final String fileName = createTargetFileName(pin, imgUrl);

            try {
                final File download = doDownload(imgUrl, fileName);
                pin.setFileOnDisk(download);
                failed = false;
                break;
            } catch (IOException ignored) {
                logWarning("Failed downloading pin from " + imgUrl + ", trying next uri...");
            }
        }

        if (failed) {
            logWarning("Failed downloading pin " + pin.getPinId() + ", giving up!");
        }
    }

    private String createTargetFileName(Pin pin, String imgUrl) {
        String ext = imgUrl.substring(imgUrl.lastIndexOf("."));
        String fileName;

        if (Strings.isNullOrEmpty(pin.getDescription())) {
            return pin.getPinId() + ext;
        } else {
            return pin.getPinId() + " - " + getFileSystemSafeName(pin.getDescription()) + ext;
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
