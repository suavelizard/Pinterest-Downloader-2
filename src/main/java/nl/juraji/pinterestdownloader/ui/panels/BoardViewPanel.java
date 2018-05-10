package nl.juraji.pinterestdownloader.ui.panels;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.BoardDao;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.TabWindow;
import nl.juraji.pinterestdownloader.ui.components.BoardsList;
import nl.juraji.pinterestdownloader.ui.components.PinsList;
import nl.juraji.pinterestdownloader.ui.components.PinsListContextMenu;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 8-5-2018.
 * Pinterest Downloader
 */
public class BoardViewPanel implements TabWindow {
    private JPanel contentPane;
    private BoardsList boardsList;
    private PinsList boardContentList;

    @Inject
    private BoardDao boardDao;

    @Override
    public String getTitle() {
        return I18n.get("ui.viewBoards.tabTitle");
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    @PostConstruct
    private void init() {
        boardsList.addListSelectionListener(e -> {
            final Board board = boardsList.getSelectedValue();
            if (board != null) {
                Board b = boardDao.initPinImageHashes(board);
                if (b != null) {
                    boardContentList.setPins(b.getPins());
                }
            }
        });

        boardContentList.setPopupMenu(new PinsListContextMenu(boardContentList, boardDao));
    }

    @Override
    public void activate() {
        final List<Board> boards = boardDao.get(Board.class).stream()
                .sorted(Comparator.comparing(Board::getName))
                .collect(Collectors.toList());

        boardsList.setBoards(boards);
    }

    @Override
    public void deactivate() {
        boardsList.clear();
        boardContentList.clear();
    }
}
