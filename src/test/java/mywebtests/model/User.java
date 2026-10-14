package mywebtests.model;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Варианты пользователей demo-приложения Swag Labs.
 * Пароль для всех пользователей одинаковый и хранится в data/users.json.
 */
@Getter
@AllArgsConstructor
public enum User {

    /** Стандартный пользователь с полным доступом. */
    STANDARD("standard_user"),

    /** Заблокированный пользователь: логин отклоняется. */
    LOCKED_OUT("locked_out_user"),

    /** Пользователь с неработающими изображениями товаров. */
    PROBLEM("problem_user"),

    /** Пользователь с медленной загрузкой страниц. */
    PERFORMANCE_GLITCH("performance_glitch_user"),

    /** Пользователь с ошибками при завершении оформления заказа. */
    ERROR("error_user"),

    /** Пользователь с визуальными глитчами на изображениях товаров. */
    VISUAL("visual_user");

    /** Имя пользователя на стороне приложения. */
    private final String username;

    /**
     * Находит вариант пользователя по его имени.
     *
     * @param username имя пользователя в приложении
     * @return вариант пользователя
     * @throws IllegalArgumentException если пользователь неизвестен
     */
    public static User fromUsername(String username) {
        return Arrays.stream(values())
                .filter(user -> user.username.equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + username));
    }
}
