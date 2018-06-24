package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class LocalFolderScanExecutor extends TaskExecutor<List<Pin>> {

    private final Board board;

    public LocalFolderScanExecutor(Task task, Board board) {
        super(task);
        this.board = board;
    }

    @Override
    public List<Pin> execute() {
        getTask().setTask(I18n.get("worker.localFolderScanWorker.taskName"));

        final File boardRoot = new File(board.getUrl());
        final List<Pin> existingPins = board.getPins();
        final List<File> files = FileUtils.listFiles(boardRoot, true, new String[]{"jpg", "gif", "png", "bmp"});

        return files.stream()
                .filter(file -> existingPins.stream()
                        .map(Pin::getFileOnDisk)
                        .noneMatch(f -> f.getName().equals(file.getName())))
                .map(this::mapFileToPin)
                .collect(Collectors.toList());
    }

    private Pin mapFileToPin(File file) {
        String pinUUID = UUID.randomUUID().toString();
        Pin pin = new Pin();
        pin.setFileOnDisk(file);
        pin.setBoard(board);
        pin.setDescription(file.getName());
        pin.setPinId("Local-" + pinUUID);

        return pin;
    }
}
