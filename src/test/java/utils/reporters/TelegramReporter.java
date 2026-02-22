package utils.reporters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mydomain.utils.config.TelegramConfig;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramReporter {
    private static final Logger log = LoggerFactory.getLogger(TelegramReporter.class);

    private static final TelegramConfig config = TelegramConfig.getInstance();
    private static final String BOT_TOKEN = config.getTestBotToken();
    private static final String CHAT_ID = config.getTestChatId();

    static {
        log.info("TelegramReporter инициализирован");
    }

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
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                log.debug("Сообщение отправлено в Telegram");
            } else {
                log.error("Ошибка Telegram API: {}", responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            log.error("Ошибка отправки в Telegram: {}", e.getMessage());
        }
    }

    public static void sendSimpleMessage(String text) {
        String message = "🧪 <b>Yandex Tickets</b>\n\n" + text;
        sendTelegramMessage(message);
    }

    public static void sendScreenshot(byte[] screenshot, String caption) {
        if (screenshot == null || screenshot.length == 0) {
            log.warn("Скриншот пустой, отправка пропущена");
            return;
        }

        try {
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream()) {
                // chat_id
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n").getBytes());
                os.write((CHAT_ID + "\r\n").getBytes());

                // caption
                if (caption != null && !caption.isEmpty()) {
                    os.write(("--" + boundary + "\r\n").getBytes());
                    os.write(("Content-Disposition: form-data; name=\"caption\"\r\n\r\n").getBytes());
                    os.write((caption + "\r\n").getBytes());
                }

                // photo
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"photo\"; filename=\"screenshot.png\"\r\n").getBytes());
                os.write(("Content-Type: image/png\r\n\r\n").getBytes());
                os.write(screenshot);
                os.write(("\r\n").getBytes());

                os.write(("--" + boundary + "--\r\n").getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                log.debug("Скриншот отправлен в Telegram");
            } else {
                log.error("Ошибка отправки скриншота: {}", responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            log.error("Ошибка отправки скриншота: {}", e.getMessage());
        }
    }
}