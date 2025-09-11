package ru.mydomain;

import java.time.LocalDate;

public class UserSession {
    private Long chatId;
    private String departureStation;
    private String arrivalStation;
    private String date;
    private LocalDate currentCalendarMonth;

    public enum Step{
        WAITING_FOR_DEPARTURE,
        WAITING_FOR_DEPARTURE_SELECTION,
        WAITING_FOR_ARRIVAL,
        WAITING_FOR_ARRIVAL_SELECTION,
        WAITING_FOR_DATE,
        COMPLETED
    }
    private Step currentStep;

    public UserSession(long chatId){
        this.chatId = chatId;
        this.currentStep = Step.WAITING_FOR_DEPARTURE;
    }
    // Геттеры и сеттеры
    public long getChatId(){return  chatId;}
    public void setChatId(Long chatId){this.chatId= chatId;}

    public String getDepartureStation(){return  departureStation;}
    public void setDepartureStation(String departureStation){this.departureStation=departureStation;}

    public String getArrivalStation(){return arrivalStation;}
    public void setArrivalStation(String arrivalStation){this.arrivalStation = arrivalStation;}

    public String getDate(){return date;}
    public void setDate(String date){this.date = date;}

    public Step getCurrentStep(){return  currentStep;}
    public void setCurrentStep(Step currentStep){this.currentStep = currentStep;}

    public LocalDate getCurrentCalendarMonth(){return currentCalendarMonth;}
    public void setCurrentCalendarMonth(LocalDate currentCalendarMonth){this.currentCalendarMonth = currentCalendarMonth;}
}
