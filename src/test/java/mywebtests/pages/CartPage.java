package mywebtests.pages;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import mywebtests.core.PageFactory;

/**
 * Page Object страницы корзины.
 */
public class CartPage extends BasePage {

    /**
     * Создаёт page-объект страницы корзины.
     *
     * @param pages фабрика page-объектов
     */
    public CartPage(PageFactory pages) {
        super(pages);
    }

    @Override
    public void waitUntilLoaded() {
        PlaywrightAssertions.assertThat(page.locator(".title")).hasText("Your Cart");
    }

    /**
     * Переходит к форме оформления заказа.
     *
     * @return страница данных доставки
     */
    @Step("Переходит к оформлению заказа")
    public CheckoutInformationPage checkout() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Checkout")).click();
        return pages.create(CheckoutInformationPage.class);
    }
}
