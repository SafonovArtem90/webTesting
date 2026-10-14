package mywebtests.pages;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import java.util.List;
import mywebtests.core.PageFactory;

/**
 * Page Object страницы каталога товаров.
 */
public class InventoryPage extends BasePage {

    /**
     * Создаёт page-объект страницы каталога.
     *
     * @param pages фабрика page-объектов
     */
    public InventoryPage(PageFactory pages) {
        super(pages);
    }

    @Override
    public void waitUntilLoaded() {
        PlaywrightAssertions.assertThat(page.locator(".inventory_list")).isVisible();
        PlaywrightAssertions.assertThat(page.locator(".title")).hasText("Products");
    }

    /**
     * Возвращает заголовок страницы.
     *
     * @return локатор заголовка
     */
    public Locator getTitle() {
        return page.locator(".title");
    }

    /**
     * Возвращает карточки товаров.
     *
     * @return локатор списка товаров
     */
    public Locator getItems() {
        return page.locator(".inventory_item");
    }

    /**
     * Возвращает названия товаров.
     *
     * @return локатор названий товаров
     */
    public Locator getProductNames() {
        return page.locator(".inventory_item_name");
    }

    /**
     * Возвращает изображения товаров.
     *
     * @return локатор изображений
     */
    public Locator getProductImages() {
        return page.locator(".inventory_item_img img");
    }

    /**
     * Возвращает бейдж количества товаров в корзине.
     *
     * @return локатор бейджа корзины
     */
    public Locator getCartBadge() {
        return page.locator(".shopping_cart_badge");
    }

    /**
     * Возвращает цены всех товаров в каталоге.
     *
     * @return список отображаемых цен
     */
    public List<String> getProductPrices() {
        return page.locator(".inventory_item_price").allTextContents();
    }

    /**
     * Возвращает абсолютные URL изображений товаров.
     *
     * @return список src изображений
     */
    public List<String> getProductImageSources() {
        @SuppressWarnings("unchecked")
        List<String> sources = (List<String>) getProductImages()
                .evaluateAll("images => images.map(image => image.src)");
        return sources;
    }

    /**
     * Ждёт, пока все изображения товаров завершат загрузку (успех или ошибку).
     */
    @Step("Ждёт завершения загрузки всех изображений товаров")
    public void waitForProductImagesSettled() {
        page.waitForFunction(
                "() => Array.from(document.querySelectorAll('.inventory_item_img img'))"
                        + ".every(image => image.complete)",
                null,
                new Page.WaitForFunctionOptions().setTimeout(20000.0));
    }

    /**
     * Добавляет первый товар из каталога в корзину.
     */
    @Step("Добавляет первый товар в корзину")
    public void addFirstItemToCart() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add to cart"))
                .first().click();
    }

    /**
     * Открывает корзину.
     *
     * @return страница корзины
     */
    @Step("Открывает корзину")
    public CartPage openCart() {
        page.locator(".shopping_cart_link").click();
        return pages.create(CartPage.class);
    }

    /**
     * Выполняет разлогин через боковое меню.
     *
     * @return страница логина
     */
    @Step("Выполняет разлогин через боковое меню")
    public LoginPage logout() {
        page.locator("#react-burger-menu-btn").click();
        page.locator("#logout_sidebar_link").click();
        return pages.create(LoginPage.class);
    }
}
