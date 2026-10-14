package mywebtests.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import java.util.List;
import mywebtests.core.BaseTest;
import mywebtests.model.User;
import mywebtests.pages.DynamicCatalogPage;
import mywebtests.pages.InventoryPage;
import mywebtests.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты раздела Dynamic Catalog: индикатор загрузки (Spinner)
 * и ленивая подгрузка товаров (Lazy Load). Раздел доступен только после логина.
 */
@DisplayName("Dynamic Catalog: лоадеры каталога")
class DynamicCatalogTest extends BaseTest {

    /**
     * Пока спиннер-каталог загружается, отображается лоадер (role=status, aria-label=Loading);
     * после загрузки лоадер исчезает и появляется сетка из шести товаров.
     */
    @DisplayName("Spinner-каталог: лоадер виден во время загрузки, затем сетка товаров")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void spinnerLoaderIsShownUntilCatalogLoads() {
        loginAsStandardUser();
        DynamicCatalogPage catalog = pages.create(DynamicCatalogPage.class).openSpinnerSection();

        PlaywrightAssertions.assertThat(catalog.getSpinnerLoader()).isVisible();
        PlaywrightAssertions.assertThat(catalog.getSpinnerLoader()).not().isVisible();

        PlaywrightAssertions.assertThat(catalog.getSpinnerGrid()).isVisible();
        PlaywrightAssertions.assertThat(catalog.getSpinnerItemNames()).hasCount(6);
        List<String> prices = catalog.getSpinnerItemPrices();
        assertThat(prices).hasSize(6).allMatch(price -> price.startsWith("$"));
    }

    /**
     * Lazy Load каталога показывает плейсхолдеры «Loading…» для незагруженных карточек,
     * а прокрутка к sentinel-элементу запускает подгрузку следующей партии товаров.
     */
    @DisplayName("Lazy Load: прокрутка к sentinel подгружает следующую партию товаров")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void lazyLoadCatalogLoadsNextBatchOnSentinelScroll() {
        loginAsStandardUser();
        DynamicCatalogPage catalog = pages.create(DynamicCatalogPage.class).openLazyLoadSection();

        PlaywrightAssertions.assertThat(catalog.getLazyContainer()).isVisible();
        PlaywrightAssertions.assertThat(catalog.getLazySentinel()).isVisible();

        PlaywrightAssertions.assertThat(catalog.getLazyPlaceholders().first()).isVisible();
        PlaywrightAssertions.assertThat(catalog.getLazyPlaceholders().first()).hasText("Loading…");

        int pendingBeforeScroll = catalog.getLazyPlaceholders().count();
        catalog.scrollLazySentinelIntoView();
        catalog.waitForPendingPlaceholdersBelow(pendingBeforeScroll);

        List<String> names = catalog.getLazyItemNames();
        assertThat(names).isNotEmpty().allMatch(name -> !name.isBlank());
    }

    /**
     * Выполняет логин стандартного пользователя и возвращает страницу каталога.
     *
     * @return страница каталога после успешного логина
     */
    private InventoryPage loginAsStandardUser() {
        InventoryPage inventory = pages.create(LoginPage.class)
                .open()
                .login(userDataProvider.get(User.STANDARD));
        inventory.waitUntilLoaded();
        return inventory;
    }
}
