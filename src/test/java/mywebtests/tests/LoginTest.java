package mywebtests.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import mywebtests.core.BaseTest;
import mywebtests.model.User;
import mywebtests.pages.InventoryPage;
import mywebtests.pages.LoginPage;
import org.junit.jupiter.api.Test;

/**
 * Тесты логина: стандартный пользователь и заблокированный пользователь.
 */
class LoginTest extends BaseTest {

    /**
     * Стандартный пользователь успешно логинится и видит каталог товаров.
     */
    @Test
    void standardUserSuccessfullyLogsInAndSeesInventory() {
        InventoryPage inventory = pages.create(LoginPage.class)
                .open()
                .login(userDataProvider.get(User.STANDARD));

        inventory.waitUntilLoaded();
        assertThat(page.url()).contains("/inventory.html");
        PlaywrightAssertions.assertThat(inventory.getItems()).hasCount(6);
    }

    /**
     * Стандартный пользователь разлогинивается и возвращается на форму логина.
     */
    @Test
    void standardUserCanLogOutAndReturnToLoginPage() {
        LoginPage loginPage = pages.create(LoginPage.class)
                .open()
                .login(userDataProvider.get(User.STANDARD))
                .logout();

        loginPage.waitUntilLoaded();
        assertThat(loginPage.isLoginButtonVisible()).isTrue();
    }

    /**
     * Заблокированный пользователь видит сообщение о блокировке аккаунта.
     */
    @Test
    void lockedOutUserSeesLockoutErrorMessage() {
        LoginPage loginPage = pages.create(LoginPage.class)
                .open()
                .attemptLogin(userDataProvider.get(User.LOCKED_OUT));

        PlaywrightAssertions.assertThat(loginPage.getErrorContainer()).isVisible();
        assertThat(loginPage.getErrorMessage()).contains("Sorry, this user has been locked out");
    }

    /**
     * Заблокированный пользователь не получает доступ к каталогу:
     * прямой переход на страницу каталога перенаправляет на форму логина.
     */
    @Test
    void lockedOutUserCannotAccessInventory() {
        pages.create(LoginPage.class)
                .open()
                .attemptLogin(userDataProvider.get(User.LOCKED_OUT));

        page.navigate(properties.getBaseUrl() + "/inventory.html");
        LoginPage loginPage = pages.create(LoginPage.class);
        loginPage.waitUntilLoaded();
        assertThat(page.url()).doesNotContain("/inventory.html");
    }
}
