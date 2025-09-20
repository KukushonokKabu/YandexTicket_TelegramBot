package ru.mydomain;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;


public class RailwayBot extends TelegramLongPollingBot {
    private final ConcurrentHashMap<Long,UserSession>userSessions = new ConcurrentHashMap<>();
    private final SeleniumService seleniumService;
    private final String botToken;

    public RailwayBot(String botToken){
        this.botToken = botToken;
        this.seleniumService = new SeleniumService();
        this.seleniumService.initDriver(false);
    }
    @Override
    public void onUpdateReceived(Update update){
        try {
            // Обработка нажатий на кнопки в сообщениях
            if(update.hasCallbackQuery()){
                handleCallbackQuery(update.getCallbackQuery());
                return;
            }

            // Обработка текстовых сообщений и команд
            if(update.hasMessage() && update.getMessage().hasText()){
                Long chatId = update.getMessage().getChatId();
                String messageText = update.getMessage().getText();

                // Получаем сессию пользователя (может быть null)
                UserSession session = userSessions.get(chatId);

                System.out.println("Received message: " + messageText + ", session: " + session);

                // Обработка команды /start (приоритетная)
                if("/start".equals(messageText)){
                    UserSession newSession = new UserSession(chatId);
                    newSession.setCurrentStep(UserSession.Step.WAITING_FOR_DEPARTURE);
                    userSessions.put(chatId, newSession);
                    sendWelcomeMessage(chatId);
                    return;
                }

                // Обработка остальных сообщений
                handleUserMessage(chatId, session, messageText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleUserMessage(Long chatId,UserSession session,String messageText){
        // Обработка команды /start
        if("/start".equals(messageText)){
            sendWelcomeMessage(chatId);
            UserSession newSession = new UserSession(chatId);
            newSession.setCurrentStep(UserSession.Step.WAITING_FOR_DEPARTURE);
            userSessions.put(chatId,newSession);
            return;
        }
        // Обработка команд с клавиатуры
        if("\uD83D\uDD0D Начать поиск".equals(messageText)){
            sendTextMessage(chatId,"Введите город отправления:",false);
            UserSession newSession = new UserSession(chatId);
            newSession.setCurrentStep(UserSession.Step.WAITING_FOR_DEPARTURE);
            userSessions.put(chatId,newSession);
            return;
        }
        if("\uD83D\uDD04 Сбросить поиск".equals(messageText)){
            userSessions.remove(chatId);
            sendTextMessage(chatId,"✅ Поиск успешно сброшен!\nНажми \"\uD83D\uDD0D Начать поиск\" для нового поиска.",true);
            return;
        }
        if("ℹ\uFE0F Помощь".equals(messageText)){
            sendTextMessage(chatId,"🤖 Помощь по боту:\n\n" +
                    "• 🔍 Начать поиск - новый поиск билетов\n" +
                    "• 🔄 Сбросить поиск - начать заново\n" +
                    "• ⚙️ Настройки - настройки бота\n\n" +
                    "Просто следуйте инструкциям бота!",true);
            return;
        }
        if("⚙\uFE0F Настройки".equals(messageText)){
            sendTextMessage(chatId,"⚙\uFE0F Настройки будут доступны в следующей версии!\nСейчас доступен поиск билетов \uD83D\uDE86",true);
            return;
        }
        // Если это не команда проверяем есть ли активная сессия
        if(session == null){
            sendTextMessage(chatId,"Нажмите \"\uD83D\uDD0D Начать поиск\" чтобы начать поиск билетов.",true);
            return;
        }
        // Обрабатываем сообщения в зависимости от текущего шага сессии
        switch (session.getCurrentStep()){
            case WAITING_FOR_DEPARTURE:
                try {
                    sendTextMessage(chatId, "Ищу варианты...", false);
                    // Сначала очищаем предыдущие значения
                    seleniumService.clearFields();
                    List<String> departureSuggestions = seleniumService.getStationSuggestionsDeparture(messageText);

                    if (departureSuggestions.isEmpty()) {
                        sendTextMessage(chatId, "Ничего не найдено. Попробуйте другой город.", false);
                    } else {
                        sendStationOptions(chatId, "Выберите город отправления:", departureSuggestions, "dep_");
                        session.setCurrentStep(UserSession.Step.WAITING_FOR_DEPARTURE_SELECTION);
                        userSessions.put(chatId, session);
                    }
                    return;
                }

                catch (Exception e){
                    sendTextMessage(chatId,"Произошла ошибка при поиске станции. Попробуйте еще раз.",false);
                    e.printStackTrace();
                }
                break;

            case WAITING_FOR_ARRIVAL:
                try {
                    sendTextMessage(chatId,"Ищу варианты прибытия...",false);
                    List<String>arrivalSuggestions = seleniumService.getStationSuggestionsArrival(messageText);
                    if(arrivalSuggestions.isEmpty()){
                        sendTextMessage(chatId,"Ничего не найдено. Попробуйте другой город.",false);
                    }
                    else sendStationOptions(chatId,"Выберите город прибытия:",arrivalSuggestions,"arr_");
                    session.setCurrentStep(UserSession.Step.WAITING_FOR_ARRIVAL_SELECTION);
                    userSessions.put(chatId,session);
                }
                catch (Exception e){
                    sendTextMessage(chatId,"Произошла ошибка при поиске станций. Попробуйте еще раз.",false);
                    e.printStackTrace();
                }
                break;

            default:
                sendTextMessage(chatId,"Я не понимаю эту команду.Нажмите \"\uD83D\uDD0D Начать поиск\" чтобы начать.",true);

        }

    }
    private void handleCallbackQuery(CallbackQuery callbackQuery){
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        UserSession session = userSessions.get(chatId);

        if (session == null) {
            answerCallbackQuery(callbackQuery, "Сессия истекла. Начните поиск заново.");
            return;
        }
// Кайкой-то коммент для комита
        // Ага здесь что-то поменял и все в шоке
        if(data.startsWith("dep_")){
            String selectionStation = data.substring("dep_".length());
            session.setDepartureStation(selectionStation);
            session.setCurrentStep(UserSession.Step.WAITING_FOR_ARRIVAL);
            userSessions.put(chatId,session);

            try{
                // Очищаем поле и вводим выбранную станцию
                seleniumService.selectStation(selectionStation,true);

                editMessageWithNewText(callbackQuery,"Выбрано отправление: "+ selectionStation+ "\nТеперь введите город прибытия.");
            }
            catch (Exception e){
                editMessageWithNewText(callbackQuery,"❌ Ошибка при выборе станции. Попробуйте еще раз.");
                e.printStackTrace();
            }

        }
        else if (data.startsWith("arr_")) {
            String selectedStation = data.substring("arr_".length());
            session.setArrivalStation(selectedStation);
            session.setCurrentStep(UserSession.Step.WAITING_FOR_DATE);
            session.setCurrentCalendarMonth(LocalDate.now().withDayOfMonth(1));
            userSessions.put(chatId,session);

            try {
                // Очищаем поле и вводим выбранную станцию
                seleniumService.selectStation(selectedStation,false);

                editMessageWithNewText(callbackQuery,"Выбрано прибытие: "+selectedStation);
                sendCalendar(chatId,"Выберите дату поездки:",session);
            }
            catch (Exception e){
                editMessageWithNewText(callbackQuery,"❌ Ошибка при выборе станции. Попробуйте еще раз.");
                e.printStackTrace();
            }

        }
        else if (data.startsWith("date_")) {
            String selectedDate = data.substring("date_".length());
            session.setDate(selectedDate);
            session.setCurrentStep(UserSession.Step.COMPLETED);
            userSessions.put(chatId,session);

            editMessageWithNewText(callbackQuery,"Выбрана дата: "+ selectedDate+ "\n\nМаршрут: "+ session.getDepartureStation()+ "→"+ session.getArrivalStation()+ "\nДата:"+ selectedDate+ "\n\nИщу билеты...");
            searchTicket(session);
        }
        else if (data.startsWith("calendar_prev_")) {
            String dateStr = data.substring("calendar_prev_".length());
            LocalDate currentMonth = LocalDate.parse(dateStr);
            LocalDate prevMonth = currentMonth.minusMonths(1);

            LocalDate minDate = LocalDate.now().minusMonths(3);
            if(prevMonth.isBefore(minDate.withDayOfMonth(1))){
                answerCallbackQuery(callbackQuery,"Нельзя выбрать даты старше 3 месяцев назад");
                return;
            }
            session.setCurrentCalendarMonth(prevMonth);
            updateCalendarMessage(callbackQuery,"Выберите дату поездки:",session);
        }
        else if (data.startsWith("calendar_next_")) {
            String dateStr = data.substring("calendar_next_".length());
            LocalDate currentMonth = LocalDate.parse(dateStr);
            LocalDate nextMonth = currentMonth.plusMonths(1);

            LocalDate maxDate = LocalDate.now().plusMonths(3);
            if(nextMonth.isAfter(maxDate.withDayOfMonth(1))){
                answerCallbackQuery(callbackQuery,"Нельзя выбрать даты больше чем на 3 месяца вперед.");
                return;
            }
            session.setCurrentCalendarMonth(nextMonth);
            updateCalendarMessage(callbackQuery,"Выберите дату поездки:",session);
        }
        else if (data.equals("back_to_cities")) {
            session.setCurrentStep(UserSession.Step.WAITING_FOR_ARRIVAL);
            editMessageWithNewText(callbackQuery,"Выберите город прибытия:");
        }
        else if (data.equals("ignore")) {
            answerCallbackQuery(callbackQuery,"Эта дата недоступна для выбора");
        }
    }
    private void sendWelcomeMessage(Long chatId){
        String welcomeText = "Привет! Я помогу найти железнодорожные билеты.\n"+"Нажми \"\uD83D\uDD0D Начать поиск\"чтобы начать или введи город отправления:";
        SendMessage message  = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(welcomeText);
        message.setReplyMarkup(createMainKeyboard());// добавляем клавиатуру

        try {
            execute(message);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    private void sendStationOptions(Long chatId, String text,List<String>options,String callbackPrefix){
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>rows = new ArrayList<>();

        for(String option: options){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(option);
            button.setCallbackData(callbackPrefix + option);

            List<InlineKeyboardButton>row  = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }
        keyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    private void sendTextMessage(Long chatId,String text,boolean showKeyboard){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        if(showKeyboard){
            message.setReplyMarkup(createMainKeyboard());
        }
        else {
            message.setReplyMarkup(removeKeyboard());
        }

        try {
            execute(message);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    //Перегрузка метода для обратной совместимости
    private void sendTextMessage(Long chatId,String text){
        sendTextMessage(chatId,text,true);//По умолчанию показываем клавиатуру
    }
    private void editMessageWithNewText(CallbackQuery callbackQuery,String newText){
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessage.setText(newText);

        try {
            execute(editMessage);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername(){
        return "YandexTicketBot";
    }
    @Override
    public String getBotToken(){
        return botToken;
    }
    public void shutdown(){
        seleniumService.quitDriver();
    }
    private void sendCalendar(Long chatId,String message,UserSession session){
        SendMessage calendarMessage = new SendMessage();
        calendarMessage.setChatId(chatId.toString());
        calendarMessage.setText(message);

        // Получаем текущую дату
        LocalDate firstDayOfMonth = session.getCurrentCalendarMonth();
        if(firstDayOfMonth == null){
            firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            session.setCurrentCalendarMonth(firstDayOfMonth);
        }

        // Создаем клавиатуру для календаря
        InlineKeyboardMarkup keyboardMarkup = createCalendarKeyboard(firstDayOfMonth);
        calendarMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(calendarMessage);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    private void answerCallbackQuery(CallbackQuery callbackQuery,String text){
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQuery.getId());
        answer.setText(text);
        answer.setShowAlert(false);

        try {
            execute(answer);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    private void searchTicket(UserSession session){
        // Заглушка - здесь будет реальный поиск билетов через Selenium
        try {
            Thread.sleep(2000);// Имитация поиска

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(session.getChatId()));
            message.setText("Поиск завершен! По вашему запросу найдено много билетов.\n"+ "Используйте кнопки ниже для новых действий.");
            message.setReplyMarkup(createMainKeyboard());// Возвращаем клавиатуру
            execute(message);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private ReplyKeyboardMarkup createMainKeyboard(){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);//Клавиатура постоянная

        List<KeyboardRow>keyboard = new ArrayList<>();

        // Первая строка
        KeyboardRow row1 = new KeyboardRow();
        row1.add("\uD83D\uDD0D Начать поиск");
        row1.add("\uD83D\uDD04 Сбросить поиск");

        //  Вторая строка
        KeyboardRow row2 = new KeyboardRow();
        row2.add("ℹ\uFE0F Помощь");
        row2.add("⚙\uFE0F Настройки");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
    //Метод для скрытия клавитуры
    private ReplyKeyboardRemove removeKeyboard(){
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        return keyboardRemove;
    }
    private InlineKeyboardMarkup createCalendarKeyboard(LocalDate firstDayOfMonth){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>rows = new ArrayList<>();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy",new Locale("ru"));
        List<InlineKeyboardButton>headerRow = new ArrayList<>();

        // Кнопка предыдущий месяц

        InlineKeyboardButton prevButton = new InlineKeyboardButton();
        prevButton.setText("◀\uFE0F");
        prevButton.setCallbackData("calendar_prev_"+ firstDayOfMonth.format(DateTimeFormatter.ISO_DATE));
        headerRow.add(prevButton);
        // Текущий месяц
        InlineKeyboardButton monthButton = new InlineKeyboardButton();
        monthButton.setText(firstDayOfMonth.format(monthFormatter));
        monthButton.setCallbackData("ignore");
        headerRow.add(monthButton);

        // Кнопка следующий месяц
        InlineKeyboardButton nextButton = new InlineKeyboardButton();
        nextButton.setText("▶\uFE0F");
        nextButton.setCallbackData("calendar_next_"+ firstDayOfMonth.format(DateTimeFormatter.ISO_DATE));
        headerRow.add(nextButton);
        rows.add(headerRow);

        // Дни недели
        List<InlineKeyboardButton> daysOfWeekRow = new ArrayList<>();
        String[]daysOfWeek = {"Пн","Вт","Ср","Чт","Пт","Сб","Вс"};
        for(String day : daysOfWeek){
            InlineKeyboardButton dayButton = new InlineKeyboardButton();
            dayButton.setText(day);
            dayButton.setCallbackData("ignore");
            daysOfWeekRow.add(dayButton);
        }
        rows.add(daysOfWeekRow);

        // Даты месяца
        LocalDate today = LocalDate.now();
        int daysInMonth = firstDayOfMonth.lengthOfMonth();
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        List<InlineKeyboardButton>currentRow = new ArrayList<>();

        // Пустые кнопки для первых дней
        for(int i = 1;i<dayOfWeek;i++){
            InlineKeyboardButton emptyButton = new InlineKeyboardButton();
            emptyButton.setText(" ");
            emptyButton.setCallbackData("ignore");
            currentRow.add(emptyButton);
        }

        // Заполняем даты
        for(int day = 1;day<=daysInMonth;day++){
            LocalDate currentDate = firstDayOfMonth.withDayOfMonth(day);

            InlineKeyboardButton dateButton = new InlineKeyboardButton();

            // Проверяем что дата не больше 3 месяцев вперед
            LocalDate maxDate = today.plusMonths(3);

            if(currentDate.isBefore(today)|| currentDate.isAfter(maxDate)){
                // Прошедшая дата или больше 3 месяцев  - неактивная
                dateButton.setText("✖\uFE0F");
                dateButton.setCallbackData("ignore");
            }
            else {
                // Активная дата
                 dateButton.setText(String.valueOf(day));

                 //  Форматируем дату для callback
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                String formattedDate = currentDate.format(formatter);
                dateButton.setCallbackData("date_"+formattedDate);

                // Подсветка сегодняшнего дня
                if(currentDate.equals(today)){
                    dateButton.setText("\uD83D\uDCCD "+ day);
                }
            }
            currentRow.add(dateButton);

            // Если строка заполнена (7 дней), начинаем новую
            if(currentRow.size() == 7){
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }
        // Добавляем оставшиеся пустые кнопки
        while (!currentRow.isEmpty() && currentRow.size() < 7){
            InlineKeyboardButton emptyButton = new InlineKeyboardButton();
            emptyButton.setText(" ");
            emptyButton.setCallbackData("ignore");
            currentRow.add(emptyButton);
        }
        if(!currentRow.isEmpty()){
            rows.add(currentRow);
        }
        // Кнопка возврата к выбору городов
        List<InlineKeyboardButton>backRow = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("\uD83D\uDCCD Вернуться к выбору городов");
        backButton.setCallbackData("back_to_cities");
        backRow.add(backButton);
        rows.add(backRow);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;

    }
    private void updateCalendarMessage(CallbackQuery callbackQuery, String text, UserSession session) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessage.setText(text);

        InlineKeyboardMarkup keyboard = createCalendarKeyboard(session.getCurrentCalendarMonth());
        editMessage.setReplyMarkup(keyboard);

        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
