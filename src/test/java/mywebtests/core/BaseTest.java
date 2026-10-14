package mywebtests.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import mywebtests.config.PlaywrightProperties;
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
 * Поднимает Spring-контекст, создаёт изолированную сессию Playwright на каждый тест
 * и подключает расширения: скриншот при падении и повтор при неудаче.
 */
@SpringBootTest(classes = SaucedemoTestApplication.class)
@ExtendWith({ScreenshotOnFailureExtension.class,
             RetryOnFailureExtension.class})
public abstract class BaseTest implements PlaywrightSessionReset {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    @Autowired
    protected PlaywrightProperties properties;

    @Autowired
    protected UserDataProvider userDataProvider;

    protected Playwright playwright;
    protected Browser browser;
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
     * Пересоздаёт сессию Playwright (используется между повторными запусками теста).
     */
    @Override
    public void resetSession() {
        log.debug("Пересоздание сессии Playwright для повторной попытки теста");
        closeSession();
        createSession();
    }

    /**
     * Создаёт Playwright, браузер, контекст и страницу с настроенными таймаутами.
     */
    private void createSession() {
        playwright = Playwright.create();
        browser = properties.getBrowser()
                .launch(playwright, new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(properties.getTimeout().toMillis());
        page.setDefaultNavigationTimeout(properties.getTimeout().toMillis());
        PlaywrightAssertions.setDefaultAssertionTimeout(properties.getTimeout().toMillis());
        pages = new PageFactory(page, properties);
        WebDriverHolder.setPage(page);
        log.info("Сессия Playwright создана: браузер={}, headless={}, таймаут={}",
                properties.getBrowser(), properties.isHeadless(), properties.getTimeout());
    }

    /**
     * Закрывает сессию Playwright, игнорируя ошибки при закрытии частично созданных объектов.
     */
    private void closeSession() {
        WebDriverHolder.clear();
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
        if (playwright != null) {
            playwright.close();
        }
        context = null;
        browser = null;
        playwright = null;
        page = null;
        pages = null;
        log.debug("Сессия Playwright закрыта");
    }
}
