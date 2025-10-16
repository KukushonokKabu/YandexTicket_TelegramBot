package utils;

import java.util.concurrent.ThreadLocalRandom;

public class TestData {

    // Статические данные
    public static final String DEPARTURE_CITY = "Москва";
    public static final String ARRIVAL_CITY = "Санкт-Петербург";
    public static final String TRAIN_NUMBER = "123A";
    public static final String CARRIAGE_TYPE = "плацкарт";

    // Динамические данные генерируются при каждом обращении
    public static String getTomorrowDate(){
        return TestDataGenerator.getTomorrowDate();
    }
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
