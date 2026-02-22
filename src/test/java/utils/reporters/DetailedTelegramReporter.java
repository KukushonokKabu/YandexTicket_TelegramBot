package utils.reporters;

import core.TestContext;
import org.testng.ITestContext;
import org.testng.ITestResult;
import io.qameta.allure.Description;
import pages.models.TrainInfo;
import ru.mydomain.utils.config.TelegramConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static utils.reporters.TelegramReporter.sendScreenshot;

public class DetailedTelegramReporter {

    private static final TelegramConfig config = TelegramConfig.getInstance();
    private static final String BOT_TOKEN = config.getTestBotToken();
    private static final String CHAT_ID = config.getTestChatId();
    private static final Logger log = LoggerFactory.getLogger(DetailedTelegramReporter.class);

    /**
     * Детальный отчет с шагами тестирования
     */
    public static void sendAllureDetailedReport(ITestContext context, long durationMs) {

        try {
            StringBuilder report = new StringBuilder();

            report.append("🧪 <b>Yandex Tickets - Отчет по тестам</b>\n\n");

            // Статистика
            int passed = context.getPassedTests().size();
            int failed = context.getFailedTests().size();
            int skipped = context.getSkippedTests().size();
            int total = passed + failed + skipped;
            double successRate = total > 0 ? (passed * 100.0 / total) : 0;

            report.append("📊 <b>Статистика:</b>\n");
            report.append("✅ Успешно: ").append(passed).append("\n");
            report.append("❌ Упало: ").append(failed).append("\n");
            report.append("⏸️ Пропущено: ").append(skipped).append("\n");
            report.append("📋 Всего: ").append(total).append("\n");
            report.append("🎯 Успешность: ").append(String.format("%.1f", successRate)).append("%\n\n");

            // Детали тестов
            report.append("🔍 <b>Детали:</b>\n\n");

            // Успешные тесты
            context.getPassedTests().getAllResults().forEach(result -> {
                addTestDetails(report, result, "✅");
            });

            // Упавшие тесты
            context.getFailedTests().getAllResults().forEach(result -> {
                addTestDetails(report, result, "❌");
            });

            // Пропущенные тесты
            context.getSkippedTests().getAllResults().forEach(result -> {
                addTestDetails(report, result, "⏸️");
            });

            // Время выполнения
            report.append("⏱️ <b>Время:</b> ").append(formatDuration(durationMs)).append("\n");
            report.append("📅 ").append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));

