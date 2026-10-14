package mywebtests.core.allure;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.TestResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Убирает из отчёта служебные метки, которые адаптер Allure добавляет
 * в каждый тест автоматически: внутренний ID JUnit, фреймворк и язык.
 *
 * <p>Метки {@code host} и {@code thread} намеренно остаются в отчёте:
 * из них Allure 3 строит дорожки вкладки Timeline, без них она пустая.
 *
 * <p>Перед удалением значения меток пишутся в лог запуска тестов, чтобы их
 * можно было посмотреть без отчёта. По умолчанию метки вырезаются из
 * результата; чтобы оставить их в отчёте ( режим отладки), включите
 * системное свойство {@code -Dallure.keepServiceLabels=true} или переменную
 * окружения {@code ALLURE_KEEP_SERVICE_LABELS=true}.
 *
 * <p>Регистрируется через ServiceLoader: файл
 * {@code META-INF/services/io.qameta.allure.listener.TestLifecycleListener}.
 */
public class ServiceLabelsFilter implements TestLifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLabelsFilter.class);

    /**
     * Служебные метки, не несущие смысла для теста и отчёта.
     *
     * <p>{@code host} и {@code thread} не входят: из них Allure 3 собирает
     * вкладку Timeline.
     */
    private static final Set<String> SERVICE_LABELS = Set.of(
            "junit.platform.uniqueid",
            "framework",
            "language");

    /** Системное свойство: не убирать служебные метки из отчёта. */
    private static final String KEEP_PROPERTY = "allure.keepServiceLabels";

    /** Переменная окружения: не убирать служебные метки из отчёта. */
    private static final String KEEP_ENV = "ALLURE_KEEP_SERVICE_LABELS";

    /**
     * Вызывается непосредственно перед записью результата теста в файл:
     * логирует служебные метки и удаляет их из результата,
     * если режим отладки не включён.
     *
     * @param result результат теста, который будет записан в allure-results
     */
    @Override
    public void beforeTestWrite(TestResult result) {
        List<Label> labels = result.getLabels();
        List<Label> serviceLabels = new ArrayList<>();
        for (Label label : labels) {
            if (SERVICE_LABELS.contains(label.getName())) {
                serviceLabels.add(label);
            }
        }
        if (serviceLabels.isEmpty()) {
            return;
        }
        if (keepServiceLabels()) {
            LOGGER.info("Служебные метки оставлены в отчёте ({}): {}", result.getName(), format(serviceLabels));
            return;
        }
        LOGGER.info("Служебные метки убраны из отчёта, значения из лога: {}", format(serviceLabels));
        labels.removeAll(serviceLabels);
    }

    /**
     * Форматирует метки в строку вида {@code имя=значение} для читаемого лога.
     *
     * @param serviceLabels метки для форматирования
     * @return строки «имя=значение» через запятую
     */
    private String format(List<Label> serviceLabels) {
        return serviceLabels.stream()
                .map(label -> label.getName() + "=" + label.getValue())
                .collect(Collectors.joining(", "));
    }

    /**
     * Проверяет, включён ли режим отладки со служебными метками в отчёте.
     *
     * @return {@code true}, если метки нужно оставить в отчёте
     */
    private boolean keepServiceLabels() {
        String value = System.getProperty(KEEP_PROPERTY, System.getenv().getOrDefault(KEEP_ENV, "false"));
        return Boolean.parseBoolean(value);
    }
}
