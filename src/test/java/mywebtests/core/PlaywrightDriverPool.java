package mywebtests.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import mywebtests.config.SupportedBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Пул Playwright-драйверов: на каждый рабочий поток — один процесс Playwright
 * (дорогая часть: node-процесс и драйверы браузеров создаются один раз на прогон).
 * Браузер при этом запускается свежий на каждый тест: keep-alive соединения живого
 * браузера переживают закрытие контекста и могут зависать на переиспользуемых
 * сокетах (проверено на saucedemo: флак checkout-теста error_user), поэтому
 * браузер не переиспользуется. Изоляция теста — новый BrowserContext в BaseTest.
 * Все созданные драйверы и браузеры закрываются JVM shutdown hook'ом
 * по завершении прогона.
 */
@Component
public class PlaywrightDriverPool {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightDriverPool.class);

    /** Все созданные драйверы — для закрытия по завершении прогона. */
    private static final List<Driver> ALL_DRIVERS = new CopyOnWriteArrayList<>();

    private static final AtomicBoolean HOOK_REGISTERED = new AtomicBoolean();

    /** Драйверы текущего потока: один Playwright на поток. */
    private final ThreadLocal<Driver> currentDriver = new ThreadLocal<>();

    /**
     * Запускает свежий браузер указанного типа для текущего потока, переиспользуя
     * Playwright-драйвер потока (лениво создаёт его при первом обращении).
     * Вызывающий обязан закрыть браузер после использования (делает BaseTest);
     * на случай утечек браузер дополнительно закрывается shutdown hook'ом.
     *
     * @param type тип браузера
     * @param headless запуск в фоновом режиме
     * @return новый запущенный браузер
     */
    public Browser newBrowser(SupportedBrowser type, boolean headless) {
        Driver driver = currentDriver.get();
        if (driver == null) {
            driver = new Driver(Playwright.create());
            currentDriver.set(driver);
            ALL_DRIVERS.add(driver);
            registerShutdownHook();
            log.info("Создан Playwright-драйвер для потока {}", Thread.currentThread().getName());
        }
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);
        Browser browser = type.launch(driver.playwright, options);
        driver.browsers.add(browser);
        log.info("Запущен браузер {} для потока {}", type, Thread.currentThread().getName());
        return browser;
    }

    /**
     * Закрывает все драйверы и браузеры прогона. Идемпотентно; вызывается
     * shutdown hook'ом, когда тестовые потоки уже завершены.
     */
    public static void closeAll() {
        for (Driver driver : ALL_DRIVERS) {
            driver.browsers.forEach(browser -> {
                try {
                    browser.close();
                } catch (RuntimeException e) {
                    log.debug("Браузер уже закрыт: {}", e.getMessage());
                }
            });
            driver.browsers.clear();
            try {
                driver.playwright.close();
            } catch (RuntimeException e) {
                log.debug("Playwright-драйвер уже закрыт: {}", e.getMessage());
            }
        }
        ALL_DRIVERS.clear();
        log.debug("Пул Playwright-драйверов очищен");
    }

    /**
     * Регистрирует однократный JVM shutdown hook для закрытия пула.
     */
    private static void registerShutdownHook() {
        if (HOOK_REGISTERED.compareAndSet(false, true)) {
            Thread hook = new Thread(PlaywrightDriverPool::closeAll, "playwright-pool-shutdown");
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }

    /**
     * Драйвер одного потока: один процесс Playwright и запущенные им браузеры.
     *
     * @param playwright экземпляр Playwright потока
     * @param browsers запущенные браузеры для контроля на утечки
     */
    private record Driver(Playwright playwright, List<Browser> browsers) {

        private Driver(Playwright playwright) {
            this(playwright, new CopyOnWriteArrayList<>());
        }
    }
}
