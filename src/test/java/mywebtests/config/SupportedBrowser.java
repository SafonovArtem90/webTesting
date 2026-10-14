package mywebtests.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Поддерживаемые браузеры Playwright.
 * Хранит имя каталога установки и умеет проверять, установлен ли браузер
 * в кеше Playwright (каталог PLAYWRIGHT_BROWSERS_PATH или дефолтный по ОС).
 */
@Getter
@AllArgsConstructor
public enum SupportedBrowser {

    /** Chromium (Chrome, Edge). */
    CHROMIUM("chromium", Playwright::chromium),

    /** Firefox. */
    FIREFOX("firefox", Playwright::firefox),

    /** WebKit (Safari). */
    WEB_KIT("webkit", Playwright::webkit);

    /** Имя каталога браузера в кеше Playwright (chromium-*, firefox-*, webkit-*). */
    private final String installName;

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

    /** Спецзначение PLAYWRIGHT_BROWSERS_PATH: браузеры лежат рядом с драйвером (детект невозможен). */
    private static final String EMBEDDED_BROWSERS_PATH = "0";

    /**
     * Проверяет, установлен ли браузер в кеше Playwright.
     * При PLAYWRIGHT_BROWSERS_PATH=0 (браузеры внутри пакета драйвера) детект каталога
     * невозможен — считаем браузер установленным и доверяем запуску.
     *
     * @return {@code true}, если каталог установки браузера существует
     */
    public boolean isInstalled() {
        String override = System.getenv("PLAYWRIGHT_BROWSERS_PATH");
        if (EMBEDDED_BROWSERS_PATH.equals(override)) {
            return true;
        }
        Path browsersDir = browsersPath();
        if (browsersDir == null || !Files.isDirectory(browsersDir)) {
            return false;
        }
        try (Stream<Path> entries = Files.list(browsersDir)) {
            String prefix = installName + "-";
            return entries.anyMatch(entry -> entry.getFileName().toString().startsWith(prefix));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Возвращает браузеры, установленные в кеше Playwright.
     *
     * @return список установленных браузеров
     */
    public static List<SupportedBrowser> installed() {
        return Stream.of(values()).filter(SupportedBrowser::isInstalled).toList();
    }

    /**
     * Определяет каталог кеша браузеров Playwright.
     *
     * @return путь к каталогу или {@code null}, если переменная LOCALAPPDATA не задана на Windows
     */
    private static Path browsersPath() {
        String override = System.getenv("PLAYWRIGHT_BROWSERS_PATH");
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            return localAppData == null ? null : Paths.get(localAppData, "ms-playwright");
        }
        if (os.contains("mac")) {
            return Paths.get(home, "Library", "Caches", "ms-playwright");
        }
        return Paths.get(home, ".cache", "ms-playwright");
    }
}
