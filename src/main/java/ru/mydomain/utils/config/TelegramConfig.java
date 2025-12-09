package ru.mydomain.utils.config;

import com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * Универсальная конфигурация для Telegram ботов
 * Поддерживает основной бот и тестовый бот-репортер
 */
public class TelegramConfig {
    // Константы для основного бота
    public static final String MAIN_BOT_TOKEN = "TELEGRAM_BOT_TOKEN";
    public static final String MAIN_BOT_USERNAME = "TELEGRAM_BOT_USERNAME";
    public static final String MAIN_CHAT_ID = "TELEGRAM_CHAT_ID";

    // Константы для тестового бота-репортера
    public static final String TEST_BOT_TOKEN = "TEST_REPORTER_BOT_TOKEN";
    public static final String TEST_CHAT_ID = "TEST_REPORTER_CHAT_ID";

    // Имена файлов конфигурации
    private static final String MAIN_CONFIG_FILE = "telegram.properties";
    private static final String TEST_CONFIG_FILE = "test-telegram.properties";
    private static final String ENV_FILE = ".env";
    private static final String ENV_EXAMPLE_FILE = ".env.example";

    private static TelegramConfig instance;
    private final Properties mainProperties = new Properties();
    private final Properties testProperties = new Properties();
    private boolean isInitialed = false;

    private TelegramConfig(){
        initialize();
    }

    public static TelegramConfig getInstance(){
        if(instance == null){
            instance = new TelegramConfig();
        }
        return instance;
    }

    private void initialize(){
        try {
            // 1. Загружаем из .env файла
            loadFromEnvFile();

            // 2. Загружаем из properties файлов
            loadFromPropertiesFile(MAIN_CONFIG_FILE,mainProperties);
            loadFromPropertiesFile(TEST_CONFIG_FILE,testProperties);

            // 3. Загружаем из переменного окружения (имеет найвысший приоритет)
            overrideFromEnvironment();

            isInitialed = true;
        }catch (Exception e){
            System.err.println("Ошибка инициализации TelegramConfig: "+e.getMessage());
            printHelp();
        }
    }

    // Методы основного бота

    /**
     * Получить токен основного бота
     */
    public String getMainBotToken(){
        validateInitialization();
        String token = getValue(MAIN_BOT_TOKEN,mainProperties,"telegram.bot.token");

        if(token == null || token.isEmpty()){
            throw new IllegalStateException(
                    "Токен основного бота не найден.\n"+
                            "Укажите его одним из способов:\n"+
                            "1. Переменная окружения: "+ MAIN_BOT_TOKEN + "\n" +
                            "2. Файл .env: "+ MAIN_BOT_TOKEN + "=ваш_токен" +
                            "3. Файл telegram.properties: telegram.bot.token=ваш_токен"
            );
        }

        validateTokenFormat(token,"основного бота");
        return token;
    }

    /**
     * Получить имя основного бота
     */
    public String getMainBotUsername(){
        String userName = getValue(MAIN_BOT_USERNAME,mainProperties,"telegram.bot.username");
        if(userName == null || userName.isEmpty()){
            userName = generateBotName(getMainBotToken());
        }
        return userName;
    }

    /**
     * Получить chat ID для основного бота
     */
    public String getMainChatId(){
        return getValue(MAIN_CHAT_ID,mainProperties,"telegram.chat.id","");
    }

    // Методы для тестового бота-репортера

    /**
     * Получить токен тестового бота-репортера
     */
    public String getTestBotToken(){
        String token = getValue(TEST_BOT_TOKEN,testProperties,"telegram.bot.token");

        if(token == null || token.isEmpty()){
            // Для тестовго бота можно использовать основной токен как fallback
            token = getMainBotToken();
            System.out.println("Используется токен основного бота для тестов");
        }

        validateTokenFormat(token,"тестового бота");
        return token;
    }

    /**
     * Получить chat ID для тестового бота-репортера
     */
    public String getTestChatId(){
        String chatId = getValue(TEST_CHAT_ID,testProperties,"telegram.chat.id");

        if(chatId == null || chatId.isEmpty()){
            throw new IllegalStateException(
                    "Chat ID тестового бота не найден.\n" +
                            "Укажите его одним из способов:\n" +
                            "1. Переменная окружения: "+ TEST_CHAT_ID + "\n" +
                            "2. Файл .env: "+ TEST_CHAT_ID + "=ваш_chat_id\n" +
                            "3. Файл test-telegram.properties: telegram.chat.id=ваш_chat_id"
            );
        }
        return chatId;
    }

