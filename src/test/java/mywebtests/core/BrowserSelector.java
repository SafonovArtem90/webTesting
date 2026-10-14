package mywebtests.core;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import mywebtests.config.PlaywrightProperties;
import mywebtests.config.SupportedBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Выбирает браузер для текущего теста из {@link SupportedBrowser}.
 * На весь прогон выбирается пара случайных установленных браузеров, и каждый тест
 * рандомно уходит в один из двух (набор тестов разбивается между браузерами).
 * Одиночный запуск теста идёт на браузере по умолчанию — Chromium.
 * Явный выбор браузера — через свойство playwright.browser (env PLAYWRIGHT_BROWSER).
 */
@Component
public class BrowserSelector {

    private static final Logger log = LoggerFactory.getLogger(BrowserSelector.class);

    private final PlaywrightProperties properties;
    private final List<SupportedBrowser> pair;

    /**
     * Создаёт селектор и однократно выбирает пару браузеров прогона.
     *
     * @param properties настройки Playwright
     */
    public BrowserSelector(PlaywrightProperties properties) {
        this.properties = properties;
        this.pair = pickPair();
        log.info("Браузеры прогона: {}", pair);
    }

    /**
     * Возвращает браузер для следующего теста.
     *
     * @return выбранный браузер
     * @throws IllegalArgumentException если оверрайд не совпадает с именем {@link SupportedBrowser}
     */
    public SupportedBrowser next() {
        String override = properties.getBrowser();
        if (override != null && !override.isBlank()) {
            return SupportedBrowser.valueOf(override.trim().toUpperCase());
        }
        if (TestRunInfo.isSingleTestRun()) {
            return defaultBrowser();
        }
        return pair.get(ThreadLocalRandom.current().nextInt(pair.size()));
    }

    /**
     * Возвращает браузер по умолчанию для одиночного запуска: Chromium,
     * а если он не установлен — первый установленный.
     *
     * @return браузер по умолчанию
     */
    private static SupportedBrowser defaultBrowser() {
        if (SupportedBrowser.CHROMIUM.isInstalled()) {
            return SupportedBrowser.CHROMIUM;
        }
        List<SupportedBrowser> installed = SupportedBrowser.installed();
        log.warn("Chromium не установлен, для одиночного запуска выбран первый установленный: {}",
                installed.get(0));
        return installed.get(0);
    }

    /**
     * Выбирает пару разных случайных браузеров из установленных.
     *
     * @return список из одного-двух установленных браузеров
     * @throws IllegalStateException если не установлено ни одного браузера Playwright
     */
    private static List<SupportedBrowser> pickPair() {
        List<SupportedBrowser> installed = SupportedBrowser.installed();
        if (installed.isEmpty()) {
            throw new IllegalStateException(
                    "Не найдено установленных браузеров Playwright. Выполните ./gradlew playwrightInstall");
        }
        if (installed.size() == 1) {
            return installed;
        }
        int first = ThreadLocalRandom.current().nextInt(installed.size());
        int second = ThreadLocalRandom.current().nextInt(installed.size() - 1);
        if (second >= first) {
            second++;
        }
        return List.of(installed.get(first), installed.get(second));
    }
}
