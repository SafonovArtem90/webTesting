package mywebtests.core.extensions;

import io.qameta.allure.Allure;
import mywebtests.core.BrowserSource;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Расширение JUnit 5: пишет браузер текущего теста в метку Allure {@code browser}.
 * Ставится перед выполнением метода теста (BeforeTestExecutionCallback), а не в
 * @BeforeEach: к этому моменту AllureJupiter гарантированно создал контекст теста,
 * и метка не теряется. Браузер читается из {@link BrowserSource}.
 */
public class BrowserLabelExtension implements BeforeTestExecutionCallback {

    /** Имя стандартной метки Allure для браузера. */
    private static final String BROWSER_LABEL = "browser";

    /**
     * Добавляет метку {@code browser} к текущему тесту в отчёте Allure.
     *
     * @param extensionContext контекст теста
     */
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        Object instance = extensionContext.getRequiredTestInstance();
        if (instance instanceof BrowserSource source) {
            Allure.label(BROWSER_LABEL, source.currentBrowser().name());
        }
    }
}
