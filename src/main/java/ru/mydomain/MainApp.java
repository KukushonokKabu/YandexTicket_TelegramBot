package ru.mydomain;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.mydomain.utils.config.TelegramConfig;

public class MainApp {
    public static void main(String[] args) {
        try {
            // Создаем пример конфигурации
            TelegramConfig.createEnvExample();

            // Получаем конфигурацию
            TelegramConfig config = TelegramConfig.getInstance();
            String botToken = config.getMainBotToken();
            String botUsername = config.getMainBotUsername();

            System.out.println("Запуск основного бота: "+ botUsername);
            System.out.println("Токен: "+ TelegramConfig.maskToken(botToken));


            RailwayBot bot = new RailwayBot(botToken,botUsername);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            System.out.println("Бот запущен...");

            // Добавляем обработчик завершения работы для корректного закрытия браузера
            Runtime.getRuntime().addShutdownHook(new Thread(() ->{
                System.out.println("Завершение работы бота ...");
                bot.shutdown();
            }));
        }
        catch (TelegramApiException e){
            System.err.println("Ошибка Telegram API: "+ e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            System.err.println("Критическая ошибка: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
