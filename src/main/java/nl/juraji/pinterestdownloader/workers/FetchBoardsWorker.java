package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.ScraperData;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
public class FetchBoardsWorker extends PinterestScraperWorker<Void, Board> {

    private final List<Board> currentBoards;

    public FetchBoardsWorker(String username, String password, List<Board> currentBoards) {
        super(username, password);
        this.currentBoards = currentBoards;
    }

    @Override
    protected Void doInBackground() throws Exception {
        getIndicator().setTask(I18n.get("worker.fetchBoardsWorker.taskName"));
        getIndicator().setVisible(true);

        getIndicator().setAction(I18n.get("worker.common.loggingIn"));
        login();

        getIndicator().setAction(I18n.get("worker.fetchBoardsWorker.gettingBoards"));
        goToProfile();
        scrollDown(3);

        WebElement boardsFeed = getElement(ScraperData.by("class.profileBoards.feed"));
        List<WebElement> boardWrappers = boardsFeed.findElements(ScraperData.by("xpath.profileBoards.feed.Items"));

        if (boardWrappers != null) {
            getIndicator().setAction(I18n.get("worker.fetchBoardsWorker.processingBoards"));
            getIndicator().setProgressBarMax(boardWrappers.size());

            boardWrappers.stream()
                    .peek(board -> getIndicator().incrementProgressBar())
                    .map(this::mapElementToBoard)
                    .filter(Objects::nonNull)
                    .filter(board -> currentBoards.stream()
                            .noneMatch(targetBoard -> targetBoard.getUrl().equals(board.getUrl())))
                    .sorted(Comparator.comparing(Board::getName))
                    .forEach(this::publish);
        }

        return null;
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
            // If mapping fails it's most probably not a valid board
        }

        return null;
    }
}
