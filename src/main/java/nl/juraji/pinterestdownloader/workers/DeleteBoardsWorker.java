package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItem;
import nl.juraji.pinterestdownloader.util.workers.IndicatingWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 1-5-2018.
 * Pinterest Downloader
 */
public class DeleteBoardsWorker extends IndicatingWorker<Void, Board> {

    private final List<BoardCheckboxListItem> selectedItems;

    public DeleteBoardsWorker(List<BoardCheckboxListItem> selectedItems) {
        this.selectedItems = selectedItems;
    }

    @Override
    protected Void doInBackground() {
        getIndicator().setTask(I18n.get("worker.deleteBoardsWorker.taskName"));

        for (BoardCheckboxListItem selectedItem : selectedItems) {
            Board board = selectedItem.getBoard();

            try {
                for (Pin pin : board.getPins()) {
                    if (pin.getFileOnDisk() != null) {
                        Files.deleteIfExists(pin.getFileOnDisk().toPath());
                    }
                }

                publish(board);
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed deleting files for board", e);
            }
        }

        return null;
    }
}
