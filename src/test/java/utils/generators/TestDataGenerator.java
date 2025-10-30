package utils.generators;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Класс для генерации сложных тестовых данных и дат.
 * Содержит логику работы с датами, случайными данными и сложными объектами
 * 
 * Отвечает за:
 *  - Генерацию дат в разных форматах
 *  - Созддание случайных данных для тестов
 *  - Подготовку данных для API запросов
 */
public class TestDataGenerator {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CALENDAR_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Генерируем дату отправления (завтра или позже)

    public static String getDepartureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return tomorrow.format(DISPLAY_FORMAT);
    }

    // Проверяем что дата не в прошлом
    public static boolean isValidFutureDate(String date) {
        try {
            LocalDate inputDate = LocalDate.parse(date, DISPLAY_FORMAT);
            return !inputDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    // Проверяем текущую дату в нужном формате
    public static String getCurrentDate() {
        return LocalDate.now().format(DISPLAY_FORMAT);
    }

    // Получаем завтрашнюю дату
    public static String getTomorrowDate() {
        return LocalDate.now().plusDays(1).format(DISPLAY_FORMAT);
    }

    // Генерируем случайную дату в указанном диапазоне
    public static String getRandomDate(int minDaysFromNow, int maxDaysFromNow) {
        LocalDate randomDate = LocalDate.now()
                .plusDays(ThreadLocalRandom.current().nextInt(minDaysFromNow, maxDaysFromNow + 1));
        return randomDate.format(DISPLAY_FORMAT);
    }

    // Генерируем дату для календаря (25-10-13)
    public static String getCalendarFormattedDate() {
        LocalDate date = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(3, 30));
        return date.format(CALENDAR_FORMAT);
    }

    // Генерируем xpath для календаря
    public static String getCalendarDayXpath(String date) {
        return String.format("//div[@data-qa='calendar-day-%s']", date);
    }

    // Автоматическая генерация xpath для дня календаря
    public static String getCalendarDayXpath() {
        String date = getCalendarFormattedDate();
        return getCalendarDayXpath(date);
    }

    /*
     * Генерирует данные для выбора даты в календаре
     * Возвращает xpath и отображаемую дату
     * 
     * @return Map с xpath локатором и отображаемой датой
     */
    public static Map<String, String> getDateAndXpath() {
        String date = getCalendarFormattedDate();
        String xpath = getCalendarDayXpath(date);

        Map<String, String> result = new HashMap<>();
        result.put("date", date);
        result.put("xpath", xpath);
        result.put("displayDate", LocalDate.parse(date).format(DISPLAY_FORMAT));

        return result;
    }
}
