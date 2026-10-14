package mywebtests.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import mywebtests.config.PlaywrightProperties;
import mywebtests.config.SupportedBrowser;
import mywebtests.core.extensions.BrowserLabelExtension;
import mywebtests.core.extensions.RetryOnFailureExtension;
import mywebtests.core.extensions.ScreenshotOnFailureExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import mywebtests.SaucedemoTestApplication;
import mywebtests.data.UserDataProvider;

/**
 * Базовый класс всех UI-тестов.
 * Поднимает Spring-контекст, создаёт изолированный контекст Playwright на каждый тест
 * и подключает расширения: скриншот при падении, метка браузера и повтор при неудаче.
 */
@SpringBootTest(classes = SaucedemoTestApplication.class)
@ExtendWith({ScreenshotOnFailureExtension.class,
             BrowserLabelExtension.class,
             RetryOnFailureExtension.class})
public abstract class BaseTest implements PlaywrightSessionReset, PageSource, BrowserSource {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    @Autowired
    protected PlaywrightProperties properties;

    @Autowired
    protected UserDataProvider userDataProvider;

    @Autowired
    protected BrowserSelector browserSelector;

    @Autowired
    protected PlaywrightDriverPool driverPool;

    private SupportedBrowser testBrowser;

    private Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected PageFactory pages;

    /**
     * Создаёт новую сессию Playwright перед каждым тестом.
     */
    @BeforeEach
    void setUpPlaywrightSession() {
        createSession();
    }

    /**
     * Закрывает сессию Playwright после каждого теста.
     */
    @AfterEach
    void tearDownPlaywrightSession() {
        closeSession();
    }

    /**
     * Страница текущего теста — читается расширениями JUnit через интерфейс {@link PageSource}.
     *
     * @return страница Playwright текущего теста
     */
    @Override
    public Page getPage() {
        return page;
    }

    /**
     * Браузер текущего теста — читается расширением {@link BrowserLabelExtension}
     * для метки Allure; закреплён за тестом на все попытки.
     *
     * @return выбранный для теста браузер
     */
    @Override
    public SupportedBrowser currentBrowser() {
        return testBrowser;
    }

    /**
     * Пересоздаёт контекст Playwright (используется между повторными попытками теста).
     * Браузер теста не меняется: переиспользуется из пула.
     */
    @Override
    public void resetSession() {
        log.debug("Пересоздание контекста Playwright для повторной попытки теста");
        closeSession();
        createSession();
    }

    /**
     * Создаёт браузер, контекст и страницу с настроенными таймаутами.
     * Playwright-драйвер переиспользуется из пула (один на поток), браузер — свежий
     * на тест (живые соединения переиспользуемого браузера ненадёжны). Браузер
     * закрепляется за тестом на все попытки, включая повторные.
     */
    private void createSession() {
        if (testBrowser == null) {
            testBrowser = browserSelector.next();
        }
        browser = driverPool.newBrowser(testBrowser, properties.isHeadless());
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(properties.getTimeout().toMillis());
        page.setDefaultNavigationTimeout(properties.getTimeout().toMillis());
        PlaywrightAssertions.setDefaultAssertionTimeout(properties.getTimeout().toMillis());
        pages = new PageFactory(page, properties);
        log.info("Сессия Playwright создана: браузер={}, headless={}, таймаут={}",
                testBrowser, properties.isHeadless(), properties.getTimeout());
    }

    /**
     * Закрывает контекст и браузер текущего теста, игнорируя ошибки закрытия
     * частично созданных объектов. Playwright-драйвер переиспользуется — его
     * закрывает {@link PlaywrightDriverPool} по завершении прогона.
     */
    private void closeSession() {
        try {
            if (context != null) {
                context.close();
            }
        } catch (RuntimeException ignored) {
            // Контекст мог не создаться или уже быть закрыт
        }
        try {
            if (browser != null) {
                browser.close();
            }
        } catch (RuntimeException ignored) {
            // Браузер мог не запуститься
        }
        context = null;
        browser = null;
        page = null;
        pages = null;
        log.debug("Сессия Playwright закрыта");
    }
}
