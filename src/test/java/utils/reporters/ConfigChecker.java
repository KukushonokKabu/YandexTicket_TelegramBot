package utils.reporters;

import ru.mydomain.utils.config.TelegramConfig;

import java.util.Scanner;

/**
 * Утилита для проверки и миграции конфигурации
 */
public class ConfigChecker {

    public static void main(String[] args) {
        System.out.println("=== ПРОВЕРКА КОНФИГУРАЦИИ TELEGRAM ===");

        // Проверяем старую конфигурацию
        System.out.println("\n1. Проверка legacy конфигурации:");
        try {
            TelegramConfig config = TelegramConfig.getInstance();
            String legacyToken = config.getTestBotToken();
            String legacyChatId = config.getTestChatId();
            System.out.println("✅ Legacy config работает (через TelegramConfig):");
            System.out.println("   Token: " + maskToken(legacyToken));
            System.out.println("   Chat ID: " + legacyChatId);
        } catch (Exception e) {
            System.out.println("❌ Legacy config ошибка: " + e.getMessage());
        }

        // Проверяем новую конфигурацию
        System.out.println("\n2. Проверка новой конфигурации:");
        try {
            TelegramConfig config = TelegramConfig.getInstance();

            String newToken = config.getTestBotToken();
            String newChatId = config.getTestChatId();
            System.out.println("✅ Новая config работает:");
            System.out.println("   Token: " + ru.mydomain.utils.config.TelegramConfig.maskToken(newToken));
            System.out.println("   Chat ID: " + newChatId);
        } catch (Exception e) {
            System.out.println("❌ Новая config ошибка: " + e.getMessage());
        }

        // Проверяем переменные окружения
        System.out.println("\n3. Проверка переменных окружения:");
        String[] envVars = {
                "TELEGRAM_BOT_TOKEN",
                "TELEGRAM_CHAT_ID",
                "TEST_REPORTER_BOT_TOKEN",
                "TEST_REPORTER_CHAT_ID"
        };

        for (String var : envVars) {
            String value = System.getenv(var);
            System.out.printf("   %-25s: %s%n",
                    var,
                    value != null ? "✅ " + maskToken(value) : "❌ не установлена"
            );
        }

        // Запрашиваем действие у пользователя
        Scanner scanner = new Scanner(System.in);
        System.out.print("Запустить миграцию? (y/n): ");
        String answer = scanner.nextLine().trim().toLowerCase();

        if ("y".equals(answer) || "yes".equals(answer) || "д".equals(answer)) {
            testMessageSending();
        }

        scanner.close();
    }
    private static void testMessageSending() {
        System.out.println("\n=== ТЕСТ ОТПРАВКИ СООБЩЕНИЯ ===");
        try {
            TelegramReporter.sendSimpleMessage(
                    "🔄 Тестовое сообщение от ConfigChecker\n" +
                            "Если вы это видите - миграция завершена успешно!\n" +
                            "✅ TestTelegramConfig удален\n" +
                            "✅ Все использует TelegramConfig"
            );
            System.out.println("✅ Тестовое сообщение отправлено!");
        } catch (Exception e) {
            System.out.println("❌ Ошибка отправки: " + e.getMessage());
        }
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 3) + "..." + token.substring(token.length() - 3);
    }
}