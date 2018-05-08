package nl.juraji.pinterestdownloader.workers;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import io.github.bonigarcia.wdm.WebDriverManager;
import nl.juraji.pinterestdownloader.Container;
import nl.juraji.pinterestdownloader.resources.ScraperData;
import nl.juraji.pinterestdownloader.util.workers.IndicatingWorker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 26-4-2018.
 * Pinterest Downloader
 */
public abstract class PinterestScraperWorker<T, V> extends IndicatingWorker<T, V> {
    private static final AtomicReference<RemoteWebDriver> DRIVER_REFERENCE = new AtomicReference<>();

    private final String username;
    private final String password;
    private RemoteWebDriver driver;

    public PinterestScraperWorker(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void destroyDriver() {
        if (DRIVER_REFERENCE.get() != null) {
            DRIVER_REFERENCE.get().quit();
            DRIVER_REFERENCE.set(null);
        }
    }

    protected void login() {
        RemoteWebDriver remoteWebDriver = DRIVER_REFERENCE.get();
        if (remoteWebDriver == null) {
            this.driver = DRIVER_REFERENCE.updateAndGet(d -> setupDriver());
            navigate(ScraperData.get("data.urls.pinterest.loginPage"));

            WebElement usernameInput = getElement(ScraperData.by("xpath.loginPage.usernameField"));
            WebElement passwordInput = getElement(ScraperData.by("xpath.loginPage.passwordField"));
            WebElement loginButton = getElement(ScraperData.by("class.loginPage.loginButton"));
            if (usernameInput != null && passwordInput != null) {
                usernameInput.sendKeys(username);
                passwordInput.sendKeys(password);
                loginButton.click();

                getElement(ScraperData.by("class.mainPage.feed"));
            }
        } else {
            this.driver = remoteWebDriver;
        }
    }

    protected void goToProfile() {
        if (testDriverReady()) {
            throw new IllegalStateException("No website loaded, use login() first");
        }

        String urlUsername = username.split("@")[0];
        navigate(ScraperData.get("data.urls.pinterest.main") + urlUsername);
    }

    protected void navigate(String uri) {
        if (testDriverReady()) {
            throw new IllegalStateException("No website loaded, use login() first");
        }

        if (!driver.getCurrentUrl().equals(uri)) {
            driver.get(uri);
        }
    }

    protected void scrollDown() throws InterruptedException {
        scrollDown(1);
    }

    protected void scrollDown(int times) throws InterruptedException {
        if (testDriverReady()) {
            throw new IllegalStateException("No website loaded, use login() first");
        }

        for (int i = 0; i < times; i++) {
            driver.executeScript(getScript("/js/window-scroll-down.js"));
            Thread.sleep(1000);
        }
    }

    protected WebElement getElement(By by) {
        if (testDriverReady()) {
            throw new IllegalStateException("No website loaded, use navigate() first");
        }

        return await(ExpectedConditions.presenceOfElementLocated(by));
    }

    protected List<WebElement> getElements(By by) {
        if (testDriverReady()) {
            throw new IllegalStateException("No website loaded, use navigate() first");
        }

        return await(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    private boolean testDriverReady() {
        try {
            return Strings.isNullOrEmpty(driver.getCurrentUrl());
        } catch (WebDriverException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
            return false;
        }
    }

    protected String getScript(String resourceUri) {
        InputStream resourceAsStream = PinterestScraperWorker.class.getResourceAsStream(resourceUri);
        try {
            return CharStreams.toString(new InputStreamReader(resourceAsStream, Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void awaitElementPresent(By by) {
        await(ExpectedConditions.presenceOfElementLocated(by));
    }

    private <R> R await(ExpectedCondition<R> expectedCondition) {
        return new WebDriverWait(driver, 10, 500)
                .until(expectedCondition);
    }

    private RemoteWebDriver setupDriver() {
        WebDriverManager driverManager = WebDriverManager.chromedriver();
        driverManager.targetPath("./");
        driverManager.setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1366,768");

        if (!Container.isDebugMode()) {
            options.setHeadless(true);
        }

        return new ChromeDriver(options);
    }
}