    // === Вспомогательные методы

    private String getValue(String envVar, Properties props,String propKey){
        return getValue(envVar,props,propKey,null);
    }

    private String getValue(String envVar,Properties props,String propKey,String defaultValue){
        // 1. Переменная окружения (найвысший приоритет)
        String value = System.getenv(envVar);
        if(value != null && !value.trim().isEmpty()){
            return value.trim();
        }

        //2. Системные свойства
        value = System.getProperty(envVar.toLowerCase().replace("_", "."));
        if(value != null && !value.trim().isEmpty()){
            return value.trim();
        }

        //3. Properties фвйл
        value = props.getProperty(propKey);
        if(value != null && value.trim().isEmpty()){
            return value.trim();
        }

        return defaultValue;
    }

    private void loadFromEnvFile(){
        Path envPath =  Paths.get(ENV_FILE);
        if(Files.exists(envPath)){
            try {
                String content = Files.readString(envPath);
                String[] lines = content.split("\n");

                for(String line : lines){
                    line = line.trim();
                    if(line.isEmpty() ||  line.startsWith("#")) continue;

                    String[] parts = line.split("=", 2);
                    if(parts.length == 2){
                        String key = parts[0].trim();
                        String value = parts[1].trim();

                        // Автоматически добавляем в переменные окружения для текущей сессии

                        if(!System.getenv().containsKey(key)){
                            System.setProperty(key.toLowerCase().replace("_","."),value);
                        }
                    }
                }
            }catch (IOException e){
                System.err.println("Не удалось прочитать .env файл: "+ e.getMessage());
            }
        }
    }

    private void loadFromPropertiesFile(String fileName,Properties targetProps){
        // Пробуем загрузить из classpath (для тестов)
        try(InputStream input = TelegramConfig.class.getClassLoader().getResourceAsStream(fileName)){
            if(input != null){
                targetProps.load(input);
                System.out.println("Загружена конфигурация из "+ fileName);
            }
        }catch (IOException e){
            System.err.println("Это не ошибка, это нормально: "+e.getMessage());
        }

        // Пробуем загрузить из корня проекта
        Path filePath = Paths.get(fileName);
        if(Files.exists(filePath)){
            try (InputStream input = Files.newInputStream(filePath)){
                Properties fileProps = new Properties();
                fileProps.load(input);

                //  Объединяем свойства
                fileProps.forEach((key,value) ->{
                    if(value != null && !value.toString().trim().isEmpty()){
                        targetProps.setProperty(key.toString(),value.toString().trim());
                    }
                });
                System.out.println("Загружена конфигурация из "+ filePath.toAbsolutePath());
            }catch (IOException e){
                System.err.println("Не удалось прочитать "+ fileName+ ": "+ e.getMessage());
            }
        }
    }

    private void overrideFromEnvironment(){
        // Переменные окружения переопределяют всё
        overrideFromEnv(MAIN_BOT_TOKEN,mainProperties,"telegram.bot.token");
        overrideFromEnv(MAIN_BOT_USERNAME,mainProperties,"telegram.bot.username");
        overrideFromEnv(MAIN_CHAT_ID,mainProperties,"telegram.chat.id");
        overrideFromEnv(TEST_BOT_TOKEN,testProperties,"telegram.bot.token");
        overrideFromEnv(TEST_CHAT_ID,testProperties,"telegram.chat.id");

    }

    private void overrideFromEnv(String envVar, Properties props,String propKey){
        String envValue = System.getenv(envVar);
        if(envValue != null && !envValue.trim().isEmpty()){
            props.setProperty(propKey,envValue.trim());
            System.out.println("Переопределение из переменной окружения: "+envVar);
        }
    }

