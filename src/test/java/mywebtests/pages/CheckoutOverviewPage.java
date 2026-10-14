package mywebtests.pages;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import mywebtests.core.PageFactory;

/**
 * Page Object страницы обзора заказа.
 */
public class CheckoutOverviewPage extends BasePage {

    /**
     * Создаёт page-объект страницы обзора заказа.
     *
     * @param pages фабрика page-объектов
     */
    public CheckoutOverviewPage(PageFactory pages) {
        super(pages);
    }

    @Override
    public void waitUntilLoaded() {
        PlaywrightAssertions.assertThat(page.locator(".title")).hasText("Checkout: Overview");
    }

    /**
     * Нажимает кнопку завершения оформления заказа.
     */
    @Step("Нажимает кнопку Finish")
    public void clickFinish() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Finish")).click();
    }

    /**
     * Возвращает контейнер сообщения об ошибке.
     *
     * @return локатор сообщения об ошибке
     */
    public Locator getErrorContainer() {
        return page.locator(".error-message-container");
    }
}
