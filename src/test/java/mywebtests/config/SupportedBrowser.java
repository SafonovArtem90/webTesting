package mywebtests.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Поддерживаемые браузеры Playwright.
 */
@Getter
@AllArgsConstructor
public enum SupportedBrowser {

    /** Chromium (Chrome, Edge). */
    CHROMIUM(Playwright::chromium),

    /** Firefox. */
    FIREFOX(Playwright::firefox),

    /** WebKit (Safari). */
    WEB_KIT(Playwright::webkit);

    /** Провайдер типа браузера. */
    private final Function<Playwright, BrowserType> provider;

    /**
     * Запускает браузер с указанными опциями.
     *
     * @param playwright экземпляр Playwright
     * @param options опции запуска
     * @return запущенный браузер
     */
    public Browser launch(Playwright playwright, BrowserType.LaunchOptions options) {
        return provider.apply(playwright).launch(options);
    }
}
