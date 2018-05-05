package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItem;
import nl.juraji.pinterestdownloader.ui.components.renderers.BoardCheckboxListItemRenderer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
public class BoardsCheckboxList extends JList<BoardCheckboxListItem> {
    private AtomicReference<Consumer<Board>> onBoardUpdate = new AtomicReference<>();

    public BoardsCheckboxList() {
        setCellRenderer(new BoardCheckboxListItemRenderer());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setModel(new DefaultListModel<>());

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
        DefaultListModel<BoardCheckboxListItem> model = (DefaultListModel<BoardCheckboxListItem>) getModel();

        if (!append) {
            model.clear();
        }

        boards.stream()
                .sorted(Comparator.comparing(Board::getName))
                .map(BoardCheckboxListItem::new)
                .forEach(model::addElement);

        repaint();
    }

    public void setOnBoardUpdate(Consumer<Board> onBoardUpdate) {
        this.onBoardUpdate.set(onBoardUpdate);
    }

    public void updateSelectionForAll(boolean doSelect) {
        updateSelectionFor(i -> doSelect);
    }

    public void updateSelectionFor(Predicate<BoardCheckboxListItem> predicate) {
        ListModel<BoardCheckboxListItem> model = getModel();

        for (int i = 0; i < model.getSize(); i++) {
            BoardCheckboxListItem item = model.getElementAt(i);
            item.setSelected(predicate.test(item));
        }

        repaint();
    }

    public List<BoardCheckboxListItem> getSelectedItems() {
        List<BoardCheckboxListItem> result = new ArrayList<>();
        ListModel<BoardCheckboxListItem> model = getModel();

        for (int i = 0; i < model.getSize(); i++) {
            BoardCheckboxListItem item = model.getElementAt(i);
            if (item.isSelected()) {
                result.add(item);
            }
        }

        return result;
    }
}
