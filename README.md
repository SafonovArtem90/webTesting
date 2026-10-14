# webTesting — UI-автотесты (Playwright + JUnit 5)

Фреймворк UI-автотестов для приложения [saucedemo.com](https://www.saucedemo.com/): логин всех вариантов пользователей и пользовательские сценарии (каталог, корзина, оформление заказа).

- **12 независимых тестов**, запускаются параллельно (3 потока)
- **Page Object** с собственной фабрикой и внедрением зависимостей через **Spring Boot**
- **Повтор при падении** и **скриншоты при падении** — отдельными расширениями JUnit 5
- **Checkstyle** с обязательным Javadoc, **GitHub Actions** CI
- Настройки — через переменные окружения и `application.properties`

## Стек

| Компонент | Версия |
|---|---|
| Java | 17 (toolchain) |
| Gradle | 9.0.0 (wrapper) |
| Playwright (Java) | 1.63.0 |
| JUnit 5 (Jupiter) | 5.12.x (Spring Boot BOM) |
| Spring Boot (BOM, только тесты) | 3.5.16 |
| Lombok / Jackson | 1.18.48 / BOM |
| Checkstyle | 12.0.0 |
| Браузер | Chromium |

## Быстрый старт

```bash
# 1. Установить браузеры Playwright (однократно, той же версии, что и фреймворк)
./gradlew playwrightInstall

# 2. Запустить всё: тесты + чекстайл
./gradlew check
```

Только тесты:

```bash
./gradlew test
./gradlew test --tests "mywebtests.tests.LoginTest"
./gradlew test --tests "mywebtests.tests.LoginTest.loginWithStandardUser"
```

## Тесты

| Класс | Кол-во | Что проверяет |
|---|---|---|
| `LoginTest` | 4 | Успешный логин и выход (`standard_user`), сообщение об ошибке и блокировка (`locked_out_user`) |
| `UserBehaviorTest` | 8 | Сценарии `problem_user`, `performance_glitch_user`, `error_user`, `visual_user` по 2 теста |

Особенности поведения saucedemo, заложенные в проверки (проверено экспериментально):

| Пользователь | Проверка |
|---|---|
| `problem_user` | Все изображения товаров — общая заглушка `404` |
| `performance_glitch_user` | Каталог грузится медленно, но полностью (таймаут 30 с) |
| `error_user` | Finish → API отвечает 401, заказ не завершается, пользователь остаётся на обзоре |
| `visual_user` | Цены случайно искажены при каждой загрузке |
| `locked_out_user` | Ошибка логина, переход в каталог заблокирован |

Пароль всех пользователей — `secret_sauce` (хранится в `src/test/resources/data/users.json`, в коде не дублируется).

## Архитектура

```
тесты (tests/)
  → page object (pages/) — селекторы и пользовательские действия
    → core/ — сессия Playwright, фабрика page object, расширения JUnit
      → config/ + model/ + data/ — настройки, модели, тестовые данные
```

| Слой | Ключевые классы |
|---|---|
| База тестов | `BaseTest` — сессия Playwright на каждый тест (`@BeforeEach` / `@AfterEach`), регистрация расширений |
| Фабрика | `PageFactory` — создаёт page object через Spring-бин, ошибки оборачивает в `PageObjectCreationException` |
| Хранение | `WebDriverHolder` — `ThreadLocal`, потокобезопасность при параллельном запуске |
| Расширения | `ScreenshotOnFailureExtension` (`AfterTestExecutionCallback`) — скриншот до закрытия браузера; `RetryOnFailureExtension` — повтор с пересозданием сессии |
| Spring-контекст | `SaucedemoTestApplication` (`@SpringBootConfiguration`) + `@EnableConfigurationProperties(PlaywrightProperties.class)` |
| Page Object | `BasePage`, `LoginPage`, `InventoryPage`, `CartPage`, `CheckoutInformationPage`, `CheckoutOverviewPage` |
| Данные | `UserDataProvider` → `users.json` через Jackson; `User` / `UserData` / `CheckoutInfo` на Lombok |

Паттерны: Page Object + собственная фабрика, слоистая архитектура, Builder (модели), DI через Spring, разделение тестов по назначению.

## Конфигурация

`src/test/resources/application.properties`:

| Свойство | Значение по умолчанию | Переменная окружения |
|---|---|---|
| `playwright.base-url` | `https://www.saucedemo.com` | `PLAYWRIGHT_BASE_URL` |
| `playwright.browser` | `CHROMIUM` | `PLAYWRIGHT_BROWSER` (`CHROMIUM` / `FIREFOX` / `WEBKIT`) |
| `playwright.headless` | `true` | `PLAYWRIGHT_HEADLESS` |
| `playwright.timeout` | `10s` | `PLAYWRIGHT_TIMEOUT` |
| `testrun.retry-count` | `1` (итого 2 попытки) | `TESTRUN_RETRY_COUNT` |
| `reporting.screenshot-dir` | `build/screenshots` | `SCREENSHOT_DIR` |

Переменные окружения переопределяют значения файла (relaxed binding Spring Boot). Мосты для системных свойств JUnit — в `build.gradle.kts`.

## Параллельность

`src/test/resources/junit-platform.properties`: фиксированный пул из 3 потоков, классы и методы — параллельно. Каждый тест получает собственный `Page`, поэтому тесты независимы и порядок не важен.

## Скришоты и отчёты

- При падении теста в `build/screenshots/<имя-теста>_<время>.png` сохраняется скриншот страницы (каталог переопределяется `SCREENSHOT_DIR`).
- Отчёт Gradle: `build/reports/tests/test/index.html`.
- XML-результаты: `build/test-results/test/`.

## Код-стайл

Checkstyle 12.0.0, конфиг — `config/checkstyle/checkstyle.xml`: Javadoc над классами и методами обязателен, максимум 120 символов в строке, отступы 4 пробела, ноль предупреждений.

```bash
./gradlew checkstyleTest
```

## CI (GitHub Actions)

`.github/workflows/ui-tests.yml` — на push, pull request и вручную:

1. JDK 17 (temurin) с кешем Gradle
2. `./gradlew playwrightInstall` — браузеры той же версии, что и в тестах
3. `./gradlew check` — тесты + чекстайл
4. При падении — `build/screenshots` выгружается артефактом

## Возможные проблемы

| Проблема | Решение |
|---|---|
| `Executable did not launch browser` / нет браузера | `./gradlew playwrightInstall` |
| Ошибка «unsupported class file major version» | Нужна Java 17 (`java -version`) |
| Падает Checkstyle на новом файле | Обязательный Javadoc над классом и методами, строка ≤ 120 символов |
| Нет скриншота при падении | Проверьте `SCREENSHOT_DIR`; скриншот снимается до закрытия браузера в `@AfterEach` |
