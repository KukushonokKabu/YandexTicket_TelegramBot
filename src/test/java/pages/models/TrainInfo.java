package pages.models;

/*
 * Модель данных для хранения информации о поезде
 */
public class TrainInfo {
    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    private String trainNumber;
    private String carriageNumber;// Номер вагона
    private String dateDeparture; // Дата отправления
    private String dateArrival; // Дата прибытия
    private String departureTime; // Время отправления
    private String arrivalTime; // Время прибытия
    private String arrivalStation; // Станция прибытия
    private String travelTime; // Время в пути
    private String price; // Цена
    private String place; // Номер места


    @Override
    public String toString() {
        return "TrainInfo{" +
                "Номер поезда: '"+ trainNumber + '\''+
                "Номер вагона: '" + carriageNumber + '\'' +
                ", Дата отправления: '" + dateDeparture + '\'' +
                ", Дата прибытия: '" + dateArrival + '\'' +
                ", Время отправления: '" + departureTime + '\'' +
                ", Время прибытия: '" + arrivalTime + '\'' +
                ", Время в пути: '" + travelTime + '\'' +
                ", Цена: '" + price + '\'' +
                ", Место: '" + place + '\'' +
                ", Станция отправления: '" + departureStation + '\'' +
                ", Станция прибытия: '" + arrivalStation + '\'' +
                '}';
    }



    public String getCarriageNumber() {
        return carriageNumber;
    }

    public void setCarriageNumber(String carriageNumber) {
        this.carriageNumber = carriageNumber;
    }

    public String getDateDeparture() {
        return dateDeparture;
    }

    public void setDateDeparture(String dateDeparture) {
        this.dateDeparture = dateDeparture;
    }

    public String getDateArrival() {
        return dateArrival;
    }

    public void setDateArrival(String dateArrival) {
        this.dateArrival = dateArrival;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    private String departureStation; // Станция/город отправления


    public TrainInfo(String trainNumber ,String carriageNumber, String dateDeparture, String dateArrival, String departureTime, String arrivalTime, String travelTime, String price, String place, String departureStation, String arrivalStation) {
        this.carriageNumber = carriageNumber; // Номер вагона
        this.dateDeparture = dateDeparture; // Дата отправления
        this.dateArrival = dateArrival; // Дата прибытия
        this.departureTime = departureTime; // Время отправления
        this.arrivalTime = arrivalTime; // Время прибытия
        this.travelTime = travelTime; // Время в пути
        this.price = price; // Цена
        this.place = place; // Место
        this.departureStation = departureStation; // Станция отправления
        this.arrivalStation = arrivalStation; // Станция прибытия
        this.trainNumber = trainNumber; // Номер поезда
    }
    public TrainInfo(){}


}
