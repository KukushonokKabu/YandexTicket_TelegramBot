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
            By suggestionLocator = By.xpath("//div[@class='GxV0a']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));
            Thread.sleep(1000);

            // Собираем текст подсказок
            List<WebElement>cityElements = driver.findElements(suggestionLocator);
            // Собираем текст всех подсказок пока они не стали устаревшими :-)
            List<String>suggestions = new ArrayList<>();

            for(WebElement cityElement : cityElements){
                try {
                    String cityText = cityElement.getText().trim();
                            if(cityText.isEmpty())continue;

                            // Ищем дополнительную информацию
                    String additionalInfo="";
                    // Поиск информации в том же контейнере
                    try {
                        // Ищем родительский контейнер , затем в нем дополнительную информацию
                        WebElement parentContainer = cityElement.findElement(By.xpath("./ancestor::div[contains(@class, 'EhCXF') or contains(@class, '_274Q5')][1]"));
                        List<WebElement>infoElements = parentContainer.findElements(By.xpath(".//div[@class='tbbhG']"));
                        if(!infoElements.isEmpty()){
                            additionalInfo = " ("+ infoElements.get(0).getText().trim()+")";
                        }
                    }
                    catch (Exception e){
                        // Если не нашли дополнительной информации - это норммально
                    }
                    String fullSuggestion = cityText + additionalInfo;
                    suggestions.add(fullSuggestion);

                    if(suggestions.size()>=5) break;
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
            By suggestionLocator = By.xpath("//div[@class='GxV0a']");
            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));
            Thread.sleep(1000);

            // Собираем текст подсказок
            List<WebElement>cityElements = driver.findElements((suggestionLocator));
            List<String>suggestions = new ArrayList<>();

            for(WebElement cityElement : cityElements){
                try {
                    String cityText = cityElement.getText().trim();
                    if(cityText.isEmpty())continue;

                    String additionalInfo = "";

                    //  Поиск дополнительной информации для этого города
                    try {
                        WebElement parentContainer = cityElement.findElement(By.xpath("./ancestor::div[contains(@class, 'EhCXF') or contains(@class, '_274Q5')][1]"));
                        List<WebElement>infoElements = parentContainer.findElements(By.xpath(".//div[@class='tbbhG']"));

                        if(!infoElements.isEmpty()){
                            additionalInfo = " ("+ infoElements.get(0).getText().trim()+")";
                        }
                    }
                    catch (Exception e){
                        // Дополнительная информация не найдена  - пропускаем
                    }
                    String fullSuggestion = cityText + additionalInfo;
                    suggestions.add(fullSuggestion);

                    if(suggestions.size()>=5) break;

                }
                catch (StaleElementReferenceException e){
                    // Если элемент устарел пропускаем его
                    continue;
                }


            }
            return suggestions;

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

            //  Извлекаем только основное название станций(без доп информации)
            String cleanStationName = stationName.split("\\s*\\(")[0].trim();



            if(isDeparture){
                // Очищаем поле  "Откуда" и вводим выбранную станцию
                WebElement cityDeparture = driver.findElement(By.xpath(xpath.getTextFieldOut()));
                WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
                clearButton.click();
                cityDeparture.sendKeys(cleanStationName);

                // Ждем появления подсказок и кликаем на нужную
                By suggestionLocator = By.xpath("//div[@class='GxV0a']");
                wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));
                Thread.sleep(1000);

                //Находим и кликаем на элемент с нужным текстом
                WebElement exactSuggestion = driver.findElement(
                        By.xpath("//div[@class='GxV0a'][contains(text(), '" + cleanStationName + "')]")
                );
                exactSuggestion.click();
                wait.until(ExpectedConditions.attributeContains(cityDeparture,"value",cleanStationName));
                Thread.sleep(1000);


            }
            else {
                // Очищаем поле "Куда" и вводим выбранную станцию
                WebElement cityArrival = driver.findElement(By.xpath(xpath.getTextFieldIn()));
                cityArrival.click();
                cityArrival.clear();
                cityArrival.sendKeys(cleanStationName);

                //  Ждем появления подсказок и кликаем на нужную
                By suggestionLocator =  By.xpath(xpath.getSuggestionStation());
                wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));

                Thread.sleep(1000);

                // Находим и кликаем на элемент с нужным текстом
                WebElement exactSuggestion = driver.findElement(
                        By.xpath("//div[@class='GxV0a'][contains(text(), '" + cleanStationName + "')]")
                );
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
