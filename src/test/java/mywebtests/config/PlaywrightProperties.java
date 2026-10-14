package mywebtests.config;

import java.time.Duration;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки Playwright из application.properties.
 * Переопределяются переменными окружения: PLAYWRIGHT_BASE_URL, PLAYWRIGHT_BROWSER,
 * PLAYWRIGHT_HEADLESS, PLAYWRIGHT_TIMEOUT.
 */
@Getter
@ConfigurationProperties(prefix = "playwright")
public class PlaywrightProperties {

    /** Базовый URL тестируемого приложения. */
    private String baseUrl = "https://www.saucedemo.com";

    /** Оверрайд браузера: имя {@link SupportedBrowser} (CHROMIUM/FIREFOX/WEB_KIT) или пусто — автовыбор. */
    private String browser;

    /**
     * Устанавливает оверрайд браузера. Сеттер обязателен: без него Spring Boot
     * не может забиндить переопределение из переменной окружения PLAYWRIGHT_BROWSER.
     *
     * @param browser имя браузера или {@code null}/пусто — автовыбор
     */
    public void setBrowser(String browser) {
        this.browser = browser;
    }

    /** Запуск браузера в фоновом режиме. */
    private boolean headless = true;

    /** Таймаут ожиданий Playwright по умолчанию. */
    private Duration timeout = Duration.ofSeconds(10);
}
