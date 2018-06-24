package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.ScraperData;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class FetchBoardsExecutor extends PinterestWebExecutor<List<Board>> {

    private final List<Board> currentBoards;

    public FetchBoardsExecutor(Task task, String username, String password, List<Board> currentBoards) {
        super(task, username, password);
        this.currentBoards = currentBoards;
    }

    @Override
    protected List<Board> execute() throws Exception {
        getTask().setTask(I18n.get("worker.fetchBoardsWorker.gettingBoards"));
        goToProfile();

        final WebElement boardsFeed = getElement(ScraperData.by("class.profileBoards.feed"));
        List<WebElement> boardWrappers;
        List<WebElement> boardWrappersTemp = new ArrayList<>();

        do {
            boardWrappers = boardWrappersTemp;
            scrollDown();
            boardWrappersTemp = boardsFeed.findElements(ScraperData.by("xpath.profileBoards.feed.Items"));
        } while (boardWrappersTemp.size() > boardWrappers.size());

        getTask().setTask(I18n.get("worker.fetchBoardsWorker.processingBoards"));
        getTask().setProgressMax(boardWrappers.size());

        return boardWrappers.stream()
                .peek(board -> getTask().incrementProgress())
                .map(this::mapElementToBoard)
                .filter(Objects::nonNull)
                .filter(board -> currentBoards.stream()
                        .noneMatch(targetBoard -> targetBoard.getUrl().equals(board.getUrl())))
                .sorted(Comparator.comparing(Board::getName))
                .collect(Collectors.toList());
    }

    private Board mapElementToBoard(WebElement webElement) {
        try {
            String boardUri = webElement
                    .findElement(ScraperData.by("xpath.profileBoards.feed.items.boardLink"))
                    .getAttribute("href");
            String boardName = webElement
                    .findElement(ScraperData.by("xpath.profileBoards.feed.items.boardName"))
                    .getText();


            Board board = new Board();
            board.setName(boardName);
            board.setUrl(boardUri);

            return board;
        } catch (NoSuchElementException ignored) {
            // If mapping fails it's most probably not an actual board
        }

        return null;
    }
}
