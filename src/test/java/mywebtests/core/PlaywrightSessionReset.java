package mywebtests.core;

/**
 * Контракт для пересоздания сессии Playwright между повторными запусками теста.
 */
public interface PlaywrightSessionReset {

    /**
     * Закрывает текущую сессию Playwright и создаёт новую.
     */
    void resetSession();
}
