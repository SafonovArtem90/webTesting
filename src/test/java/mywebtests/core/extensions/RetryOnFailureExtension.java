package mywebtests.core.extensions;

import io.qameta.allure.Allure;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mywebtests.core.PlaywrightSessionReset;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Расширение JUnit 5: повторяет упавший тест заданное число раз.
 * Перед каждым повтором контекст Playwright пересоздаётся, чтобы тест стартовал с чистой
 * страницы; в отчёт Allure добавляется шаг «Повторная попытка N из M…».
 * Реализовано через TestExecutionExceptionHandler с рефлексивным повторным вызовом метода,
 * а не InvocationInterceptor: повторный {@code proceed()} конфликтует с цепочкой
 * TimeoutExtension/AllureJupiter (JUnitException «called invocation multiple times»).
 * Побочный эффект подхода: таймауты JUnit и перехватчики на повторной попытке не действуют,
 * но таймауты Playwright (page.setDefaultTimeout) действуют — для UI-тестов это главное.
 * Количество повторов задаётся системным свойством testrun.retry-count
 * (мостится из переменной окружения TESTRUN_RETRY_COUNT в build.gradle, по умолчанию 1).
 */
public class RetryOnFailureExtension implements TestExecutionExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RetryOnFailureExtension.class);

    private static final String RETRY_COUNT_PROPERTY = "testrun.retry-count";
    private static final int DEFAULT_RETRY_COUNT = 1;

    /**
     * Перехватывает исключение теста и повторяет его выполнение до исчерпания повторов.
     *
     * @param context контекст теста
     * @param throwable исключение, выброшенное тестом
     * @throws Throwable последнее исключение, если повторы исчерпаны
     */
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        int retries = resolveRetryCount();
        Throwable lastFailure = throwable;
        for (int attempt = 1; attempt <= retries; attempt++) {
            log.warn("Тест {} упал (попытка {} из {}): {}",
                    context.getDisplayName(), attempt, retries + 1, lastFailure.getMessage());
            Allure.step(String.format("Повторная попытка %d из %d после ошибки: %s",
                    attempt + 1, retries + 1, lastFailure.getMessage()));
            resetPlaywrightContext(context);
            try {
                invokeTestMethod(context);
                log.info("Тест {} прошёл при повторной попытке {}",
                        context.getDisplayName(), attempt + 1);
                return;
            } catch (Throwable t) {
                lastFailure = t;
            }
        }
        log.error("Тест {} не прошёл после {} попыток", context.getDisplayName(), retries + 1);
        throw lastFailure;
    }

    /**
     * Читает количество повторов из системного свойства.
     *
     * @return количество повторных запусков (0 — повторы отключены)
     */
    private int resolveRetryCount() {
        String value = System.getProperty(RETRY_COUNT_PROPERTY);
        if (value == null) {
            return DEFAULT_RETRY_COUNT;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_COUNT;
        }
    }

    /**
     * Пересоздаёт контекст Playwright, если тест поддерживает пересоздание сессии.
     *
     * @param context контекст теста
     */
    private void resetPlaywrightContext(ExtensionContext context) {
        Object instance = context.getRequiredTestInstance();
        if (instance instanceof PlaywrightSessionReset resettable) {
            resettable.resetSession();
        }
    }

    /**
     * Вызывает метод теста повторно рефлексией.
     *
     * @param context контекст теста
     * @throws Throwable исключение, выброшенное методом теста
     */
    private void invokeTestMethod(ExtensionContext context) throws Throwable {
        Method method = context.getRequiredTestMethod();
        method.setAccessible(true);
        try {
            method.invoke(context.getRequiredTestInstance());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
