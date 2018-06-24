package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItem;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItemRenderer;
import nl.juraji.pinterestdownloader.util.ArrayListModel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
public class BoardsCheckboxList extends JList<BoardCheckboxListItem> {
    private final AtomicReference<Consumer<Board>> onBoardUpdate = new AtomicReference<>();

    public BoardsCheckboxList() {
        setCellRenderer(new BoardCheckboxListItemRenderer());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setModel(new ArrayListModel<>());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index >= 0) {
                    BoardCheckboxListItem item = getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());
                    repaint(getCellBounds(index, index));
                }
            }
        });
    }

    public void updateBoards(List<Board> boards) {
        updateBoards(boards, false);
    }

    public void updateBoards(List<Board> boards, boolean append) {
        ArrayListModel<BoardCheckboxListItem> model = (ArrayListModel<BoardCheckboxListItem>) getModel();

        if (append) {
            boards = new ArrayList<>(boards);
            model.stream()
                    .map(BoardCheckboxListItem::getBoard)
                    .forEach(boards::add);
        } else {
            model.clear();
        }

        boards.stream()
                .sorted(Comparator.comparing(Board::getName))
                .map(BoardCheckboxListItem::new)
                .forEach(model::add);
    }

    public void setOnBoardUpdate(Consumer<Board> onBoardUpdate) {
        this.onBoardUpdate.set(onBoardUpdate);
    }

    public void updateSelectionForAll(boolean doSelect) {
        updateSelectionFor(i -> doSelect);
    }

    public void updateSelectionFor(Predicate<BoardCheckboxListItem> predicate) {
        ((ArrayListModel<BoardCheckboxListItem>) getModel())
                .forEach(item -> item.setSelected(predicate.test(item)));

        repaint();
    }

    public List<BoardCheckboxListItem> getSelectedItems() {
        return ((ArrayListModel<BoardCheckboxListItem>) getModel()).stream()
                .filter(BoardCheckboxListItem::isSelected)
                .collect(Collectors.toList());
    }
}
