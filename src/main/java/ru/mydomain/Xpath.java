package ru.mydomain;

public class Xpath {

    public String getSearchButton() {
        return searchButton;
    }
    public String getPlatz(){
        return platz;
    }
    public String getNotPlaces(){
        return notPlaces;
    }
    public String getTextFieldOut(){
        return textFieldOut;
    }
    public String getButtonClear(){
        return buttonClear;
    }
    public String getCalendar(){
        return calendar;
    }
    public String getPriceElements(){
        return  priceElements;
    }
    public String getTextFieldIn(){
        return textFieldIn;
    }
    public String getTicketCards(){
        return ticketCards;
    }
    public String getCarriageNumber(){
        return  carriageNumber;
    }
    public String getPrice(){
        return price;
    }
    public String getDepartureDateDate(){
        return  departureDateDate;
    }
    public String getDepartureDateTime(){
        return departureDateTime;
    }
    public String getDepartureStation(){
        return  departureStation;
    }
    public String getPlaceNumber(){
        return placeNumber;
    }
    public String getArrivalDate(){
        return arrivalDate;
    }
    public String getArrivalTime(){
        return arrivalTime;
    }

    private String searchButton = "//button[@type='submit']";// Кнопка "Найти"
    private String platz = "//span[@class='w49Yf' and text()='плац']";// поезда у которых есть плацкарт
    private String notPlaces = "//h2[@class='ReS7e XFySC b9-76']";// Надпись  "На заданном направлении (или поезде) мест нет"
    private String textFieldOut = "//input[@class='w_eHd']";// Ввод города отправления
    private String textFieldIn = "//input[@class='w_eHd input_center']";// Ввод города прибытия
    private String buttonClear = "//button[@aria-label='Очистить поле']";// Кнопка очисти города отправления
    private String calendar = "//div[@class='YC-8m vap86 UWwtJ']";// Тригер календаря
    private String priceElements = "//div[@data-qa='calendar']//span[@data-qa='price']";// Xpath  дней содержащих цены в календаре
    private String ticketCards =  "//*[contains(@class, 'Place_available')]";// Доступные для бронирования места в поезде
    private String carriageNumber  = "//div[@class='UOPED jneZF _4ZiVK']";// Номер вагона
    private String price = "//div[@class='SJEAC']";//  Цена
    private String departureDateDate = "//div[@class='Eqn7e b9-76']";//Дата отправления
    private String departureDateTime = "//div[@class='EhCXF EnnSI _274Q5']//div[@class='fr56h b9-76']";// Время отправления
    private String departureStation ="//div[@class='uAsFU']";// Вокзал отправления
    private String placeNumber = "//span[@class='lQbtv kdDAS b9-76']";// Номер места
    private String arrivalDate ="//div[@class='M3inU GU5NH']//div[@class='kdDAS dNANh']";// Дата прибытия
    private String arrivalTime = "//div[@class='EhCXF EnnSI _274Q5']//div[@class='fr56h b9-76'][2]";// Время прибытия
    private String suggestionStation = "//div[@class='GxV0a']";// Всплывающие подсказки с названия станций и городов
    private String calendarDepartureValue = "//div[@data-qa='start-trigger-value']";// Значение поля даты отправления


    public String getCalendarDepartureValue() {
        return calendarDepartureValue;
    }

    public void setCalendarDepartureValue(String calendarDepartureValue) {
        this.calendarDepartureValue = calendarDepartureValue;
    }



    public String getSuggestionStation() {
        return suggestionStation;
    }

    public void setSuggestionStation(String suggestionStation) {
        this.suggestionStation = suggestionStation;
    }

}
