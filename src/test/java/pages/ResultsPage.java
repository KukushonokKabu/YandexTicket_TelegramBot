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

import java.util.List;
import java.util.Random;

/*
 * Page Object  класс для работы со страницей результатов поиска поездов.
 * Инкапсулирует все взаимодействия с элементами страницы результатов.
 */
public class ResultsPage extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private Xpath xpath;

    public ResultsPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        this.xpath = new Xpath();
    }

    // Ожидание загрузки страницы результатов

    @Step("Ожидание загрузки страницы результатов")
    public void waitForResultsToLoad() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getPlatz())));
        } catch (Exception e) {
            throw new RuntimeException("На странице нет поездов с доступными плацкартами: " + e.getMessage());
        }
    }

    // Получение количества найденных поездов
    @Step("Проверяем количество найденных поездов")
    public int getTrainCount() {
        return driver.findElements(By.xpath(xpath.getPlatz())).size();
    }

    // Собираем информацию о случайном поезде по индексу

    @Step("Получение информации о поезде по индексу :{index}")
    public TrainInfo getTrainInfoByIndex() {
        TrainInfo info = new TrainInfo();

        // Ожидание загрузки контейнеров вагонов
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getCarriageContainer())));

        // Получаем все контейнеры-вагоны
        List<WebElement> carriageContainers = driver.findElements(By.xpath(xpath.getCarriageContainer()));

        if (carriageContainers.isEmpty()) {
            System.out.println("Не найдено контейнеров вагонов");
            return info;
        }

        // Выбираем случайный вагон
        Random random1 = new Random();
        int randomIndex = random1.nextInt(carriageContainers.size());
        int actualIndex = randomIndex + 1;

        System.out.println("Выбран вагон с индексом: " + actualIndex + " из " + carriageContainers.size());

        try {
            // 1. Получаем номер вагона из контейнера
            String carriageXPath = xpath.getCarriageNumberWithIndex(actualIndex);
            WebElement carriageElement = driver.findElement(By.xpath(carriageXPath));

            // Прокрутка к вагону
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});", carriageElement);

            // Получаем номер вагона
            String carriageNumber = carriageElement.getText();
            info.setCarriageNumber(carriageNumber);
            System.out.println("Номер вагона: " + carriageNumber);

            // 2. Находим доступные места ВНУТРИ этого контейнера
            String availablePlacesXPath = "//ul[@role='tablist']/li[" + actualIndex + "]//*[contains(@class, 'Place_available')]";
            List<WebElement> availablePlaces = driver.findElements(By.xpath(availablePlacesXPath));

            if (availablePlaces.isEmpty()) {
                System.out.println("В вагоне " + carriageNumber + " нет доступных мест");
                return info;
            }

            // Выбираем случайное место
            int randomPlaceIndex = random1.nextInt(availablePlaces.size());
            WebElement selectedPlace = availablePlaces.get(randomPlaceIndex);

            System.out.println("Выбрано место с индексом: " + randomPlaceIndex + " из " + availablePlaces.size());

            // Прокрутка к месту
            //  js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});", selectedPlace);

            // Делаем скриншот ДО клика
            takeScreenshot("Скриншот до выбора места в вагоне " + carriageNumber);

            // Получаем номер места из атрибута (например, из id)
            String placeId = selectedPlace.getAttribute("id");
            String placeNumber = placeId.replace("tier/place-", ""); // "tier/place-47" -> "47"
            System.out.println("Номер места из атрибута: " + placeNumber);

            // Кликаем на выбранное место
            selectedPlace.click();
            System.out.println("Кликнули на место: " + placeNumber);

            // Сохраняем номер места
            info.setPlace(placeNumber);

            // Делаем скриншот ПОСЛЕ клика
            takeScreenshot("Скриншот после выбора места");

            // 3. Получаем остальную информацию со страницы
            try {
                info.setArrivalStation(driver.findElement(By.xpath(xpath.getArrivalStation())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения станции прибытия: " + e.getMessage());
            }

            try {
                info.setDepartureStation(driver.findElement(By.xpath(xpath.getDepartureStation())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения станции отправления: " + e.getMessage());
            }

            try {
                info.setDepartureTime(driver.findElement(By.xpath(xpath.getDepartureDateTime())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения времени отправления: " + e.getMessage());
            }

            try {
                info.setArrivalTime(driver.findElement(By.xpath(xpath.getArrivalTime())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения времени прибытия: " + e.getMessage());
            } try {
                info.setDateDeparture(driver.findElement(By.xpath(xpath.getDepartureDateDate())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения даты отправления: " + e.getMessage());
            }try {
                info.setDateArrival(driver.findElement(By.xpath(xpath.getArrivalDate())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения даты прибытия: " + e.getMessage());
            }try {
                info.setArrivalStation(driver.findElement(By.xpath(xpath.getArrivalStation())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения станции прибытия: " + e.getMessage());
            }try {
                info.setDepartureStation(driver.findElement(By.xpath(xpath.getDepartureStation())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения станции отправления: " + e.getMessage());
            }

            try {
                info.setPrice(driver.findElement(By.xpath(xpath.getPrice())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения цены: " + e.getMessage());
            }

            try {
                info.setTravelTime(driver.findElement(By.xpath(xpath.getTravelTime())).getText());
            } catch (Exception e) {
                System.out.println("Ошибка получения времени в пути: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Ошибка при работе с вагоном " + actualIndex + ": " + e.getMessage());
        }

        return info;
    }

    // Проверка наличия Shadow DOM
    public boolean hasShadowRoot(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRoot = (WebElement) js.executeScript("return arguments[0].shadowRoot", element);
        return shadowRoot != null;
    }


}
