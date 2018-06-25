package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.model.Board;
import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.ScraperData;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class FetchPinsExecutor extends PinterestWebExecutor<List<Pin>> {

    private final Board board;
    private final BackupMode backupMode;

    public FetchPinsExecutor(Task task, String username, String password, Board board, BackupMode backupMode) {
        super(task, username, password);
        this.board = board;
        this.backupMode = backupMode;
    }

    @Override
    public List<Pin> execute() throws Exception {
        getTask().setTask(I18n.get("worker.fetchPinsWorker.taskName", board.getName()));
        navigate(board.getUrl());
        super.executeScript("/js/hide-pinwrappers.js");

        final List<Pin> downloadedPins = board.getPins().stream()
                .filter(pin -> pin.getFileOnDisk() != null)
                .collect(Collectors.toList());

        final int reportedPinCount = getReportedPinCount();
        final int pinsToFetchCount;

        if (BackupMode.INCREMENTAL_UPDATE.equals(backupMode)) {
            int downloadedPinsCount = downloadedPins.size();
            pinsToFetchCount = reportedPinCount - downloadedPinsCount;

            if (reportedPinCount == downloadedPinsCount) {
                // return null to prevent further processing, but do not try to fetch more.
                return null;
            }
        } else {
            // We allow a delta of 5, since Pinterest can be wrong on the total pin count
            pinsToFetchCount = reportedPinCount - 5;
        }

        AtomicInteger previousElCount = new AtomicInteger(0);
        AtomicInteger currentCount = new AtomicInteger(0);
        AtomicInteger retryCounter = new AtomicInteger(1);
        getTask().setProgressMax(pinsToFetchCount);

        do {
            // Scroll to end and wait for a bit
            scrollDown();

            // Fetch all pinwrapper elements
            final int count = countElements(ScraperData.get("xpath.boardPins.pins.feed"));
            currentCount.set(count);
            getTask().setProgress(count);

            if (previousElCount.get() == count) {
                // If the previous element count equals the current,
                // increment the retry counter and try again.
                // on fifth try break the loop and hope for better next time
                if (retryCounter.addAndGet(1) == 5) {
                    throw new Exception("Too many retries for fetching pins, board: " + board.getName()
                            + ", reported count: " + reportedPinCount + ", found: " + count);
                }
            } else {
                retryCounter.set(1);
                previousElCount.set(count);
            }
        } while (currentCount.get() < pinsToFetchCount);

        final List<Pin> resultingPins = new ArrayList<>();
        final List<WebElement> elements = getElements(ScraperData.by("xpath.boardPins.pins.feed"));

        getTask().reset();
        getTask().setTask(I18n.get("worker.fetchPinsWorker.processingPins"));
        getTask().setProgressMax(elements.size());

        elements.stream()
                .peek(pin -> getTask().incrementProgress())
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
                    .replace(" Pins", "")
                    .replace(".", "");

            return Integer.parseInt(count);
        }

        return 0;
    }

    private Pin mapElementToPin(WebElement webElement) {
        try {
            final Pin pin = new Pin();
            pin.setBoard(board);

            final String pinUrl = webElement
                    .findElement(ScraperData.by("xpath.boardPins.pins.feed.pinLink"))
                    .getAttribute("href");

            pin.setPinId(pinUrl.replaceAll("^.*/pin/(.+)/$", "$1"));
            pin.setUrl(pinUrl);

            final String[] pinImgSrcSet = webElement
                    .findElement(ScraperData.by("xpath.boardPins.pins.feed.pinImgLink"))
                    .getAttribute("srcset")
                    .split(", ");
            final List<String> imgUrls = Arrays.stream(pinImgSrcSet)
                    .sorted((a, b) -> {
                        final String ax = a.substring(a.length() - 3, a.length() - 1);
                        final String bx = b.substring(b.length() - 3, b.length() - 1);
                        return bx.compareTo(ax);
                    })
                    .map(s -> s.substring(0, s.length() - 3))
                    .collect(Collectors.toList());

            pin.setSourceUrls(imgUrls);

            try {
                final String description = webElement
                        .findElement(ScraperData.by("xpath.boardPins.pins.feed.pinDescription"))
                        .getText();
                pin.setDescription(description.trim());
            } catch (NoSuchElementException ignored) {
            }

            return pin;
        } catch (NoSuchElementException ignored) {
            // If mapping fails it's most probably not a valid pin
        }
        return null;
    }
}
