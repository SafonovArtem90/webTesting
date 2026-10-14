package mywebtests.core.extensions;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import mywebtests.core.WebDriverHolder;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Расширение JUnit 5: при падении теста сохраняет скриншот страницы в файл
 * и прикрепляет его к отчёту Allure.
 * Реализовано через AfterTestExecutionCallback, чтобы снимок делался до закрытия
 * браузера в @AfterEach. Файлы пишутся в директорию из системного свойства
 * reporting.screenshot-dir (мостится из переменной окружения SCREENSHOT_DIR в build.gradle).
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotOnFailureExtension.class);

    private static final String DEFAULT_SCREENSHOT_DIR = "build/screenshots";
    private static final String ALLURE_ATTACHMENT_NAME = "Скриншот при падении";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * Вызывается сразу после выполнения метода теста: при исключении сохраняет скриншот.
     *
     * @param context контекст выполненного теста
     */
    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Page page = WebDriverHolder.getPage();
        if (page == null) {
            log.warn("Скриншот для '{}' не сделан: страница уже закрыта", context.getDisplayName());
            return;
        }
        Path dir = Paths.get(System.getProperty("reporting.screenshot-dir", DEFAULT_SCREENSHOT_DIR));
        String fileName = sanitize(context.getDisplayName()) + "_"
                + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".png";
        try {
            byte[] screenshot = page.screenshot();
            Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), screenshot);
            log.info("Скриншот при падении '{}' сохранён: {}", context.getDisplayName(), dir.resolve(fileName));
            attachToAllure(screenshot);
        } catch (IOException | RuntimeException e) {
            log.warn("Не удалось сохранить скриншот для '{}': {}", context.getDisplayName(), e.getMessage());
        }
    }

    /**
     * Прикрепляет скриншот к текущему тесту в отчёте Allure.
     *
     * @param screenshot содержимое скриншота
     */
    private void attachToAllure(byte[] screenshot) {
        try {
            Allure.addAttachment(ALLURE_ATTACHMENT_NAME, "image/png",
                    new ByteArrayInputStream(screenshot), "png");
        } catch (RuntimeException e) {
            log.warn("Не удалось прикрепить скриншот в Allure: {}", e.getMessage());
        }
    }

    /**
     * Заменяет недопустимые символы в имени файла.
     *
     * @param name отображаемое имя теста
     * @return безопасное имя файла
     */
    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
