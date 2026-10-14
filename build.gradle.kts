plugins {
    id("java")
    id("checkstyle")
    id("io.qameta.allure") version "4.3.0"
}

group = "myWebTests"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot: тестовый контекст, инъекции, Jackson (версии управляются BOM)
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.16"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Launcher JUnit для TestExecutionListener (TestRunInfoListener); версия от BOM
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")

    // Браузерная автоматизация
    testImplementation("com.microsoft.playwright:playwright:1.63.0")

    // Lombok (только для компиляции тестов)
    testCompileOnly("org.projectlombok:lombok:1.18.48")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.48")

    // JUnit 5 (версия управляется spring-boot-dependencies)
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // Мостим только нужные свойства. Полный проброс System.getProperties() утаскивал бы
    // в тестовую JVM секреты демона (systemProp.* из gradle.properties) и мусор IDEA.
    // env переопределяет -D; -D переопределяет значение по умолчанию
    val retryCount = System.getenv("TESTRUN_RETRY_COUNT")
            ?: System.getProperty("testrun.retry-count")
            ?: "1"
    systemProperty("testrun.retry-count", retryCount)
    System.getProperty("allure.keepServiceLabels")?.let {
        systemProperty("allure.keepServiceLabels", it)
    }

    // Логи фреймворка (logback) видны в консоли при локальном запуске
    testLogging {
        showStandardStreams = true
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Результаты Allure не накапливаются между прогонами: каждый запуск
    // начинает с пустого build/allure-results, иначе отчёт склеит старые
    // и новые результаты по historyId и покажет устаревшие данные
    doFirst {
        delete(layout.buildDirectory.dir("allure-results"))
    }
}

allure {
    // Автоконфигурация Allure Java 2.35.5: JUnit Jupiter listener'ы и результаты
    // в build/allure-results подключаются автоматически, отчёт — ./gradlew allureReport
    adapter {
        autoconfigure.set(true)
        autoconfigureListeners.set(true)
        autoconfigureJavadocDescriptions.set(true)
    }
    report {
        // Allure 3: конфиг с историей трендов (build/allure-history/history.jsonl)
        configFile.set(layout.projectDirectory.file("config/allure/allurerc.mjs"))
    }
}

// Каталог отчёта очищается перед генерацией: Allure 3 (плагин Awesome) при
// повторном запуске в непустой каталог оставляет прошлый отчёт в корне,
// а новый складывает в подкаталог awesome/ вместе со старыми результатами
tasks.named("allureReport") {
    doFirst {
        delete(layout.buildDirectory.dir("reports/allure-report"))
    }
}

checkstyle {
    toolVersion = "12.0.0"
    configFile = file("$rootDir/config/checkstyle/checkstyle.xml")
    maxWarnings = 0
}

// Установка браузеров Playwright той же версии, что и в тестах: ./gradlew playwrightInstall
tasks.register<JavaExec>("playwrightInstall") {
    group = "verification"
    description = "Installs Playwright browsers matching the test framework version"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "--with-deps", "chromium", "firefox", "webkit")
}
