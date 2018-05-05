package nl.juraji.pinterestdownloader.util.workers;

import com.google.common.base.Strings;
import net.sf.jmimemagic.*;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.workers.workerutils.Worker;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public class PinImageTypeCheckWorker extends Worker<Void> {

    private final List<Pin> pins;

    public PinImageTypeCheckWorker(ProgressIndicator indicator, List<Pin> pins) {
        super(indicator);
        this.pins = pins;
    }

    @Override
    protected Void doInBackground() throws Exception {
        getIndicator().setTask(I18n.get("worker.pinImageTypeCheckWorker.taskName"));
        getIndicator().setAction(I18n.get("worker.pinImageTypeCheckWorker.checkingFiles"));
        getIndicator().setProgressBarMax(pins.size());
        getIndicator().setVisible(true);

        for (Pin pin : pins) {
            getIndicator().incrementProgressBar();
            File fileOnDisk = pin.getFileOnDisk();

            if (fileOnDisk != null) {
                File magicMatchFile = getMagicMatchedFile(fileOnDisk);

                if (!fileOnDisk.equals(magicMatchFile)) {
                    Files.move(fileOnDisk.toPath(), magicMatchFile.toPath());
                    pin.setFileOnDisk(magicMatchFile);
                }
            }
        }

        return null;
    }

    private File getMagicMatchedFile(File file) {
        try {
            String orgFilePath = file.getAbsolutePath();
            String orgExtension = orgFilePath.substring(orgFilePath.lastIndexOf('.') + 1);
            MagicMatch magicMatch = Magic.getMagicMatch(file, false, true);

            String magicMatchExtension = magicMatch.getExtension();
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
