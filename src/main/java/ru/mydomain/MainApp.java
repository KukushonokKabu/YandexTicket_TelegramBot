package ru.mydomain;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MainApp {
    public static void main(String[] args) {
        try {
            RailwayBot bot = new RailwayBot("7160724468:AAFCIeGiqJmlhiZ3Ijndb9vR_Mv4dcCppHw");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            System.out.println("Бот запущен...");

            // Добавляем обработчик завершения работы для корректного закрытия браузера
            Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
}
