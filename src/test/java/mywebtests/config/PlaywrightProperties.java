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

    /** Браузер для запуска. */
    private SupportedBrowser browser = SupportedBrowser.CHROMIUM;

    /** Запуск браузера в фоновом режиме. */
    private boolean headless = true;

    /** Таймаут ожиданий Playwright по умолчанию. */
    private Duration timeout = Duration.ofSeconds(10);
}
