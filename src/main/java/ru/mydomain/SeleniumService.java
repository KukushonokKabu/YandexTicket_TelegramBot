package ru.mydomain;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SeleniumService {
    private WebDriver driver;
    private Xpath xpath;

    public void initDriver(boolean headless){
        this.xpath = new Xpath();
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if(headless){
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    public List<String> getStationSuggestionsDeparture(String query){
        try {
            //Переходим на сайт
            driver.get("https://travel.yandex.ru/trains/");
            // Находим поле "Откуда" и вводим запрос
            WebElement cityDeparture = driver.findElement(By.xpath(xpath.getTextFieldOut()));
            WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
            clearButton.click();
            cityDeparture.sendKeys(query);

            // Ждём появления подсказок
            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(5));
            By suggestionLocator = By.xpath(xpath.getSuggestionStation());
            wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));
            Thread.sleep(1000);

            // Собираем текст подсказок
            List<WebElement>suggestionElements = driver.findElements(suggestionLocator);
            // Собираем текст всех подсказок пока они не стали устаревшими :-)
            List<String>suggestions = new ArrayList<>();

            for(WebElement element : suggestionElements){
                try {
                    String text = element.getText();
                            if(!text.trim().isEmpty()){
                                suggestions.add(text);
                                if (suggestions.size()>=5)break;//Ограничиваемся 5 подсказками
                            }
                }
                catch (StaleElementReferenceException e){
                    // Если элемент устарел пропускаем его.
                    continue;
                }
            }

            return suggestions;

        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении подсказок:"+ e.getMessage());
        }
    }
    List<String>getStationSuggestionsArrival(String query){
        try {
            WebElement cityArrival = driver.findElement(By.xpath(xpath.getTextFieldIn()));
            cityArrival.click();
            cityArrival.sendKeys(query);

            // Ждём появления подсказок
            By suggestionArrivalLocator = By.xpath(xpath.getSuggestionStation());
            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionArrivalLocator));
            Thread.sleep(1000);

            // Собираем текст подсказок
            List<WebElement>suggestionArrival = driver.findElements(By.xpath(xpath.getSuggestionStation()));
            List<String>suggestionArrivalText = new ArrayList<>();

            for(WebElement element : suggestionArrival){
                try {
                    suggestionArrivalText.add(element.getText());
                    if(suggestionArrivalText.size()>=5)break;// Ограничиваемся 5 подсказками
                }
                catch (StaleElementReferenceException e){
                    // Если элемент устарел пропускаем его
                    continue;
                }


            }
            return suggestionArrivalText;

        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении подсказок :"+  e.getMessage());
        }


    }
    public void selectStation(String stationName,boolean isDeparture){
        try {
            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(5));
            // Сначала очищаем
            clearFields();

            if(isDeparture){
                // Очищаем поле  "Откуда" и вводим выбранную станцию
                WebElement cityDeparture = driver.findElement(By.xpath(xpath.getTextFieldOut()));
                WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
                clearButton.click();
                cityDeparture.sendKeys(stationName);

                // Ждем появления подсказок и кликаем на нужную
                By suggestionLocator = By.xpath(xpath.getSuggestionStation());
                wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));

                //Находим и кликаем на элемент с нужным текстом
                WebElement exactSuggestion = driver.findElement(By.xpath(xpath.getSuggestionStation()+"[text()='" + stationName+ "']"));
                exactSuggestion.click();


            }
            else {
                // Очищаем поле "Куда" и вводим выбранную станцию
                WebElement cityArrival = driver.findElement(By.xpath(xpath.getTextFieldIn()));
                cityArrival.click();
                cityArrival.clear();
                cityArrival.sendKeys(stationName);

                //  Ждем появления подсказок и кликаем на нужную
                By suggestionLocator =  By.xpath(xpath.getSuggestionStation());
                wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));

                Thread.sleep(1000);

                // Находим и кликаем на элемент с нужным текстом
                WebElement exactSuggestion = driver.findElement(By.xpath(xpath.getSuggestionStation()+"[contains(text(), '" + stationName+ "')]"));
                exactSuggestion.click();


            }
            Thread.sleep(1000);//  Время для принятия изменений
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Ошибка при выборе станции: "+e.getMessage());
        }
    }
    public  void clearFields(){
        try {
            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(3));

            // Очищаем поле "Откуда"
            WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
            WebElement clearDepartureButton = driver.findElement(By.xpath(xpath.getButtonClear()));
            clearDepartureButton.click();

            // Очищаем поле "Куда"
            WebElement arrivalField = driver.findElement(By.xpath(xpath.getTextFieldIn()));
            arrivalField.click();
            arrivalField.clear();

            // Даем время для очистки
            Thread.sleep(500);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Ошибка при очистке полей: "+  e.getMessage());
        }
    }
    public void quitDriver(){
        if (driver!=null){
            driver.quit();
        }
    }
}
