package utils.generators;
/*
 * Класс предоставляет готовые тестовые данные для автоматизации.
 * Содержит как статические константы, так и методы для генерации динамических данных.
 *
 * Использование:
 * - Статические данные для предсказуемых тестов
 * - Динамические данные для разнообразия сценариев
 * - Случайные маршруты для нагрузки и реалистичности
 */
import java.util.concurrent.ThreadLocalRandom;

public class TestData {
/*
 * Статические данные для базовых тестов
 * Используются в smoke-тестах и критических сценариях
 */
    public static final String DEPARTURE_CITY = "Москва";
    public static final String ARRIVAL_CITY = "Санкт-Петербург";
    public static final String TRAIN_NUMBER = "123A";
    public static final String CARRIAGE_TYPE = "плацкарт";
/*
 * Генерирует дату на завтра в формате dd.MM.yyyy
 * Используется для тестов с ближайшими датами
 *
 * @return строка с датой завтрашнего дня
 */
    public static String getTomorrowDate(){
        return TestDataGenerator.getTomorrowDate();
    }
/*
 * Генерирует случайную дату в указанном диапазоне
 * Используется для тестов с разными датами
 *
 * @param
 */
    public static String getRandomFutureDate(){
        return TestDataGenerator.getRandomDate(3,30);
    }

    // Альтернативные маршруты для разнообразия тестов
     public static class Routes{
        public static final String MOSCOW_KAZAN = "Казань";
        public static final String MOSCOW_NOVOSIBIRSK ="Новосибирск";
        public static final String MOSCOW_SOCHI ="Сочи";
        public static final String SPB_MOSCOW ="Москва";

    }
    // Случайный выбор маршрутов для тестов
    public static String getRandomRoute(){
        String[]routes = {Routes.MOSCOW_KAZAN,Routes.MOSCOW_NOVOSIBIRSK,Routes.MOSCOW_SOCHI};
        return routes[ThreadLocalRandom.current().nextInt(routes.length)];
    }

}
