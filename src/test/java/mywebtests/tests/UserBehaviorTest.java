package mywebtests.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import mywebtests.core.BaseTest;
import mywebtests.model.CheckoutInfo;
import mywebtests.model.User;
import mywebtests.pages.CheckoutOverviewPage;
import mywebtests.pages.InventoryPage;
import mywebtests.pages.LoginPage;
import org.junit.jupiter.api.Test;

/**
 * Поведенческие тесты для специальных пользователей demo-приложения:
 * problem_user, performance_glitch_user, error_user, visual_user.
 */
class UserBehaviorTest extends BaseTest {

    /** Расширенный таймаут для пользователя с производительностными глитчами. */
    private static final Duration GLITCH_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Эталонные цены стандартного пользователя: у visual_user они искажены.
     */
    private static final List<String> CANONICAL_PRICES =
            List.of("$29.99", "$9.99", "$15.99", "$49.99", "$7.99", "$15.99");

    /**
     * Problem-user: логин успешен, каталог отображается с названиями товаров.
     */
    @Test
    void problemUserLogsInAndSeesProductNames() {
        InventoryPage inventory = loginAs(User.PROBLEM);

        inventory.waitUntilLoaded();
        PlaywrightAssertions.assertThat(inventory.getItems()).hasCount(6);
        PlaywrightAssertions.assertThat(inventory.getProductNames()).hasCount(6);
        assertThat(inventory.getProductNames().first().textContent()).isNotEmpty();
    }

    /**
     * Problem-user: каждое изображение товара заменено на общую заглушку 404.
     */
    @Test
    void problemUserSeesPlaceholderImageForEveryProduct() {
        InventoryPage inventory = loginAs(User.PROBLEM);

        inventory.waitUntilLoaded();
        PlaywrightAssertions.assertThat(inventory.getProductImages()).hasCount(6);
        inventory.waitForProductImagesSettled();

        List<String> sources = inventory.getProductImageSources();
        assertThat(sources).hasSize(6).allMatch(source -> source.contains("404"));
    }

    /**
     * Performance-glitch-user: логин проходит медленно, но успешно.
     */
    @Test
    void performanceGlitchUserLogsInWithExtendedTimeout() {
        InventoryPage inventory = loginAs(User.PERFORMANCE_GLITCH);

        PlaywrightAssertions.assertThat(inventory.getTitle()).hasText("Products",
                new LocatorAssertions.HasTextOptions().setTimeout(GLITCH_TIMEOUT.toMillis()));
    }

    /**
     * Performance-glitch-user: товары появляются в каталоге с задержкой.
     */
    @Test
    void performanceGlitchUserEventuallySeesAllProducts() {
        InventoryPage inventory = loginAs(User.PERFORMANCE_GLITCH);

        PlaywrightAssertions.assertThat(inventory.getItems()).hasCount(6,
                new LocatorAssertions.HasCountOptions().setTimeout(GLITCH_TIMEOUT.toMillis()));
    }

    /**
     * Error-user: логин успешен и каталог отображается.
     */
    @Test
    void errorUserSuccessfullyLogsIn() {
        InventoryPage inventory = loginAs(User.ERROR);

        inventory.waitUntilLoaded();
        PlaywrightAssertions.assertThat(inventory.getItems()).hasCount(6);
    }

    /**
     * Error-user: после Finish внутренний API отвечает 401 — оформление
     * не завершается, пользователь остаётся на странице обзора заказа.
     */
    @Test
    void errorUserCheckoutFailsWithApiError() {
        InventoryPage inventory = loginAs(User.ERROR);
        inventory.waitUntilLoaded();
        inventory.addFirstItemToCart();
        PlaywrightAssertions.assertThat(inventory.getCartBadge()).hasText("1");

        CheckoutOverviewPage overview = inventory.openCart()
                .checkout()
                .fillAndContinue(CheckoutInfo.builder()
                        .firstName("Standard")
                        .lastName("User")
                        .postalCode("12345")
                        .build());
        overview.waitUntilLoaded();

        page.waitForConsoleMessage(
                new Page.WaitForConsoleMessageOptions()
                        .setPredicate(message -> message.text().contains("401"))
                        .setTimeout(GLITCH_TIMEOUT.toMillis()),
                overview::clickFinish);

        PlaywrightAssertions.assertThat(page)
                .hasURL(Pattern.compile(".*/checkout-step-two\\.html"));
    }

    /**
     * Visual-user: логин успешен и каталог отображается.
     */
    @Test
    void visualUserSuccessfullyLogsIn() {
        InventoryPage inventory = loginAs(User.VISUAL);

        inventory.waitUntilLoaded();
        PlaywrightAssertions.assertThat(inventory.getItems()).hasCount(6);
    }

    /**
     * Visual-user: цены товаров визуально искажены и отличаются от эталонных
     * (значения случайны при каждой загрузке страницы).
     */
    @Test
    void visualUserSeesDistortedPrices() {
        InventoryPage inventory = loginAs(User.VISUAL);

        inventory.waitUntilLoaded();
        List<String> prices = inventory.getProductPrices();
        assertThat(prices).hasSize(6);
        assertThat(prices).isNotEqualTo(CANONICAL_PRICES);
    }

    /**
     * Выполняет логин пользователя и возвращает страницу каталога.
     *
     * @param user вариант пользователя
     * @return страница каталога
     */
    @Step("Логин пользователя {user} и переход в каталог")
    private InventoryPage loginAs(User user) {
        LoginPage loginPage = pages.create(LoginPage.class).open();
        return loginPage.login(userDataProvider.get(user));
    }
}
