package utils.reporters;

import org.testng.ITestContext;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TelegramReporter {

    // ЗАМЕНИТЕ НА ВАШИ ДАННЫЕ
    private static final String BOT_TOKEN = "8392190074:AAEePUpivRQU67JRvEdEFWzeFq7n9Jym788";
    private static final String CHAT_ID = "1848447175"; // ваш chat_id

    /**
     * Отправляет отчет о результатах тестирования в Telegram
     */
    public static void sendTestReport(int passed, int failed, int skipped, long durationMs) {
        try {
            int total = passed + failed + skipped;
            double successRate = total > 0 ? (passed * 100.0 / total) : 0;

            String message = buildReportMessage(passed, failed, skipped, total, successRate, durationMs);
            sendTelegramMessage(message);

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки отчета в Telegram: " + e.getMessage());
        }
    }



    /**
     * Формирует красивое сообщение для Telegram
     */
    private static String buildReportMessage(int passed, int failed, int skipped, int total,
                                             double successRate, long durationMs) {

        String duration = formatDuration(durationMs);
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
        String statusEmoji = successRate >= 90 ? "🎉" : successRate >= 70 ? "⚠️" : "🚨";

        String message = statusEmoji + " <b>Yandex Tickets - Отчет о тестировании</b> " + statusEmoji + "\n\n" +
                "📊 <b>Статистика тестов:</b>\n" +
                "✅ Успешно: <b>" + passed + "</b>\n" +
                "❌ Упало: <b>" + failed + "</b>\n" +
                "⏸️ Пропущено: <b>" + skipped + "</b>\n" +
                "📋 Всего: <b>" + total + "</b>\n\n" +
                "🎯 <b>Успешность:</b> <code>" + String.format("%.1f", successRate) + "%%</code>\n\n" +
                "⏱️ <b>Время выполнения:</b>\n" +
                duration + "\n\n" +
                "📅 <b>Дата запуска:</b>\n" +
                date + "\n\n" +
                "<i>Автоматическое уведомление</i> 🧪";

        return message;
    }

    /**
     * Форматирует время выполнения
     */
    private static String formatDuration(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;

        if (minutes > 0) {
            return String.format("%d мин %d сек", minutes, seconds);
        } else {
            return String.format("%d сек", seconds);
        }
    }

    /**
     * Отправляет сообщение в Telegram через HTTP API
     */
    private static void sendTelegramMessage(String message) {
        try {
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String postData = "chat_id=" + CHAT_ID +
                    "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8) +
                    "&parse_mode=HTML";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ Telegram API вернул код: " + responseCode);
            } else {
                System.out.println("✅ Отчет отправлен в Telegram");
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки в Telegram: " + e.getMessage());
        }
    }

    /**
     * Отправляет простое уведомление
     */
    public static void sendSimpleMessage(String text) {
        try {
            String message = "🧪 <b>Yandex Tickets</b>\n\n" + text;
            sendTelegramMessage(message);
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки сообщения: " + e.getMessage());
        }
    }


    /**
     * Детальный отчет с информацией о каждом тесте
     */
    public static void sendDetailedReport(ITestContext context, long durationMs) {
        try {
            StringBuilder report = new StringBuilder();

            // Заголовок
            report.append("🧪 <b>Yandex Tickets - Детальный отчет</b>\n\n");

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

            // Упавшие тесты
            if (!context.getFailedTests().getAllResults().isEmpty()) {
                report.append("🚨 <b>Упавшие тесты:</b>\n");
                context.getFailedTests().getAllResults().forEach(result -> {
                    String testName = result.getMethod().getMethodName();
                    Throwable error = result.getThrowable();
                    report.append("• ").append(testName);
                    if (error != null) {
                        String errorMsg = error.getMessage();
                        if (errorMsg != null && errorMsg.length() > 50) {
                            errorMsg = errorMsg.substring(0, 50) + "...";
                        }
                        report.append(" - ").append(errorMsg).append("\n");
                    } else {
                        report.append("\n");
                    }
                });
                report.append("\n");
            }

            // Успешные тесты
            if (!context.getPassedTests().getAllResults().isEmpty()) {
                report.append("✅ <b>Успешные тесты:</b>\n");
                context.getPassedTests().getAllResults().forEach(result -> {
                    String testName = result.getMethod().getMethodName();
                    report.append("• ").append(testName).append("\n");
                });
                report.append("\n");
            }

            // Время и дата
            report.append("⏱️ <b>Время выполнения:</b>\n");
            report.append(formatDuration(durationMs)).append("\n\n");

            report.append("📅 <b>Дата запуска:</b>\n");
            report.append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date())).append("\n\n");

            report.append("<i>Автоматическое уведомление</i> 🧪");

            sendTelegramMessage(report.toString());

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки детального отчета: " + e.getMessage());
        }
    }
}