package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.model.Board;

import javax.swing.*;
import java.util.List;

/**
 * Created by Juraji on 8-5-2018.
 * Pinterest Downloader
 */
public class BoardsList extends JList<Board> {
    public BoardsList() {
        setCellRenderer(new DefaultListCellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setModel(new DefaultListModel<>());
    }

    public void setBoards(List<Board> boards) {
        final DefaultListModel<Board> model = (DefaultListModel<Board>) getModel();
        boards.forEach(model::addElement);
    }

    public void clear() {
        ((DefaultListModel<Board>) getModel()).clear();
    }
}
