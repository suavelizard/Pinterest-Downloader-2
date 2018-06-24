package nl.juraji.pinterestdownloader.util.webdrivers;

import io.github.bonigarcia.wdm.WebDriverManager;
import nl.juraji.pinterestdownloader.Container;
import nl.juraji.pinterestdownloader.util.AtomicObject;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Created by Juraji on 21-6-2018.
 * Pinterest Downloader
 */
public class WebDriverPool extends GenericObjectPool<RemoteWebDriver> {
    private static final AtomicObject<WebDriverPool> INSTANCE = new AtomicObject<>();

    private WebDriverPool() {
        super(new PoolabeWebDriverFactory());
        this.setBlockWhenExhausted(true);
        this.setMaxTotal(3);
        this.setMinEvictableIdleTimeMillis(5000);
        this.setTestOnReturn(true);
    }

    public static RemoteWebDriver getDriver() throws Exception {
        // Synchronization guarantees threadsafety when creating the driver pool
        synchronized (WebDriverPool.class) {
            if (INSTANCE.isEmpty()) {
                final WebDriverPool pool = new WebDriverPool();
                pool.preparePool();
                INSTANCE.set(pool);
            }
        }

        return INSTANCE.get().borrowObject();
    }

    public static void returnDriver(RemoteWebDriver driver) {
        if (INSTANCE.isSet()) {
            INSTANCE.get().returnObject(driver);
        }
    }

    public static void shutdown() {
        if (INSTANCE.isSet()) {
            INSTANCE.get().close();
        }
    }

    public static void invalidateDriver(RemoteWebDriver driver) throws Exception {
        if (INSTANCE.isSet()) {
            INSTANCE.get().invalidateObject(driver);
        }
    }

    private static class PoolabeWebDriverFactory extends BasePooledObjectFactory<RemoteWebDriver> {
        private static final long MAX_JS_MEMORY_USAGE = 800000000; // 800Mb
        private final ChromeOptions driverOptions;

        public PoolabeWebDriverFactory() {
            WebDriverManager driverManager = WebDriverManager.chromedriver();
            driverManager.targetPath("./");
            driverManager.setup();

            driverOptions = new ChromeOptions();
            driverOptions.addArguments("--window-size=1366,768");
            driverOptions.setHeadless(!Container.isDebugMode());
        }

        @Override
        public RemoteWebDriver create() {
            return new ChromeDriver(driverOptions);
        }

        @Override
        public PooledObject<RemoteWebDriver> wrap(RemoteWebDriver driver) {
            return new DefaultPooledObject<>(driver);
        }

        @Override
        public void destroyObject(PooledObject<RemoteWebDriver> p) {
            p.getObject().quit();
        }

        @Override
        public boolean validateObject(PooledObject<RemoteWebDriver> p) {
            try {
                Long jsMemoryUsage = (Long) p.getObject().executeScript("return console.memory.totalJSHeapSize");
                return jsMemoryUsage < MAX_JS_MEMORY_USAGE;
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}
