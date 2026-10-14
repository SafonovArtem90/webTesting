package mywebtests.core;

/**
 * Число тестов в текущем прогоне, известное до их выполнения.
 * Заполняется {@link TestRunInfoListener} из TestPlan JUnit и используется
 * селектором браузеров, чтобы одиночный локальный запуск шёл на браузере
 * по умолчанию, а полный прогон — на паре случайных браузеров.
 */
public final class TestRunInfo {

    /** Число тестов в прогоне; -1 — ещё не известно. */
    private static volatile int testCount = -1;

    private TestRunInfo() {
    }

    /**
     * Запоминает число тестов, найденных в прогоне.
     *
     * @param count число тестовых методов в TestPlan
     */
    public static void recordTestCount(int count) {
        testCount = count;
    }

    /**
     * Проверяет, что в прогоне ровно один тест.
     *
     * @return {@code true} для одиночного запуска теста
     */
    public static boolean isSingleTestRun() {
        return testCount == 1;
    }
}