            TelegramReporter.sendTelegramMessage(report.toString());

        } catch (Exception e) {
            log.error("Ошибка отправки отчета", e);
        }
    }

    /**
     * Добавляет детали конкретного теста
     */
    private static void addTestDetails(StringBuilder report, ITestResult result, String status) {
        String testName = result.getMethod().getMethodName();

        report.append(status).append(" <b>").append(testName).append("</b>\n");

        // ВСЕГДА добавляем данные о поезде, если они есть
        TrainInfo trainInfo = TestContext.getTrainInfo();
        if (trainInfo != null) {
            report.append("   🚂 <b>Поезд:</b> ").append(trainInfo.getTrainNumber()).append("\n");
            report.append("   🚃 <b>Вагон:</b> ").append(trainInfo.getCarriageNumber()).append("\n");
            report.append("   💺 <b>Место:</b> ").append(trainInfo.getPlace()).append("\n");
            report.append("   💰 <b>Цена:</b> ").append(trainInfo.getPrice()).append("\n");
        }

        // Ошибка для упавших тестов
        if (status.equals("❌") && result.getThrowable() != null) {
            String error = result.getThrowable().getMessage();
            if (error != null) {
                report.append("   💥 ").append(shortenError(error)).append("\n");
            }
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
        // Получаем сохраненные данные теста
        TrainInfo trainInfo = core.TestContext.getTrainInfo();
        String testData = core.TestContext.getData();
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
                report.append("   🎯 <b>Действия:</b> Заполнение города отправления и назначения , выбор даты отправления , нажатие кнопки поиска, переход на страницу результатов,сбор результатов\n");

                if (trainInfo != null) {
                    report.append("\n");
                    report.append("   🎫 <b>Собранные данные:</b>\n");
                    report.append("   🚂 <b>Поезд:</b> ").append(trainInfo.getTrainNumber()).append("\n");
                    report.append("   📍 <b>Вагон:</b> ").append(trainInfo.getCarriageNumber()).append("\n");
                    report.append("   🪑 <b>Место:</b> ").append(trainInfo.getPlace()).append("\n");
                    report.append("   💰 <b>Цена:</b> ").append(trainInfo.getPrice()).append("\n");
                    report.append("   📅 <b>Отправление:</b> ").append(trainInfo.getDateDeparture()).append(" ").append(trainInfo.getDepartureTime()).append("\n");
                    report.append("   🏁 <b>Прибытие:</b> ").append(trainInfo.getDateArrival()).append(" ").append(trainInfo.getArrivalTime()).append("\n");
                    report.append("   ⏳ <b>В пути:</b> ").append(trainInfo.getTravelTime()).append("\n");
                    report.append("   \uD83D\uDE86 <b>Рейс:</b> ").append(trainInfo.getDepartureAndArrivalCity()).append("\n");
                    report.append("   \uD83C\uDFC1 <b>Станция отправления:</b> ").append(trainInfo.getDepartureStation()).append("\n");
                    report.append("   ✅ <b>Станция прибытия:</b> ").append(trainInfo.getArrivalStation()).append("\n");
                } else if (testData != null) {
                    report.append("\n");
                    report.append("   📋 <b>Данные теста:</b>\n");
                    report.append("   ").append(testData.replace("\n", "\n   ")).append("\n");
                }
                break;
            case "testSearchWithDateSelectionAndRetry":
                report.append("   🔍 <b>Проверка:</b> Полный сценарий поиска с выбором даты из календаря c retry\n");
                report.append("   ✅ <b>Результат:</b> Пользовательский сценарий выполнен успешно\n");
                report.append("   🎯 <b>Действия:</b> Заполнение города отправления и назначения , выбор даты отправления , нажатие кнопки поиска, переход на страницу результатов,сбор результатов\n");

                if (trainInfo != null) {
                    report.append("\n");
                    report.append("   🎫 <b>Собранные данные:</b>\n");
                    report.append("   🚂 <b>Поезд:</b> ").append(trainInfo.getTrainNumber()).append("\n");
                    report.append("   📍 <b>Вагон:</b> ").append(trainInfo.getCarriageNumber()).append("\n");
                    report.append("   🪑 <b>Место:</b> ").append(trainInfo.getPlace()).append("\n");
                    report.append("   💰 <b>Цена:</b> ").append(trainInfo.getPrice()).append("\n");
                    report.append("   📅 <b>Отправление:</b> ").append(trainInfo.getDateDeparture()).append(" ").append(trainInfo.getDepartureTime()).append("\n");
                    report.append("   🏁 <b>Прибытие:</b> ").append(trainInfo.getDateArrival()).append(" ").append(trainInfo.getArrivalTime()).append("\n");
                    report.append("   ⏳ <b>В пути:</b> ").append(trainInfo.getTravelTime()).append("\n");
                    report.append("   \uD83D\uDE86 <b>Рейс:</b> ").append(trainInfo.getDepartureAndArrivalCity()).append("\n");
                    report.append("   \uD83C\uDFC1 <b>Станция отправления:</b> ").append(trainInfo.getDepartureStation()).append("\n");
                    report.append("   ✅ <b>Станция прибытия:</b> ").append(trainInfo.getArrivalStation()).append("\n");
                } else if (testData != null) {
                    report.append("\n");
                    report.append("   📋 <b>Данные теста:</b>\n");
                    report.append("   ").append(testData.replace("\n", "\n   ")).append("\n");
                }
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

    private static String formatDuration(long ms) {
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        return min > 0 ? String.format("%d мин %d сек", min, sec) : String.format("%d сек", sec);
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

    /**
     * Детальный отчет с шагами тестирования и скриншотом
     */
    public static void sendAllureDetailedReportWithScreenshot(ITestContext context, long durationMs, byte[] screenshot) {
        try {
            // Сначала отправляем текстовый отчет
            sendAllureDetailedReport(context, durationMs);

            // Затем отправляем скриншот с данными
            if (screenshot != null && screenshot.length > 0) {
                // Получаем данные о поезде для подписи
                TrainInfo trainInfo =core.TestContext.getTrainInfo();
                String caption = buildScreenshotCaption(trainInfo, context, durationMs);

                // Отправляем скриншот
                sendScreenshot(screenshot, caption);
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки отчета со скриншотом: " + e.getMessage());
        }
    }

    /**
     * Создает подпись для скриншота
     */
    private static String buildScreenshotCaption(TrainInfo trainInfo, ITestContext context, long durationMs) {
        StringBuilder caption = new StringBuilder();

        caption.append("📸 Скриншот выбранного места\n\n");

        caption.append("🚨 === ВАЖНАЯ ИНФОРМАЦИЯ ===\n");
        caption.append("💵 Цена на скриншоте без постельного белья\n");
        caption.append("💳 Полная цена в отчете выше\n");

        if (trainInfo != null) {
            caption.append("Для деталей бронирования смотри текстовый отчет выше 👆\n\n");
        }

        // Статистика тестов
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        caption.append("📅 Дата: ").append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));

        return caption.toString();
    }
    /**
     * Отправляет полный отчет: сначала текст, потом скриншот (если есть)
     */
    public static void sendCompleteReport(ITestContext context, long durationMs, byte[] screenshot) {
        try {
            // Всегда отправляем текстовый отчет первым
            sendAllureDetailedReport(context, durationMs);

            // Если есть скриншот - отправляем его вторым сообщением с минимальной информацией
            if (screenshot != null && screenshot.length > 0) {
                // Ждем немного между сообщениями
                Thread.sleep(1000);

                String caption = buildScreenshotCaption(core.TestContext.getTrainInfo(), context, durationMs);
                sendScreenshot(screenshot, caption);
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки полного отчета: " + e.getMessage());
        }
    }
}