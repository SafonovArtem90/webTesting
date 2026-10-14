package mywebtests.model;

import lombok.Builder;

/**
 * Учётные данные пользователя, загружаемые из data/users.json.
 * Доступен билдер Lombok для создания произвольных данных в тестах.
 *
 * @param username имя пользователя
 * @param password пароль
 * @param description описание поведения пользователя
 */
@Builder
public record UserData(String username, String password, String description) {
}
