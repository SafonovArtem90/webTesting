package mywebtests.core.extensions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mywebtests.core.PlaywrightSessionReset;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Расширение JUnit 5: повторяет упавший тест заданное число раз.
 * Перед каждым повтором сессия Playwright пересоздаётся, чтобы тест стартовал с чистой страницы.
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
            resetPlaywrightSession(context);
            try {
                invokeTestMethod(context);
                log.info("Тест {} прошёл при повторной попытке {}", context.getDisplayName(), attempt + 1);
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
     * Пересоздаёт сессию Playwright, если тест поддерживает сброс сессии.
     *
     * @param context контекст теста
     */
    private void resetPlaywrightSession(ExtensionContext context) {
        Object instance = context.getRequiredTestInstance();
        if (instance instanceof PlaywrightSessionReset resettable) {
            resettable.resetSession();
        }
    }

    /**
     * Вызывает метод теста повторно.
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
