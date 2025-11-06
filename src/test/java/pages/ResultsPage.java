package pages;

import core.BaseTest;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.models.TrainInfo;

import java.nio.file.WatchEvent;
import java.util.List;

/*
 * Page Object  класс для работы со страницей результатов поиска поездов.
 * Инкапсулирует все взаимодействия с элементами страницы результатов.
 */
public class ResultsPage extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;

    public ResultsPage(WebDriver driver,WebDriverWait wait){
        this.driver = driver;
        this.wait = wait;
    }

    // Ожидание загрузки страницы результатов

    @Step("Ожидание загрузки страницы результатов")
 public void waitForResultsToLoad(){
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getPlatz())));
        }
        catch (Exception e){
            throw new RuntimeException("На странице нет поездов с доступными плацкартами: "+ e.getMessage());
        }
    }

    // Получение количества найденных поездов
    @Step("Проверяем количество найденных поездов")
    public int getTrainCount(){
         return driver.findElements(By.xpath(xpath.getPlatz())).size();
    }

    // Собираем информацию о случайном поезде по индексу

    @Step("Получение информации о поезде по индексу :{index}")
    public TrainInfo getTrainInfoByIndex(int index){
        List<WebElement>trainElements = driver.findElements(By.xpath(xpath.getPlatz()));
        if(index >= trainElements.size()){
            throw new RuntimeException("Поезд с индексом "+index+" не найден");
        }
        WebElement trainElement = trainElements.get(index);
        trainElement.click();
        TrainInfo info = new TrainInfo();

        try {
            info.setArrivalStation(trainElement.findElement(By.xpath(xpath.getDepartureAndArrivalCity())).getText());
            info.setCarriageNumber(trainElement.findElement(By.xpath(xpath.getCarriageNumber())).getText());
            info.setDateArrival(trainElement.findElement(By.xpath(xpath.getArrivalDate())).getText());
            info.setArrivalTime(trainElement.findElement(By.xpath(xpath.getArrivalTime())).getText());
            info.setPrice(trainElement.findElement(By.xpath(xpath.getPrice())).getText());
            info.setDateDeparture(trainElement.findElement(By.xpath(xpath.getDepartureDateDate())).getText());
            info.setDepartureTime(trainElement.findElement(By.xpath(xpath.getDepartureDateTime())).getText());
            info.setTravelTime(trainElement.findElement(By.xpath(xpath.getTravelTime())).getText());
            info.setPlace(trainElement.findElement(By.xpath(xpath.getPlaceNumber())).getText());
            info.setDepartureStation(trainElement.findElement(By.xpath(xpath.getDepartureStation())).getText());

        }
        catch (Exception e){
            new RuntimeException("При получении данных произошла ошибка: "+ e.getMessage());
        }

        return info;
    }


}
