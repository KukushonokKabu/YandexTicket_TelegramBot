package utils.reporters;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestTelegramConfig {

    private static final Properties properties = new Properties();

    static {
        try(InputStream input = TestTelegramConfig.class.getClassLoader()
        .getResourceAsStream("test-telegram.properties")){
    if(input !=null){
        properties.load(input);
    }
        }
        catch (IOException e){
            System.err.println("Не удалось загрузить test-telegram.properties");
        }
    }

    public static String getBotToken(){
        // Проверяем переменную окружения
        String token = System.getenv("TEST_REPORTER_BOT_TOKEN");
        if(token != null  && !token.isEmpty()) return  token;

        // Проверяем системное свойство
        token = System.getenv("test.reporter.bot.token");
        if(token != null && !token.isEmpty()) return token;

        // Проверяем properties файл
        token = properties.getProperty("telegram.bot.token");
        if(token != null && !token.isEmpty()) return token;

        throw new IllegalStateException(
                "Токен тестового бота-репортера не найден.\n"+
                        "Добавьте в test-telegram.properties иои установите переменную окружения" +
                        "TEST_REPORTER_BOT_TOKEN"
        );
    }

    public static String getChatId(){
        String chatId = System.getenv("TEST_REPORTER_CHAT_ID");
        if(chatId != null && !chatId.isEmpty()) return chatId;

        chatId = System.getProperty("test.reporter.chat.id");
        if(chatId != null && !chatId.isEmpty()) return chatId;

        chatId = properties.getProperty("telegram.chat.id");
        if(chatId !=null && !chatId.isEmpty()) return chatId;

        throw new IllegalStateException("Chat ID тестового бота не найден");
    }
}
