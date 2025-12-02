package ru.mydomain;

public class Xpath {

    public String getSearchButton() {
        return searchButton;
    }

    public String getPlatz() {
        return platz;
    }

    public String getNotPlaces() {
        return notPlaces;
    }

    public String getTextFieldOut() {
        return textFieldOut;
    }

    public String getButtonClear() {
        return buttonClear;
    }

    public String getCalendar() {
        return calendar;
    }

    public String getPriceElements() {
        return priceElements;
    }

    public String getTextFieldIn() {
        return textFieldIn;
    }

    public String getTicketCards() {
        return ticketCards;
    }

    public String getCarriageNumber() {
        return carriageNumber;
    }

    public String getPrice() {
        return price;
    }

    public String getDepartureDateDate() {
        return departureDateDate;
    }

    public String getDepartureDateTime() {
        return departureDateTime;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public String getPlaceNumber() {
        return placeNumber;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    private String searchButton = "//button[@type='submit']";// Кнопка "Найти"
    private String platz = "//a[contains(@href, 'coachType=platzkarte')]";// поезда у которых есть плацкарт
    private String notPlaces = "//h2[@class='ReS7e XFySC b9-76']";// Надпись "На заданном направлении (или поезде) мест
    // нет"
    private String textFieldOut = "//input[@class='urSya']";// Ввод города отправления
    private String textFieldIn = "//input[@class='urSya input_center']";// Ввод города прибытия
    private String buttonClear = "//button[@aria-label='Очистить поле']";// Кнопка очисти города отправления
    private String calendar = "//div[@class='YC-8m vap86 UWwtJ'][contains(text(),'Туда')]";// Тригер календаря
    private String priceElements = "//div[@data-qa='calendar']//span[@data-qa='price']";// Xpath дней содержащих цены в
    // календаре
    private String ticketCards = "//*[contains(@class, 'Place_available')]";// Доступные для бронирования места в поезде
    private String carriageNumber = "//span[contains(text(),' вагон')]";// Номер вагона
    private String price = "//span[@data-qa='price' and contains(@class, 'total')]";// Цена
    private String departureDateDate = "//time[@datetime]";// Дата отправления
    private String departureDateTime = "//div[contains(@class, 'b9-76') and contains(text(), ':')][1]";// Время
    // отправления
    private String departureStation = "//form[@name='TRAIN_BOOK']//div[contains(@class, 'KWm7w')]/div[1]";// Вокзал отправления
    private String placeNumber = "//span[contains(text(), 'место')]";// Номер места
    private String arrivalDate = "//div[@class='XHFnM A21d5']//div[1]";// Дата прибытия
    private String arrivalTime = "//div[contains(@class, 'b9-76') and contains(text(), ':')][2]";// Время прибытия
    private String suggestionStation = "//div[contains(@id, 'suggest-')]";// Всплывающие подсказки с названия станций и городов
    private String calendarDepartureValue = "//div[@data-qa='start-trigger-value' or @data-qa='end-trigger-value']";// Значение
    private String trainNumber = "//*[contains(text(),'Поезд')]"; // Номер поезда
    private String arrivalStation = "//div[@class='Eqn7e b9-76 XoAQK']"; // Станция прибытия
    private String departureAndArrivalCity = "//div[@class='fr56h b9-76']"; // Город отправления - прибытия
    private String travelTime = "//div[@class='hAMfi']//div[1]"; // Время в пути
    private String carriageContainer = "//ul[@role='tablist']/li"; // Котнейнер вагона


    public String getTrainNumber() {
        return trainNumber;
    }
    public String getCarriageContainer() {
        return carriageContainer;
    }
    // Номер вагона с индексом
    public String getCarriageNumberWithIndex(int index) {
        return String.format("//ul[@role='tablist']/li[%d]//span[contains(text(),' вагон')]", index);
    }

    // Номер места с индексом
    public String getPlaceNumberWithIndex(int index) {
        return String.format("//ul[@role='tablist']/li[%d]//span[contains(text(),'место')]/following-sibling::span[1]", index);
    }



    public String getArrivalStation() {
        return arrivalStation;
    }



    public String getTravelTime() {
        return travelTime;
    }


    public String getDepartureAndArrivalCity() {
        return departureAndArrivalCity;
    }



    public String getCalendarDepartureValue() {
        return calendarDepartureValue;
    }



    public String getSuggestionStation() {
        return suggestionStation;
    }

    // Доступные места в конкретном контейнере
    public String getAvailablePlacesWithIndex(int index) {
        return String.format("//ul[@role='tablist']/li[%d]//*[contains(@class, 'Place_available')]", index);
    }


}
