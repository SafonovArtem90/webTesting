package mywebtests.pages;

import com.microsoft.playwright.Page;
import mywebtests.config.PlaywrightProperties;
import mywebtests.core.PageFactory;

/**
 * Базовый класс для всех page-объектов.
 * Хранит страницу, настройки и фабрику для создания дочерних page-объектов.
 */
public abstract class BasePage {

    protected final Page page;
    protected final PlaywrightProperties properties;
    protected final PageFactory pages;

    /**
     * Создаёт page-объект с контекстом текущей страницы.
     *
     * @param pages фабрика page-объектов
     */
    protected BasePage(PageFactory pages) {
        this.page = pages.getPage();
        this.properties = pages.getProperties();
        this.pages = pages;
    }

    /**
     * Ожидает загрузки страницы: проверяет признак, характерный для конкретной страницы.
     */
    public abstract void waitUntilLoaded();

    /**
     * Открывает базовый URL тестируемого приложения.
     */
    protected void navigateToBaseUrl() {
        page.navigate(properties.getBaseUrl());
    }
}
