package mywebtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import mywebtests.config.PlaywrightProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Конфигурация Spring-контекста для тестов.
 * Сканирует компоненты пакета mywebtests и подключает свойства Playwright.
 */
@SpringBootConfiguration
@EnableConfigurationProperties(PlaywrightProperties.class)
@ComponentScan
public class SaucedemoTestApplication {

    /**
     * Jackson ObjectMapper для чтения тестовых данных из JSON.
     *
     * @return настроенный ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
