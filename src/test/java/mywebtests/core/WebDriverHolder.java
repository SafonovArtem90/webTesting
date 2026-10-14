package mywebtests.core;

import com.microsoft.playwright.Page;

/**
 * Потокобезопасное хранилище текущей страницы Playwright.
 * Расширения JUnit (скриншоты) читают страницу отсюда, так как не управляются Spring.
 */
public final class WebDriverHolder {

    private static final ThreadLocal<Page> CURRENT_PAGE = new ThreadLocal<>();

    private WebDriverHolder() {
        // Утилитарный класс, экземпляр не создаётся
    }

    /**
     * Сохраняет страницу текущего потока.
     *
     * @param page страница Playwright
     */
    public static void setPage(Page page) {
        CURRENT_PAGE.set(page);
    }

    /**
     * Возвращает страницу текущего потока.
     *
     * @return страница Playwright или null, если сессия не создана
     */
    public static Page getPage() {
        return CURRENT_PAGE.get();
    }

    /**
     * Очищает страницу текущего потока.
     */
    public static void clear() {
        CURRENT_PAGE.remove();
    }
}
