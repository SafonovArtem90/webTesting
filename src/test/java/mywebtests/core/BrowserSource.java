package mywebtests.core;

import mywebtests.config.SupportedBrowser;

/**
 * Даёт расширениям JUnit доступ к браузеру текущего теста —
 * браузер выбирается в BaseTest и закрепляется за тестом на все попытки.
 */
public interface BrowserSource {

    /**
     * Браузер текущего теста.
     *
     * @return выбранный для теста браузер
     */
    SupportedBrowser currentBrowser();
}
