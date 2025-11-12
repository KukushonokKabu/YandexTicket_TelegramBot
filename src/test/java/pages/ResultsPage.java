package pages;

import core.BaseTest;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.models.TrainInfo;
import ru.mydomain.Xpath;

/*
 * Page Object  класс для работы со страницей результатов поиска поездов.
 * Инкапсулирует все взаимодействия с элементами страницы результатов.
 */
public class ResultsPage extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private Xpath xpath;

    public ResultsPage(WebDriver driver,WebDriverWait wait ){
        this.driver = driver;
        this.wait = wait;
        this.xpath = new Xpath();
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
//        List<WebElement>trainElements = driver.findElements(By.xpath(xpath.getPlatz()));
//        if(index >= trainElements.size()){
//            throw new RuntimeException("Поезд с индексом "+index+" не найден");
//        }
//        WebElement trainElement = trainElements.get(index);
//        trainElement.click();
        TrainInfo info = new TrainInfo();
        // Ожидание загрузки страницы

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getCarriageNumber())));


            System.out.println("Код выполняется");
            try {
                info.setCarriageNumber(driver.findElement(By.xpath(xpath.getCarriageNumber())).getText());
                System.out.println("Получен номер вагона");
            }
            catch (Exception e){
                System.out.println("Ошибка получения номера вагона: "+ e.getMessage());
            }

            try {
                info.setArrivalStation(driver.findElement(By.xpath(xpath.getArrivalStation())).getText());
                System.out.println("Получена станция отправления и прибытия");
            }
            catch (Exception e){
                System.out.println("Ошибка получения станции отправления и прибытия: "+ e.getMessage());
            }
           try {
               info.setDateArrival(driver.findElement(By.xpath(xpath.getArrivalDate())).getText());
               System.out.println("Получена дата прибытия");
           }
           catch (Exception e){
               System.out.println("Ошибка получения даты прибытия: "+e.getMessage());
           }
           try {
               info.setArrivalTime(driver.findElement(By.xpath(xpath.getArrivalTime())).getText());
               System.out.println("Получена время прибытия");
           }
           catch (Exception e){
               System.out.println("Ошибка получения времени прибытия: "+e.getMessage());
           }
            try {
                info.setPrice(driver.findElement(By.xpath(xpath.getPrice())).getText());
                System.out.println("Получена цена");
            }
            catch (Exception e){
                System.out.println("Ошибка получения цены: "+e.getMessage());
            }
            try {
                info.setDateDeparture(driver.findElement(By.xpath(xpath.getDepartureDateDate())).getText());
                System.out.println("Получена дата отправления");
            }
            catch (Exception e){
                System.out.println("Ошибка получения даты отправления: "+ e.getMessage());
            }
            WebElement element = driver.findElement(By.xpath(xpath.getDepartureDateDate()));
            System.out.println("Есть shadow root: "+ hasShadowRoot(element));

           try {
               info.setDepartureTime(driver.findElement(By.xpath(xpath.getDepartureDateTime())).getText());
               System.out.println("Получено время отправления");
           }
           catch (Exception e){
               System.out.println("Ошибка получения времени отправления: "+e.getMessage());
           }
           try {
               info.setTravelTime(driver.findElement(By.xpath(xpath.getTravelTime())).getText());
               System.out.println("Получено время в пути");
           }
           catch (Exception e){
               System.out.println("Ошибка получения времени в пути: "+e.getMessage());
           }
            try {
                info.setPlace(driver.findElement(By.xpath(xpath.getPlaceNumber())).getText());
                System.out.println("Получен  номер места");
            }
            catch (Exception e){
                System.out.println("Ошибка получения номера места: "+e.getMessage());
            }
            try {
                info.setDepartureStation(driver.findElement(By.xpath(xpath.getDepartureStation())).getText());
                System.out.println("Получено станция отправления");
            }
            catch (Exception e){
                System.out.println("Ошибка получения станции отправления : "+e.getMessage());
            }




        return info;
    }

    // Проверка наличия Shadow DOM
    public boolean  hasShadowRoot(WebElement element){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRoot = (WebElement) js.executeScript("return arguments[0].shadowRoot",element);
        return shadowRoot != null;
    }


}
