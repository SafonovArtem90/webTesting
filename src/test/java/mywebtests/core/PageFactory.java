package mywebtests.core;

import com.microsoft.playwright.Page;
import mywebtests.config.PlaywrightProperties;
import mywebtests.pages.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Фабрика создания page-объектов для Playwright.
 */
public final class PageFactory {

    private static final Logger log = LoggerFactory.getLogger(PageFactory.class);

    private final Page page;
    private final PlaywrightProperties properties;

    /**
     * Создаёт фабрику для текущей страницы.
     *
     * @param page текущая страница браузера
     * @param properties настройки Playwright
     */
    public PageFactory(Page page, PlaywrightProperties properties) {
        this.page = page;
        this.properties = properties;
    }

    /**
     * Создаёт page-объект, внедряя контекст страницы через конструктор.
     *
     * @param pageObjectClass класс page-объекта с конструктором (PageFactory)
     * @param <T> тип page-объекта
     * @return новый экземпляр page-объекта
     */
    public <T extends BasePage> T create(Class<T> pageObjectClass) {
        try {
            T pageObject = pageObjectClass.getDeclaredConstructor(PageFactory.class).newInstance(this);
            log.debug("Создан page-объект: {}", pageObjectClass.getSimpleName());
            return pageObject;
        } catch (ReflectiveOperationException e) {
            throw new PageObjectCreationException(
                    "Не удалось создать page-объект: " + pageObjectClass.getName(), e);
        }
    }

    /**
     * Возвращает текущую страницу браузера.
     *
     * @return страница Playwright
     */
    public Page getPage() {
        return page;
    }

    /**
     * Возвращает настройки Playwright.
     *
     * @return свойства Playwright
     */
    public PlaywrightProperties getProperties() {
        return properties;
    }
}
