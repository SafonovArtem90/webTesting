package mywebtests.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Поведенческие тесты для специальных пользователей demo-приложения:
 * problem_user, performance_glitch_user, error_user, visual_user.
 */
@DisplayName("Поведенческие сценарии специальных пользователей")
class UserBehaviorTest extends BaseTest {

    /** Расширенный таймаут для пользователя с производительностными глитчами. */
    private static final Duration GLITCH_TIMEOUT = Duration.ofSeconds(30);

    /** Ограниченное ожидание навигации, которая для error_user не должна наступить. */
    private static final Duration COMPLETION_WAIT = Duration.ofSeconds(5);

    /**
     * Эталонные цены стандартного пользователя: у visual_user они искажены.
     */
    private static final List<String> CANONICAL_PRICES =
            List.of("$29.99", "$9.99", "$15.99", "$49.99", "$7.99", "$15.99");

    /**
     * Problem-user: логин успешен, каталог отображается с названиями товаров.
     */
    @DisplayName("Problem-user: логин и отображение названий товаров")
    @Severity(SeverityLevel.NORMAL)
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
    @DisplayName("Problem-user: каждое изображение товара — заглушка 404")
    @Severity(SeverityLevel.MINOR)
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
    @DisplayName("Performance-glitch-user: медленный логин с расширенным таймаутом")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void performanceGlitchUserLogsInWithExtendedTimeout() {
        InventoryPage inventory = loginAs(User.PERFORMANCE_GLITCH);

        PlaywrightAssertions.assertThat(inventory.getTitle()).hasText("Products",
                new LocatorAssertions.HasTextOptions().setTimeout(GLITCH_TIMEOUT.toMillis()));
    }

    /**
     * Performance-glitch-user: товары появляются в каталоге с задержкой.
     */
    @DisplayName("Performance-glitch-user: товары появляются в каталоге с задержкой")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void performanceGlitchUserEventuallySeesAllProducts() {
        InventoryPage inventory = loginAs(User.PERFORMANCE_GLITCH);

        PlaywrightAssertions.assertThat(inventory.getItems()).hasCount(6,
                new LocatorAssertions.HasCountOptions().setTimeout(GLITCH_TIMEOUT.toMillis()));
    }

    /**
     * Error-user: логин успешен и каталог отображается.
     */
    @DisplayName("Error-user: успешный логин и отображение каталога")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void errorUserSuccessfullyLogsIn() {
        InventoryPage inventory = loginAs(User.ERROR);

        inventory.waitUntilLoaded();
        PlaywrightAssertions.assertThat(inventory.getItems()).hasCount(6);
    }

    /**
     * Error-user: Finish не завершает оформление заказа — переход на страницу
     * завершения не происходит, пользователь остаётся на странице обзора заказа.
     * Проверка браузеро-агностичная: сообщение «401» в консоли — это телеметрия
     * Backtrace, которую видит только Chrome; в Firefox/WebKit таких сообщений нет.
     */
    @DisplayName("Error-user: оформление заказа не завершается, пользователь остаётся на обзоре")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void errorUserCheckoutDoesNotComplete() {
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

        overview.clickFinish();

        // Негативная проверка вместо sleep: явно ждём возможную навигацию на страницу
        // завершения с ограниченным таймаутом и ожидаем, что она не наступит
        assertThatThrownBy(() -> page.waitForURL(
                        Pattern.compile(".*/checkout-complete\\.html"),
                        new Page.WaitForURLOptions().setTimeout(COMPLETION_WAIT.toMillis())))
                .isInstanceOf(TimeoutError.class);

        PlaywrightAssertions.assertThat(page)
                .hasURL(Pattern.compile(".*/checkout-step-two\\.html"));
    }

    /**
     * Visual-user: логин успешен и каталог отображается.
     */
    @DisplayName("Visual-user: успешный логин и отображение каталога")
    @Severity(SeverityLevel.NORMAL)
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
    @DisplayName("Visual-user: цены товаров искажены относительно эталонных")
    @Severity(SeverityLevel.MINOR)
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
