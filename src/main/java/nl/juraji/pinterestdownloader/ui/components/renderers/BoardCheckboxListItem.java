package nl.juraji.pinterestdownloader.ui.components.renderers;

import nl.juraji.pinterestdownloader.model.Board;

import java.util.Objects;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
public class BoardCheckboxListItem {
    private final Board board;
    private boolean selected = false;
    private int availablePinCount;
    private long downloadedPinCount;

    public BoardCheckboxListItem(Board board) {
        Objects.requireNonNull(board);
        this.board = board;

        updatePinCounts();

        // Set selected if pins available
        if (availablePinCount > 0) {
            selected = true;
        }
    }

    public Board getBoard() {
        return board;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getAvailablePinCount() {
        return availablePinCount;
    }

    public long getDownloadedPinCount() {
        return downloadedPinCount;
    }

    public void updatePinCounts() {
        this.availablePinCount = board.getPins().size();
        if (this.availablePinCount > 0) {
            this.downloadedPinCount = board.getPins().stream()
                    .filter(pin -> pin.getFileOnDisk() != null)
                    .count();
        }
    }
}
