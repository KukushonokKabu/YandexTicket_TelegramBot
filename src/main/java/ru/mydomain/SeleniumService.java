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
import java.util.NoSuchElementException;

public class SeleniumService {
    private WebDriver driver;
    private Xpath xpath;
     private WebDriverWait wait;

    public void initDriver(boolean headless) {
        this.xpath = new Xpath();
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        this.wait = new WebDriverWait(driver,Duration.ofSeconds(10));
    }

    /*public List<String> getStationSuggestionsDeparture(String query){
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
    }*/

    public List<ElementInfo> getStationSuggestionsDeparture(String query) {
try {
    driver.get("https://travel.yandex.ru/trains/");
    WebElement cityDeparture = driver.findElement(By.xpath(xpath.getTextFieldOut()));
    WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
    clearButton.click();
    cityDeparture.sendKeys(query);

    // Ждём появления блоков-контейнеров
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='EhCXF _274Q5']")));
    Thread.sleep(1000);

    List<ElementInfo>result = new ArrayList<>();

    // Находим все блоки-контейнеры
    List<WebElement>contentBlocks = driver.findElements(By.xpath("//div[@class='EhCXF _274Q5']"));
    System.out.println("Найдено блоков :"+ contentBlocks.size());// Для отладки


    // Обрабатываем каждый блок отдельно
    for(WebElement block : contentBlocks){
        try {
            ElementInfo blockData  = extractDataFromBlock(block);
            if(!blockData.getMainText().isEmpty()){
                result.add(blockData);
            }

            // Ограничиваемся количеством результатов
            if (result.size()>=5)break;
        }
        catch (StaleElementReferenceException e){
            System.out.println("Элемент устарел , пропускаем ...");
            continue;
        }
    }
    System.out.println("Собрано результатов :"+ result.size());// Для отладки
    return  result;
}
catch (Exception e){
    e.printStackTrace();
    throw new RuntimeException("Ошибка при получении подсказок: "+  e.getMessage());
}
    }

    private ElementInfo extractDataFromBlock(WebElement block) {
        try {
            // Ищем информацию внутри этого блока
            String mainText = block.findElement(By.xpath(".//div[@class='GxV0a']")).getText();
            String additionalText = "";

            try {
                // Ищем дополнительную информацию внутри этого блока
                WebElement additional = block.findElement(By.xpath(".//div[@class='tbbhG']"));
                additionalText = additional.getText();
            } catch (NoSuchElementException e) {
                // Дополнения нет - это нормально
                System.out.println("Для '" + mainText + "' дополнения нет");
            }

            // ВАЖНО: возвращаем результат после успешного извлечения
            System.out.println("Извлечено: '" + mainText + "' + '" + additionalText + "'");
            return new ElementInfo(mainText, additionalText);

        } catch (Exception e) {
            System.out.println("Ошибка при извлечении данных из блока: " + e.getMessage());
            return new ElementInfo("", "");
        }
    }
    public List<ElementInfo> getStationSuggestionsArrival(String query) {
        try {
            WebElement cityArrival = driver.findElement(By.xpath(xpath.getTextFieldIn()));
            cityArrival.click();
            cityArrival.sendKeys(query);

            // Ждём появления блоков-контейнеров (как в departure)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='EhCXF _274Q5']")));
            Thread.sleep(1000);

            List<ElementInfo> result = new ArrayList<>();
            List<WebElement> contentBlocks = driver.findElements(By.xpath("//div[@class='EhCXF _274Q5']"));
            System.out.println("Найдено блоков прибытия: " + contentBlocks.size());

            for (WebElement block : contentBlocks) {
                try {
                    ElementInfo blockData = extractDataFromBlock(block);
                    if (!blockData.getMainText().isEmpty()) {
                        result.add(blockData);
                    }
                    if (result.size() >= 5) break;
                } catch (StaleElementReferenceException e) {
                    System.out.println("Элемент устарел, пропускаем...");
                    continue;
                }
            }
            System.out.println("Собрано результатов прибытия: " + result.size());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении подсказок прибытия: " + e.getMessage());
        }
    }

    public void selectStation(String stationName, boolean isDeparture) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // Очищаем поля
            clearFields();

            // Извлекаем только основное название станции (без доп информации)
            String cleanStationName = stationName.split("\\s*\\(")[0].trim();

            if (isDeparture) {
                // Для отправления
                WebElement cityDeparture = driver.findElement(By.xpath(xpath.getTextFieldOut()));
                WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
                clearButton.click();
                cityDeparture.sendKeys(cleanStationName);

                // Ждем появления подсказок
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[@class='EhCXF _274Q5']")
                ));
                Thread.sleep(1000);

                // НАХОДИМ И КЛИКАЕМ на нужный элемент в выпадающем списке
                WebElement exactSuggestion = findAndClickExactSuggestion(cleanStationName);

                if (exactSuggestion == null) {
                    throw new RuntimeException("Не удалось найти станцию: " + cleanStationName);
                }

            } else {
                // Для прибытия
                WebElement cityArrival = driver.findElement(By.xpath(xpath.getTextFieldIn()));
                cityArrival.click();
                cityArrival.clear();
                cityArrival.sendKeys(cleanStationName);

                // Ждем появления подсказок
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[@class='EhCXF _274Q5']")
                ));
                Thread.sleep(1000);

                // НАХОДИМ И КЛИКАЕМ на нужный элемент в выпадающем списке
                WebElement exactSuggestion = findAndClickExactSuggestion(cleanStationName);

                if (exactSuggestion == null) {
                    throw new RuntimeException("Не удалось найти станцию: " + cleanStationName);
                }
            }

            Thread.sleep(1000); // Даем время для применения изменений

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при выборе станции: " + e.getMessage());
        }
    }

    // НОВЫЙ МЕТОД для поиска и клика по точному совпадению
    private WebElement findAndClickExactSuggestion(String stationName) {
        try {
            // Ищем ВСЕ блоки-контейнеры
            List<WebElement> contentBlocks = driver.findElements(By.xpath("//div[@class='EhCXF _274Q5']"));

            for (WebElement block : contentBlocks) {
                try {
                    // Извлекаем основной текст из блока
                    WebElement mainTextElement = block.findElement(By.xpath(".//div[@class='GxV0a']"));
                    String blockText = mainTextElement.getText().trim();

                    // Сравниваем с искомым названием (можно использовать contains для гибкости)
                    if (blockText.equalsIgnoreCase(stationName) ||
                            blockText.toLowerCase().contains(stationName.toLowerCase())) {

                        // КЛИКАЕМ на весь блок или на основной текст
                        mainTextElement.click();
                        System.out.println("Успешно кликнули на станцию: " + blockText);
                        return mainTextElement;
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("Элемент устарел при поиске, продолжаем...");
                    continue;
                }
            }

            // Если не нашли точного совпадения, пробуем найти по частичному совпадению
            System.out.println("Точное совпадение не найдено, ищем по частичному...");
            return findAndClickPartialSuggestion(stationName);

        } catch (Exception e) {
            System.out.println("Ошибка при поиске станции: " + e.getMessage());
            return null;
        }
    }

    // Дополнительный метод для поиска по частичному совпадению
    private WebElement findAndClickPartialSuggestion(String stationName) {
        try {
            // Альтернативный поиск - ищем любой элемент с текстом станции
            WebElement suggestion = driver.findElement(
                    By.xpath("//div[@class='GxV0a'][contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" +
                            stationName.toLowerCase() + "')]")
            );
            suggestion.click();
            System.out.println("Нашли станцию по частичному совпадению: " + suggestion.getText());
            return suggestion;

        } catch (Exception e) {
            System.out.println("Не удалось найти станцию даже по частичному совпадению: " + stationName);
            return null;
        }
    }

    public void clearFields() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка при очистке полей: " + e.getMessage());
        }
    }
    // Метод для отладки , нужно вызывать после каждого действия чтоб видеть что происходит в браузере
    public void debugCurrentState(){
        try {
            System.out.println("=== ТЕКУЩЕЕ СОСТОЯНИЕ ===");

            // Проверяем поля ввода
            WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
            WebElement arrivalField = driver.findElement(By.xpath(xpath.getTextFieldIn()));

            System.out.println("Поле 'Откуда': " + departureField.getAttribute("value"));
            System.out.println("Поле 'Куда': " + arrivalField.getAttribute("value"));

            // Проверяем есть ли открытые подсказки
            List<WebElement> suggestions = driver.findElements(By.xpath("//div[@class='EhCXF _274Q5']"));
            System.out.println("Открыто подсказок: " + suggestions.size());

            for (int i = 0; i < suggestions.size(); i++) {
                try {
                    String text = suggestions.get(i).findElement(By.xpath(".//div[@class='GxV0a']")).getText();
                    System.out.println("Подсказка " + i + ": " + text);
                } catch (Exception e) {
                    System.out.println("Не удалось прочитать подсказку " + i);
                }
            }
            System.out.println("=========================");
        } catch (Exception e) {
            System.out.println("Ошибка при отладке: " + e.getMessage());
        }
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

}
