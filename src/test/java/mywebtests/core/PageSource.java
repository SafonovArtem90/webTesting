package mywebtests.core;

import com.microsoft.playwright.Page;

/**
 * Даёт расширениям JUnit доступ к странице текущего теста
 * без глобальных ThreadLocal-холдеров.
 */
public interface PageSource {

    /**
     * Страница текущего теста.
     *
     * @return страница Playwright (доступна до закрытия сессии в @AfterEach)
     */
    Page getPage();
}
