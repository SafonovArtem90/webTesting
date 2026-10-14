package mywebtests.pages;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import mywebtests.core.PageFactory;
import mywebtests.model.CheckoutInfo;

/**
 * Page Object страницы ввода данных доставки.
 */
public class CheckoutInformationPage extends BasePage {

    /**
     * Создаёт page-объект страницы данных доставки.
     *
     * @param pages фабрика page-объектов
     */
    public CheckoutInformationPage(PageFactory pages) {
        super(pages);
    }

    @Override
    public void waitUntilLoaded() {
        PlaywrightAssertions.assertThat(page.locator(".title")).hasText("Checkout: Your Information");
    }

    /**
     * Заполняет данные доставки и переходит к обзору заказа.
     *
     * @param info данные доставки
     * @return страница обзора заказа
     */
    @Step("Заполняет данные доставки и нажимает Continue")
    public CheckoutOverviewPage fillAndContinue(CheckoutInfo info) {
        page.locator("#first-name").fill(info.firstName());
        page.locator("#last-name").fill(info.lastName());
        page.locator("#postal-code").fill(info.postalCode());
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();
        return pages.create(CheckoutOverviewPage.class);
    }
}
