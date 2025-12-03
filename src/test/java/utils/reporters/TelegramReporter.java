package utils.reporters;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramReporter {

    private static final String BOT_TOKEN = TestTelegramConfig.getBotToken();
    private static final String CHAT_ID = TestTelegramConfig.getChatId();


    /**
     * Отправляет сообщение в Telegram через HTTP API
     */
    public static void sendTelegramMessage(String message) {
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
     * Отправляет скриншот в Telegram
     */
    public static void sendScreenshot(byte[] screenshot, String caption) {
        try {
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Создаем boundary для multipart/form-data
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream()) {
                // Добавляем chat_id
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n").getBytes());
                os.write((CHAT_ID + "\r\n").getBytes());

                // Добавляем caption (подпись к фото)
                if (caption != null && !caption.isEmpty()) {
                    os.write(("--" + boundary + "\r\n").getBytes());
                    os.write(("Content-Disposition: form-data; name=\"caption\"\r\n\r\n").getBytes());
                    os.write((caption + "\r\n").getBytes());
                }

                // Добавляем файл
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"photo\"; filename=\"screenshot.png\"\r\n").getBytes());
                os.write(("Content-Type: image/png\r\n\r\n").getBytes());
                os.write(screenshot);
                os.write(("\r\n").getBytes());

                // Завершаем boundary
                os.write(("--" + boundary + "--\r\n").getBytes());
            }

            int responseCode = conn.getResponseCode();
            System.out.println("📡 Telegram Photo Response Code: " + responseCode);

            if (responseCode == 200) {
                System.out.println("✅ Скриншот отправлен в Telegram!");
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки скриншота: " + e.getMessage());
        }
    }
}