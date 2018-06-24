package nl.juraji.pinterestdownloader.executors;

import com.google.common.base.Strings;
import net.sf.jmimemagic.*;
import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class FileTypeCorrectionExecutor extends TaskExecutor<Void> {

    private final Board board;

    public FileTypeCorrectionExecutor(Task task, Board board) {
        super(task);
        this.board = board;
    }

    @Override
    protected Void execute() throws Exception {
        final List<Pin> pins = board.getPins();
        getTask().setTask(I18n.get("worker.pinImageTypeCheckWorker.checkingFiles"));
        getTask().setProgressMax(pins.size());

        for (Pin pin : pins) {
            getTask().incrementProgress();
            final File fileOnDisk = pin.getFileOnDisk();

            if (fileOnDisk != null) {
                final File magicMatchFile = getMagicMatchedFile(fileOnDisk);

                if (!fileOnDisk.equals(magicMatchFile)) {
                    try {
                        Files.move(fileOnDisk.toPath(), magicMatchFile.toPath());
                    } catch (FileAlreadyExistsException e) {
                        // The target file already exists, so delete the newly downloaded one and proceed
                        Files.deleteIfExists(fileOnDisk.toPath());
                    }

                    pin.setFileOnDisk(magicMatchFile);
                }
            }
        }

        return null;
    }

    private File getMagicMatchedFile(File file) {
        try {
            final String orgFilePath = file.getAbsolutePath();
            final String orgExtension = orgFilePath.substring(orgFilePath.lastIndexOf('.') + 1);
            final MagicMatch magicMatch = Magic.getMagicMatch(file, false, true);

            final String magicMatchExtension = magicMatch.getExtension();
            if (!Strings.isNullOrEmpty(magicMatchExtension)) {
                if (!magicMatchExtension.equals(orgExtension)) {
                    file = new File(orgFilePath.replace(orgExtension, magicMatchExtension));
                }
            }
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
            // If file type can't be inferred there's nothing we can do
        }

        return file;
    }
}
