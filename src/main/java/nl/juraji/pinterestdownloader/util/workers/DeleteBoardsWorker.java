package nl.juraji.pinterestdownloader.util.workers;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItem;
import nl.juraji.pinterestdownloader.util.workers.workerutils.Worker;

import java.nio.file.Files;
import java.util.List;

/**
 * Created by Juraji on 1-5-2018.
 * Pinterest Downloader
 */
public class DeleteBoardsWorker extends Worker<Void> {

    private final List<BoardCheckboxListItem> selectedItems;

    public DeleteBoardsWorker(ProgressIndicator indicator, List<BoardCheckboxListItem> selectedItems) {
        super(indicator);
        this.selectedItems = selectedItems;
    }

    @Override
    protected Void doInBackground() throws Exception {
        getIndicator().setTask(I18n.get("worker.deleteBoardsWorker.taskName"));

        for (BoardCheckboxListItem selectedItem : selectedItems) {
            Board board = selectedItem.getBoard();

            for (Pin pin : board.getPins()) {
                if (pin.getFileOnDisk() != null) {
                    Files.deleteIfExists(pin.getFileOnDisk().toPath());
                }
            }
        }

        return null;
    }
}
