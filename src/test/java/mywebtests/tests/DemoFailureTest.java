package mywebtests.tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import mywebtests.core.BaseTest;
import mywebtests.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Демо-тест: заведомо падает для проверки отображения ошибки и скриншота
 * в отчёте Allure. По умолчанию пропускается и запускается только при
 * переменной окружения DEMO_FAIL=true.
 */
@DisplayName("Демонстрация падения: ошибка и скриншот в Allure")
class DemoFailureTest extends BaseTest {

    /**
     * Осознанно неверное ожидание: проверяет, что падение теста порождает
     * ошибку в Allure и скриншот (файл и вложение к тесту).
     */
    @DisplayName("Демо-падение: ошибка и скриншот отображаются в отчёте")
    @Severity(SeverityLevel.TRIVIAL)
    @Test
    @EnabledIfEnvironmentVariable(
            named = "DEMO_FAIL",
            matches = "true",
            disabledReason = "Демо-падение включается переменной окружения DEMO_FAIL=true")
    void deliberatelyFailingTestShowsErrorAndScreenshotInAllure() {
        LoginPage loginPage = pages.create(LoginPage.class).open();

        // На странице логина кнопка видна — ожидание обратного заведомо ложно
        assertThat(loginPage.isLoginButtonVisible()).isFalse();
    }
}
