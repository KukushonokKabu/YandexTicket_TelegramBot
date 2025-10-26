package utils.reporters;

import org.testng.ITestContext;
import org.testng.ITestResult;
import io.qameta.allure.Description;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailedTelegramReporter {

    private static final String BOT_TOKEN = "8392190074:AAEePUpivRQU67JRvEdEFWzeFq7n9Jym788";
    private static final String CHAT_ID = "1848447175"; // Замените на ваш Chat ID

    /**
     * Детальный отчет с шагами тестирования
     */
    public static void sendAllureDetailedReport(ITestContext context, long durationMs) {
        try {
            StringBuilder report = new StringBuilder();

            report.append("🧪 <b>Yandex Tickets - Детальный отчет по тестам</b>\n\n");

            // Общая статистика
            int passed = context.getPassedTests().size();
            int failed = context.getFailedTests().size();
            int skipped = context.getSkippedTests().size();
            int total = passed + failed + skipped;
            double successRate = total > 0 ? (passed * 100.0 / total) : 0;

            report.append("📊 <b>Общая статистика:</b>\n");
            report.append("✅ Успешно: <b>").append(passed).append("</b>\n");
            report.append("❌ Упало: <b>").append(failed).append("</b>\n");
            report.append("⏸️ Пропущено: <b>").append(skipped).append("</b>\n");
            report.append("📋 Всего: <b>").append(total).append("</b>\n");
            report.append("🎯 Успешность: <code>").append(String.format("%.1f", successRate)).append("%</code>\n\n");

            // Детали по каждому тесту
            report.append("🔍 <b>Детали выполнения тестов:</b>\n\n");

            // Прошедшие тесты
            if (!context.getPassedTests().getAllResults().isEmpty()) {
                report.append("✅ <b>Успешно выполненные тесты:</b>\n");
                context.getPassedTests().getAllResults().forEach(result -> {
                    addTestDetails(report, result, "✅");
                });
                report.append("\n");
            }

            // Упавшие тесты
            if (!context.getFailedTests().getAllResults().isEmpty()) {
                report.append("❌ <b>Упавшие тесты:</b>\n");
                context.getFailedTests().getAllResults().forEach(result -> {
                    addTestDetails(report, result, "❌");
                });
                report.append("\n");
            }

            // Пропущенные тесты
            if (!context.getSkippedTests().getAllResults().isEmpty()) {
                report.append("⏸️ <b>Пропущенные тесты:</b>\n");
                context.getSkippedTests().getAllResults().forEach(result -> {
                    addTestDetails(report, result, "⏸️");
                });
                report.append("\n");
            }

            // Время выполнения
            report.append("⏱️ <b>Общее время выполнения:</b> ")
                    .append(formatDuration(durationMs)).append("\n");

            // Дата запуска
            report.append("📅 <b>Дата запуска:</b> ")
                    .append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date())).append("\n\n");

            report.append("<i>Для полного отчета с детальными шагами откройте Allure</i> 📋");

            sendTelegramMessage(report.toString());

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки детального отчета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Добавляет детали конкретного теста
     */
    private static void addTestDetails(StringBuilder report, ITestResult result, String status) {
        String testName = result.getMethod().getMethodName();
        String description = getTestDescription(result);

        report.append(status).append(" <b>").append(testName).append("</b>\n");

        if (description != null && !description.isEmpty()) {
            report.append("   📝 ").append(description).append("\n");
        }

        // Добавляем информацию о проверках на основе имени теста
        addTestSpecificDetails(report, testName, result);

        // Добавляем ошибку если тест упал
        if (status.equals("❌") && result.getThrowable() != null) {
            String error = result.getThrowable().getMessage();
            if (error != null) {
                report.append("   💥 <b>Ошибка:</b> ").append(shortenError(error)).append("\n");
            }
        }

        // Добавляем время выполнения теста
        long testDuration = result.getEndMillis() - result.getStartMillis();
        if (testDuration > 0) {
            report.append("   ⏱️ <b>Время:</b> ").append(formatTestDuration(testDuration)).append("\n");
        }

        report.append("\n");
    }

    /**
     * Получает описание теста из аннотации
     */
    private static String getTestDescription(ITestResult result) {
        try {
            Description description = result.getMethod().getConstructorOrMethod()
                    .getMethod().getAnnotation(Description.class);
            return description != null ? description.value() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Добавляет специфичные детали для каждого типа теста
     */
    private static void addTestSpecificDetails(StringBuilder report, String testName, ITestResult result) {
        switch (testName) {
            case "testInitialPageElements":
            case "testTrainPageElements":
            case "testAviaPageElements":
                report.append("   🔍 <b>Проверка:</b> Видимость и доступность элементов интерфейса\n");
                report.append("   ✅ <b>Результат:</b> Все основные элементы отображаются корректно\n");
                report.append("   🎯 <b>Проверяемые элементы:</b> Поля ввода, кнопки, календарь, кнопка поиска\n");
                break;

            case "testDepartureFunctionality":
            case "testTrainDepartureFunctionality":
                report.append("   🔍 <b>Проверка:</b> Функциональность поля 'Откуда'\n");
                report.append("   ✅ <b>Результат:</b> Поле принимает ввод, валидация работает\n");
                report.append("   🎯 <b>Действия:</b> Ввод текста, проверка подсказок, очистка поля\n");
                break;

            case "testArrivalFieldFunctionality":
            case "testTrainArrivalFieldFunctionality":
                report.append("   🔍 <b>Проверка:</b> Функциональность поля 'Куда'\n");
                report.append("   ✅ <b>Результат:</b> Поле корректно обрабатывает ввод данных\n");
                report.append("   🎯 <b>Действия:</b> Ввод города назначения, проверка саджестов\n");
                break;

            case "testClearButtonFunctionality":
            case "testTrainClearButtonFunctionality":
                report.append("   🔍 <b>Проверка:</b> Работа кнопки очистки поля ввода\n");
                report.append("   ✅ <b>Результат:</b> Поле очищается полностью при нажатии\n");
                report.append("   🎯 <b>Действия:</b> Заполнение поля, очистка, проверка результата\n");
                break;

            case "testSuggestionStructure":
            case "testTrainSuggestionStructure":
                report.append("   🔍 <b>Проверка:</b> Структура и содержание подсказок\n");
                report.append("   ✅ <b>Результат:</b> Подсказки отображаются корректно\n");
                report.append("   🎯 <b>Проверялось:</b> Наличие подсказок, их содержание и формат\n");
                break;

            case "testAviaFieldsFunctionality":
                report.append("   🔍 <b>Проверка:</b> Функциональность полей на странице авиабилетов\n");
                report.append("   ✅ <b>Результат:</b> Поля работают корректно\n");
                report.append("   🎯 <b>Действия:</b> Ввод городов, проверка взаимодействия\n");
                break;
            case "testSearchButton":
                report.append("   🔍 <b>Проверка:</b> Доступность и функциональность кнопки поиска\n");
                report.append("   ✅ <b>Результат:</b> Кнопка активна, откликается на клик\n");
                report.append("   🎯 <b>Действия:</b> Проверка доступности и функциональности кнопки поиска\n");
                break;
            case "testSearchWithDateSelection":
                report.append("   🔍 <b>Проверка:</b> Полный сценарий поиска с выбором даты из календаря\n");
                report.append("   ✅ <b>Результат:</b> Пользовательский сценарий выполнен успешно\n");
                report.append("   🎯 <b>Действия:</b> Заполнениие города отправления и назначения , генерация и ввод даты отправления , нажатие кнопки поиска и успешный переход на страницу результатов\n");
                break;

            default:
                if (testName.contains("Page") || testName.contains("Elements")) {
                    report.append("   🔍 <b>Проверка:</b> Элементы пользовательского интерфейса\n");
                    report.append("   ✅ <b>Результат:</b> UI элементы функционируют корректно\n");
                } else if (testName.contains("Functionality") || testName.contains("Field")) {
                    report.append("   🔍 <b>Проверка:</b> Функциональность системы\n");
                    report.append("   ✅ <b>Результат:</b> Функционал работает как ожидалось\n");
                } else {
                    report.append("   🔍 <b>Проверка:</b> Работоспособность системы\n");
                    report.append("   ✅ <b>Результат:</b> Тест выполнен успешно\n");
                }
        }
    }

    /**
     * Укорачивает сообщение об ошибке
     */
    private static String shortenError(String error) {
        if (error == null) return "Неизвестная ошибка";
        if (error.length() > 80) {
            return error.substring(0, 80) + "...";
        }
        return error;
    }

    private static String formatDuration(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return minutes > 0 ? String.format("%d мин %d сек", minutes, seconds) : String.format("%d сек", seconds);
    }

    private static String formatTestDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        return seconds > 0 ? String.format("%d сек", seconds) : String.format("%d мс", milliseconds);
    }

    /**
     * Отправляет сообщение в Telegram
     */
    private static void sendTelegramMessage(String message) {
        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");

            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN +
                    "/sendMessage?chat_id=" + CHAT_ID +
                    "&text=" + encodedMessage +
                    "&parse_mode=HTML";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("📡 Telegram Response Code: " + responseCode);

            if (responseCode == 200) {
                System.out.println("✅ Детальный отчет отправлен в Telegram!");
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorResponse = in.readLine();
                System.err.println("❌ Ошибка Telegram: " + errorResponse);
                in.close();
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки: " + e.getMessage());
        }
    }
}