    private void validateTokenFormat(String token, String botName) {
        // Telegram Bot Token формат: 1234567890:ABCdefGHIjklMNOpqrSTUvwxYZabcDEFghiJKL
        // ИЛИ: 1234567890:AAHdT2jM6Lc5fG8hJkL9pOqRstUvWxYzAbCdEf

        if (!token.matches("\\d{10}:[A-Za-z0-9_-]{35}")) {
            System.err.println("ВНИМАНИЕ: Нестандартный формат токена " + botName);
            System.err.println("Ожидается 46 символов в формате 1234567890:ABC...XYZ");
            System.err.println("Получено: " + maskToken(token));
        }

        // Проверка на тестовое значение
        if (token.toLowerCase().contains("example") ||
                token.toLowerCase().contains("test") ||
                token.toLowerCase().contains("demo") ||
                token.toLowerCase().contains("placeholder")) {
            System.err.println("ВНИМАНИЕ: Используется тестовый токен для " + botName);
        }
    }

    private void validateInitialization(){
        if(!isInitialed){
            throw new IllegalStateException("TelegramConfig не инициализирован");
        }
    }

    private String generateBotName(String token){
        if(token != null && token.contains(":")) {
            String numericPart = token.split(":")[0];
            return "Bot"+ numericPart.substring(0,Math.min(6,numericPart.length()));
        }
        return "TelegramBot";
    }

    // === Публичные утилиты ===

    public static String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 3) + "..." + token.substring(token.length() - 3);
    }

    public static void createEnvExample(){
        Path examplePath = Paths.get(ENV_EXAMPLE_FILE);
        String exampleContent =
                "# ===== Telegram Bot Configuration =====\n" +
                        "# Copy this file to .env and fill in your values\n" +
                        "# NEVER commit .env file to version controll\n\n" +

                        "# === Основной бот (для RailwayBot) === \n" +
                        "# Токен от @BotFather\n" +
                        MAIN_BOT_TOKEN + "=your_main_bot_token_here\n" +
                        "# Имя бота (опционально)\n" +
                        MAIN_BOT_USERNAME + "=YourMainBotName\n" +
                        "# Chat ID для уведомлений (опционально)\n" +
                        MAIN_CHAT_ID + "=123456789\n\n" +


                        "# === Тестовый бот-репортер ===\n" +
                        "# Токен тестового бота (можно использовать тот же)\n" +
                        TEST_BOT_TOKEN + "=your_test_bot_token_here\n" +
                        "# Chat ID для тестовых отчетов\n" +
                        TEST_CHAT_ID + "=987654321\n\n" +

                        "# === Другие настройки ===\n" +
                        "# DATABASE_URL=jdbc:postgresql://localhost:5432/db\n" +
                        "# API_KEY=your_api_key_here\n" +
                        "# LOG_LEVEL=DUBUG\n";

        try {
            if(!Files.exists(examplePath)){
                Files.writeString(examplePath,exampleContent);
                System.out.println("Создан файл "+ ENV_EXAMPLE_FILE);
                System.out.println("Скопируйте его в .env и заполните своими значениями");
            }
        }catch (IOException e){
            System.err.println("Не удалось создать .env.example: "+ e.getMessage());
        }
    }

    public static boolean containsSecrets(String text){
        if( text == null) return false;

        // Паттерны Telegram токена
        if(text.matches(".*\\d{8,10}:[A-Za-z0-9_-]{35}.*")){
            return true;
        }

        // Общие паттерны секретов
        String lowerText = text.toLowerCase();
        return lowerText.matches(".*(bot[._-]?token|api[._-]?key|secret|password).*=[^\\s]{10,}.*");
    }

    private void printHelp(){
        System.out.println("\n=== КОНФИГУРАЦИЯ TELEGRAM БОТОВ ===");
        System.out.println("Поддерживаемые источники конфигурации (в порядке приоритета)");
        System.out.println("1. Переменные окружения");
        System.out.println("2. Файл .env в корне проекта");
        System.out.println("3. Файлы properties в classpath или корне проекта");
        System.out.println("\nОбязательные параметры:");
        System.out.println("- "+ MAIN_BOT_TOKEN + " (основной бот)");
        System.out.println("- "+ TEST_CHAT_ID + " (тестовый бот)");
        System.out.println("\nСоздайте пример конфигурации:");
        System.out.println("TelegramConfig.createEnvExample();");
        System.out.println("====================================\n");
    }



}
