package mywebtests.core;

/**
 * Исключение, возникающее при невозможности создать page-объект через фабрику.
 */
public class PageObjectCreationException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message текст ошибки
     * @param cause причина (ошибка рефлексии)
     */
    public PageObjectCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
