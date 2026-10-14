package mywebtests.pages;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import mywebtests.core.PageFactory;
import mywebtests.model.UserData;

/**
 * Page Object страницы логина Swag Labs.
 */
public class LoginPage extends BasePage {

    /**
     * Создаёт page-объект страницы логина.
     *
     * @param pages фабрика page-объектов
     */
    public LoginPage(PageFactory pages) {
        super(pages);
    }

    /**
     * Открывает страницу логина.
     *
     * @return сама страница логина
     */
    @Step("Открывает страницу логина")
    public LoginPage open() {
        navigateToBaseUrl();
        waitUntilLoaded();
        return this;
    }

    /**
     * Выполняет успешный логин и переходит к каталогу товаров.
     *
     * @param user учётные данные пользователя
     * @return страница каталога
     */
    @Step("Выполняет логин пользователя {user}")
    public InventoryPage login(UserData user) {
        return login(user.username(), user.password());
    }

    /**
     * Выполняет логин с указанными учётными данными.
     *
     * @param username имя пользователя
     * @param password пароль
     * @return страница каталога
     */
    @Step("Выполняет логин: {username}")
    public InventoryPage login(String username, String password) {
        enterCredentials(username, password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
        return pages.create(InventoryPage.class);
    }

    /**
     * Пытается выполнить логин, не переходя с формы (для негативных сценариев).
     *
     * @param user учётные данные пользователя
     * @return сама страница логина с сообщением об ошибке
     */
    @Step("Пытается выполнить логин пользователя {user}")
    public LoginPage attemptLogin(UserData user) {
        enterCredentials(user.username(), user.password());
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
        return this;
    }

    /**
     * Вводит учётные данные в форму логина.
     *
     * @param username имя пользователя
     * @param password пароль
     */
    private void enterCredentials(String username, String password) {
        page.locator("[data-test=\"username\"]").fill(username);
        page.locator("[data-test=\"password\"]").fill(password);
    }

    /**
     * Возвращает контейнер сообщения об ошибке.
     *
     * @return локатор сообщения об ошибке
     */
    public Locator getErrorContainer() {
        return page.locator(".error-message-container");
    }

    /**
     * Возвращает текст сообщения об ошибке.
     *
     * @return сообщение об ошибке
     */
    public String getErrorMessage() {
        return getErrorContainer().textContent();
    }

    /**
     * Проверяет, отображается ли кнопка логина.
     *
     * @return true, если форма логина отображена
     */
    public boolean isLoginButtonVisible() {
        return page.locator("[data-test=\"login-button\"]").isVisible();
    }

    @Override
    public void waitUntilLoaded() {
        PlaywrightAssertions.assertThat(page.locator("[data-test=\"login-button\"]")).isVisible();
    }
}
