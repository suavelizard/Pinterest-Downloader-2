package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.ScraperData;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.webdrivers.WebDriverPool;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public abstract class PinterestWebExecutor<T> extends TaskExecutor<T> {

    private final String username;
    private final String password;
    private RemoteWebDriver driver;

    public PinterestWebExecutor(Task task, String username, String password) {
        super(task);
        this.username = username;
        this.password = password;
    }

    @Override
    public T call() throws Exception {
        final T result;

        try {
            this.driver = WebDriverPool.getDriver();

            if (!isAuthenticated()) {
                this.login();
            }

            result = this.execute();

            WebDriverPool.returnDriver(driver);
        } catch (Exception e) {
            WebDriverPool.invalidateDriver(driver);
            throw e;
        } finally {
            this.done();
        }

        return result;
    }

    /**
     * Navigate to the user's profile
     *
     * @throws Exception Driver error
     */
    protected void goToProfile() throws Exception {
        String urlUsername = username.split("@")[0];
        navigate(ScraperData.get("data.urls.pinterest.main") + urlUsername);
    }

    /**
     * Navigate to the given url
     */
    protected void navigate(String uri) {
        driver.get(uri);
    }

    /**
     * Scroll to the end of the current page
     * Scroll to the end of the current page
     *
     * @throws Exception Driver error oir script error
     */
    protected void scrollDown() throws Exception {
        executeScript("/js/window-scroll-down.js");
        Thread.sleep(1000);
    }

    protected WebElement getElement(By by) {
        return await(ExpectedConditions.presenceOfElementLocated(by));
    }

    protected List<WebElement> getElements(By by) {
        return await(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    protected int countElements(String xPath) throws Exception {
        return Math.toIntExact((Long) executeScript("/js/count-xpath-elements.js", xPath));
    }

    private void login() throws Exception {
        getTask().setTask(I18n.get("worker.common.loggingIn"));

        if (!isNavigated()) {
            navigate(ScraperData.get("data.urls.pinterest.loginPage"));
        }

        WebElement usernameInput = getElement(ScraperData.by("xpath.loginPage.usernameField"));
        WebElement passwordInput = getElement(ScraperData.by("xpath.loginPage.passwordField"));
        WebElement loginButton = getElement(ScraperData.by("class.loginPage.loginButton"));
        if (usernameInput != null && passwordInput != null) {
            usernameInput.sendKeys(username);
            passwordInput.sendKeys(password);
            loginButton.click();

            getElement(ScraperData.by("class.mainPage.feed"));
        }
    }

    private boolean isNavigated() {
        try {
            return !"data:,".equals(driver.getCurrentUrl());
        } catch (WebDriverException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
            return false;
        }
    }

    private boolean isAuthenticated() {
        final Cookie authCookie = driver.manage().getCookieNamed("_auth");
        return authCookie != null && "1".equals(authCookie.getValue());
    }

    private <R> R await(ExpectedCondition<R> expectedCondition) {
        return new WebDriverWait(driver, 2, 500)
                .until(expectedCondition);
    }

    protected Object executeScript(String name, Object... args) throws Exception {
        final InputStream stream = PinterestWebExecutor.class.getResourceAsStream(name);
        String script = IOUtils.toString(stream, "UTF-8");
        //noinspection unchecked
        return driver.executeScript(script, (Object[]) args);
    }
}
