package nl.juraji.pinterestdownloader.workers;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.ScraperData;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 26-4-2018.
 * Pinterest Downloader
 * <p>
 * Fetches new* pins from the given board.
 */
public class FetchPinsWorker extends PinterestScraperWorker<List<Pin>> {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Board board;
    private final boolean isIncrementalBackup;

    public FetchPinsWorker(ProgressIndicator indicator, String username, String password, Board board, boolean isIncrementalBackup) {
        super(indicator, username, password);
        this.board = board;
        this.isIncrementalBackup = isIncrementalBackup;
    }

    @Override
    protected List<Pin> doInBackground() throws Exception {
        getIndicator().setTask(I18n.get("worker.fetchPinsWorker.taskName", board.getName()));
        getIndicator().setVisible(true);

        getIndicator().setAction(I18n.get("worker.common.loggingIn"));
        login();

        getIndicator().setAction(I18n.get("worker.fetchPinsWorker.gettingPins"));
        navigate(board.getUrl());

        List<Pin> downloadedPins = board.getPins().stream()
                .filter(pin -> pin.getFileOnDisk() != null)
                .collect(Collectors.toList());

        List<WebElement> elements;
        List<Pin> resultingPins = new ArrayList<>();

        int reportedPinCount = getReportedPinCount();
        int pinsToFetchCount;

        if (isIncrementalBackup) {
            int downloadedPinsCount = downloadedPins.size();
            pinsToFetchCount = reportedPinCount - downloadedPinsCount;

            if (reportedPinCount == downloadedPinsCount) {
                // return null to prevent further processing, but do not try to fetch more.
                return null;
            }
        } else {
            pinsToFetchCount = reportedPinCount;
        }

        AtomicInteger previousElCount = new AtomicInteger(0);
        AtomicInteger retryCounter = new AtomicInteger(1);
        getIndicator().setProgressBarMax(pinsToFetchCount);

        do {
            elements = getElements(ScraperData.by("xpath.boardPins.pins.feed"));
            getIndicator().setProgressBarValue(elements.size());
            scrollDown();

            if (previousElCount.get() == elements.size()) {
                // If the previous element count equals the current,
                // increment the retry counter and try again.
                // on fifth try break the loop and hope for better next time
                if (retryCounter.addAndGet(1) == 5) {
                    logger.log(Level.WARNING, "Too many retries for fetching pins, board: " + board.getName()
                            + ", reported count: " + reportedPinCount + ", found: " + elements.size());
                    break;
                }
            } else {
                retryCounter.set(1);
                previousElCount.set(elements.size());
            }
        } while (elements.size() < pinsToFetchCount);

        getIndicator().resetProgressBar();
        getIndicator().setAction(I18n.get("worker.fetchPinsWorker.processingPins"));
        getIndicator().setProgressBarMax(elements.size());

        elements.stream()
                .peek(pin -> getIndicator().incrementProgressBar())
                .map(this::mapElementToPin)
                .filter(Objects::nonNull)
                .filter(pin -> downloadedPins.stream().noneMatch(pin1 -> pin.getPinId().equals(pin1.getPinId())))
                .forEach(resultingPins::add);

        return resultingPins;
    }

    private int getReportedPinCount() {
        WebElement pinCountElement = getElement(ScraperData.by("xpath.boardPins.pinCount"));
        if (pinCountElement != null) {
            String count = pinCountElement.getText()
                    .replace(" pins", "")
                    .replace(".", "");

            return Integer.parseInt(count);
        }

        return 0;
    }

    private Pin mapElementToPin(WebElement webElement) {
        try {
            String pinUrl = webElement
                    .findElement(ScraperData.by("xpath.boardPins.pins.feed.pinLink"))
                    .getAttribute("href");

            String pinImgSrc = webElement
                    .findElement(ScraperData.by("xpath.boardPins.pins.feed.pinImgLink"))
                    .getAttribute("src");

            String description;
            try {
                description = webElement
                        .findElement(ScraperData.by("xpath.boardPins.pins.feed.pinDescription"))
                        .getText();
            } catch (NoSuchElementException e) {
                description = "";
            }

            Pin pin = new Pin();
            pin.setPinId(pinUrl.replaceAll("^.*/pin/(.+)/$", "$1"));
            pin.setDescription(description.trim());
            pin.setUrl(pinUrl);
            pin.setOriginalUrl(pinImgSrc);
            pin.setBoard(board);

            return pin;
        } catch (NoSuchElementException ignored) {
            // If mapping fails it's most probably not a valid pin
        }
        return null;
    }
}
