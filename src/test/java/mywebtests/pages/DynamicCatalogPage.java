package mywebtests.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import java.util.List;
import java.util.regex.Pattern;
import mywebtests.core.PageFactory;

/**
 * Page Object раздела Dynamic Catalog:
 * страницы со спиннером загрузки (Spinner) и ленивой подгрузкой (Lazy Load).
 */
public class DynamicCatalogPage extends BasePage {

    private static final String SPINNER_PATH = "/dynamic-catalog-spinner.html";
    private static final String LAZY_LOAD_PATH = "/dynamic-catalog-lazy-load.html";
    private static final Pattern TITLE_PATTERN = Pattern.compile("Dynamic Catalog.*");

    /**
     * Создаёт page-объект раздела Dynamic Catalog.
     *
     * @param pages фабрика page-объектов
     */
    public DynamicCatalogPage(PageFactory pages) {
        super(pages);
    }

    @Override
    public void waitUntilLoaded() {
        PlaywrightAssertions.assertThat(page.locator(".title")).hasText(TITLE_PATTERN);
    }

    /**
     * Открывает страницу каталога со спиннером загрузки.
     *
     * @return сама страница Dynamic Catalog
     */
    @Step("Открывает Dynamic Catalog — Spinner")
    public DynamicCatalogPage openSpinnerSection() {
        page.navigate(properties.getBaseUrl() + SPINNER_PATH);
        waitUntilLoaded();
        return this;
    }

    /**
     * Открывает страницу каталога с ленивой подгрузкой товаров.
     *
     * @return сама страница Dynamic Catalog
     */
    @Step("Открывает Dynamic Catalog — Lazy Load")
    public DynamicCatalogPage openLazyLoadSection() {
        page.navigate(properties.getBaseUrl() + LAZY_LOAD_PATH);
        waitUntilLoaded();
        return this;
    }

    /**
     * Возвращает индикатор загрузки спиннер-каталога (role=status, aria-label=Loading).
     *
     * @return локатор лоадера
     */
    public Locator getSpinnerLoader() {
        return page.locator("[data-test=\"dynamic-catalog-spinner\"]");
    }

    /**
     * Возвращает сетку товаров спиннер-каталога после загрузки.
     *
     * @return локатор сетки товаров
     */
    public Locator getSpinnerGrid() {
        return page.locator("[data-test=\"dynamic-catalog-spinner-grid\"]");
    }

    /**
     * Возвращает названия товаров в сетке спиннер-каталога.
     *
     * @return локатор названий товаров
     */
    public Locator getSpinnerItemNames() {
        return page.locator("[data-test$=\"-name\"]");
    }

    /**
     * Возвращает цены товаров в сетке спиннер-каталога.
     *
     * @return список отображаемых цен
     */
    public List<String> getSpinnerItemPrices() {
        return page.locator("[data-test$=\"-price\"]").allTextContents();
    }

    /**
     * Возвращает контейнер каталога с ленивой подгрузкой.
     *
     * @return локатор контейнера
     */
    public Locator getLazyContainer() {
        return page.locator("[data-test=\"dynamic-catalog-lazy-load-container\"]");
    }

    /**
     * Возвращает маркер подгрузки (sentinel) в конце списка Lazy Load.
     *
     * @return локатор sentinel-элемента
     */
    public Locator getLazySentinel() {
        return page.locator("[data-test=\"dynamic-catalog-lazy-load-sentinel\"]");
    }

    /**
     * Возвращает плейсхолдеры карточек, которые ещё загружаются («Loading…»).
     *
     * @return локатор плейсхолдеров
     */
    public Locator getLazyPlaceholders() {
        return page.locator("[data-test$=\"-placeholder\"]");
    }

    /**
     * Возвращает названия уже загруженных карточек Lazy Load.
     *
     * @return список названий товаров
     */
    public List<String> getLazyItemNames() {
        return page.locator("[data-test$=\"-name\"]").allTextContents();
    }

    /**
     * Прокручивает страницу к sentinel-элементу для запуска подгрузки следующей партии.
     */
    @Step("Прокручивает Lazy Load к sentinel-элементу")
    public void scrollLazySentinelIntoView() {
        getLazySentinel().scrollIntoViewIfNeeded();
    }

    /**
     * Явно ждёт, пока количество незагруженных плейсхолдеров станет меньше заданного,
     * то есть пока подгрузка новой партии товаров не уменьшит очередь ожидания.
     *
     * @param limit количество плейсхолдеров до начала ожидания
     */
    public void waitForPendingPlaceholdersBelow(int limit) {
        page.waitForFunction(
                "limit => document.querySelectorAll('[data-test$=\"-placeholder\"]').length < limit",
                limit,
                new Page.WaitForFunctionOptions().setTimeout(15000.0));
    }
}
