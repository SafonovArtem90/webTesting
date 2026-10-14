package mywebtests.model;

import lombok.Builder;

/**
 * Данные для заполнения формы доставки при оформлении заказа.
 *
 * @param firstName имя
 * @param lastName фамилия
 * @param postalCode почтовый индекс
 */
@Builder
public record CheckoutInfo(String firstName, String lastName, String postalCode) {
}
