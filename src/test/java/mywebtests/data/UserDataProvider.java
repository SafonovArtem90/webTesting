package mywebtests.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import mywebtests.model.User;
import mywebtests.model.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Провайдер тестовых данных пользователей из data/users.json.
 * Данные загружаются один раз при старте Spring-контекста и безопасны для параллельного чтения.
 */
@Component
public class UserDataProvider {

    private static final Logger log = LoggerFactory.getLogger(UserDataProvider.class);

    private static final String USERS_RESOURCE_PATH = "/data/users.json";

    private final ObjectMapper objectMapper;
    private List<UserData> users;

    /**
     * Создаёт провайдера с Jackson ObjectMapper из Spring-контекста.
     *
     * @param objectMapper объект маппера JSON
     */
    public UserDataProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Загружает пользователей из JSON при инициализации контекста.
     */
    @PostConstruct
    void loadUsers() {
        try (InputStream stream = UserDataProvider.class.getResourceAsStream(USERS_RESOURCE_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("Test data not found: " + USERS_RESOURCE_PATH);
            }
            users = objectMapper.readValue(stream, new TypeReference<>() {});
            log.info("Загружено тестовых пользователей из {}: {}", USERS_RESOURCE_PATH, users.size());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load test users", e);
        }
    }

    /**
     * Возвращает всех пользователей из тестовых данных.
     *
     * @return неизменяемый список пользователей
     */
    public List<UserData> findAll() {
        return List.copyOf(users);
    }

    /**
     * Возвращает данные для выбранного варианта пользователя.
     *
     * @param user вариант пользователя
     * @return учётные данные пользователя
     * @throws IllegalArgumentException если данных для пользователя нет
     */
    public UserData get(User user) {
        return users.stream()
                .filter(data -> data.username().equals(user.getUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No test data for user: " + user));
    }
}
