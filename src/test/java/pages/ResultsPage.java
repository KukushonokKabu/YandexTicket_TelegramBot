package pages;

import core.BaseTest;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;
import pages.models.TrainInfo;
import ru.mydomain.Xpath;
import utils.helpers.BaseTestHelper;
import utils.retry.SeleniumRetry;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


/*
 * Page Object  класс для работы со страницей результатов поиска поездов.
 * Инкапсулирует все взаимодействия с элементами страницы результатов.
 */
public class ResultsPage extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(ResultsPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Xpath xpath;
    private final BaseTestHelper helper;
    private final SeleniumRetry seleniumRetry;
    private final Random random = new Random();

    public ResultsPage(WebDriver driver,WebDriverWait wait){
        this.driver = driver;
        this.wait = wait;
        this.xpath = new Xpath();
        this.helper = new BaseTestHelper(driver,wait);
        this.seleniumRetry = new SeleniumRetry(driver,3,500);
    }

    @Step("Ожидание загрузки страницы результатов")
    public void waitForResultsToLoad(){
        try {
            seleniumRetry.findElement(By.xpath(xpath.getPlatz()));
            log.info("Страница результатов успешно загружена");
            Reporter.log("Страница результатов успешно загружена");
        } catch (Exception e){
            log.error("На странице нет поездов с доступными плацкартами", e);
            helper.takeScreenshot("Ошибка_загрузки_страницы");
            throw new AssertionError("На странице нет поездов с доступными плацкартами", e);
        }
    }

    @Step("Проверяем количество найденных поездов")
    public int getTrainCount(){
        List<WebElement>trains = seleniumRetry.findElements(By.xpath(xpath.getPlatz()));

        assertThat(trains)
                .as("Список найденных поездов")
                .isNotEmpty();

        int count = trains.size();
        log.info("Найдено поездов: {}" , count);
        Reporter.log("Найдено поездов: " + count + "<br/>");
        return count;
    }

    @Step("Получение информации о поезде и месте")
    public TrainInfo collectTrainAndSeatInfo(){
        TrainInfo info = new TrainInfo();
        SoftAssertions softly = new SoftAssertions();

        try {
            log.info("Начинаем сбор информации о поезде");
            Reporter.log("Начинаем сбор информации о поезде<br/>");

            // Критическая проверка
            WebElement carriageContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getCarriageContainer())));
            assertThat(carriageContainer)
                    .as("Контейнер вагонов (Критическая проверка)")
                    .isNotNull();

            // Получаем все вагоны
            List<WebElement>carriageContainers = seleniumRetry.findElements(By.xpath(xpath.getCarriageContainer()));
            softly.assertThat(carriageContainers)
                    .as("Наличие вагонов")
                    .isNotEmpty();

            if(carriageContainers.isEmpty()){
                log.warn("Вагоны не найдены, возвращаем пустой TrainInfo");
                Reporter.log("Вагоны не найдены<br/>");
                return info;
            }

            // Выбираем случайный вагон
            int actualIndex = selectRandomCarriage(carriageContainers.size());

            // Получаем информацию о вагоне
            String carriageNumber = extractCarriageNumber(actualIndex);
            info.setCarriageNumber(carriageNumber);

            softly.assertThat(carriageNumber)
                    .as("Номер вагона")
                    .isNotEmpty()
                    .containsPattern("\\d+");

            log.info("Выбран вагон №{}", carriageNumber);
            Reporter.log("Выбран вагон №" + carriageNumber + "<br/>");

            // Скроллим к вагону
            scrollToCarriage(actualIndex);

            // Выбираем место
            selectRandomSeat(info,actualIndex,carriageNumber,softly);

            // Собираем общую информацию о поезде
            collectTrainGeneralInfo(info,softly);

            // Финальная проверка
            softly.assertThat(info.getTrainNumber())
                    .as("Номер поезда")
                    .isNotEmpty();

            softly.assertThat(info.getPrice())
                    .as("Цена билета")
                    .isNotEmpty();

            log.info("Информация успешно собрана: поезд {}, вагон {}, место {}",
                    info.getTrainNumber(),info.getCarriageNumber(),info.getPlace());
            Reporter.log(String.format("Информация успешно собрана: поезд %s,вагон %s, место %s<br/>",
                    info.getTrainNumber(),info.getCarriageNumber(),info.getPlace()));
        }catch (Exception e){
            log.error("Критическая ошибка при сборе информации", e);
            Reporter.log("Критическая ошибка при сборе информации: " + e.getMessage() + "<br/>");
            helper.takeScreenshot("Критическая ошибка");
            throw e;
        }finally {
            // Проверяем все накопленные ошибки
            try {
                softly.assertAll();
            } catch (AssertionError e){
                log.error("Обнаружены ошибки проверок: {}", e.getMessage());
                Reporter.log("Обнаружены ошибки проверок: " + e.getMessage() + "<br/>");
                helper.takeScreenshot("Ошибки_проверок");
                throw e;
            }
        }
        return info;

    }

    private int selectRandomCarriage(int totalCarriages){
        int randomIndex = random.nextInt(totalCarriages);
        int actualIndex = randomIndex + 1;

        assertThat(actualIndex)
                .as("Индекс выбранного вагона")
                .isBetween(1,totalCarriages);

        log.debug("Выбран вагон с индексом: {} из {}",actualIndex,totalCarriages);
        return actualIndex;
    }

    private String extractCarriageNumber(int carriageIndex) {
        String carriageXPath = xpath.getCarriageNumberWithIndex(carriageIndex);
        WebElement carriageElement = seleniumRetry.findElement(By.xpath(carriageXPath));

        // ✅ Явно указываем, что это ассерт для WebElement
        Assertions.assertThat(carriageElement)
                .as("Элемент номера вагона")
                .isNotNull()
                .matches(WebElement::isDisplayed, "должен отображаться"); // альтернатива isDisplayed()

        return carriageElement.getText();
    }

    private void scrollToCarriage(int carriageIndex){
        String carriageXPath =  xpath.getCarriageNumberWithIndex(carriageIndex);
        WebElement carriageElement = seleniumRetry.findElement(By.xpath(carriageXPath));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "arguments[0].scrollIntoView({behavior: 'smoth',block: 'center', inline: 'center'});",
                carriageElement
        );
        log.debug("Выполнен скролл к вагону с индексом {}", carriageIndex);
    }

    private void selectRandomSeat(TrainInfo info, int carriageIndex, String carriageNumber,
                                  SoftAssertions softly) {
        String availablePlacesXPath = String.format(
                "//ul[@role='tablist']/li[%d]//*[contains(@class, 'Place_available')]",
                carriageIndex
        );

        try {
            List<WebElement> availablePlaces = seleniumRetry.findElements(
                    By.xpath(availablePlacesXPath)
            );

            softly.assertThat(availablePlaces)
                    .as("Доступные места в вагоне " + carriageNumber)
                    .overridingErrorMessage("В вагоне %s нет доступных мест", carriageNumber)
                    .isNotEmpty();

            if (availablePlaces.isEmpty()) {
                return;
            }

            int randomPlaceIndex = random.nextInt(availablePlaces.size());
            WebElement selectedPlace = availablePlaces.get(randomPlaceIndex);

            // ✅ ИСПРАВЛЕНО: проверяем свойства WebElement через булевы методы
            softly.assertThat(selectedPlace)
                    .as("Выбранное место в вагоне " + carriageNumber)
                    .isNotNull();

            softly.assertThat(selectedPlace.isDisplayed())
                    .as("Место должно отображаться на странице")
                    .isTrue();

            softly.assertThat(selectedPlace.isEnabled())
                    .as("Место должно быть доступно для выбора")
                    .isTrue();

            log.info("💺 Выбрано место с индексом: {} из {}",
                    randomPlaceIndex, availablePlaces.size());
            Reporter.log(String.format("💺 Выбрано место с индексом: %d из %d<br/>",
                    randomPlaceIndex, availablePlaces.size()));

            // Скриншот до клика
            helper.takeScreenshot("До_выбора_места_вагон_" + carriageNumber);

            String placeNumber = extractPlaceNumber(selectedPlace);

            softly.assertThat(placeNumber)
                    .as("Номер места")
                    .isNotEmpty()
                    .matches("\\d+");

            log.info("Номер места: {}", placeNumber);
            Reporter.log("Номер места: " + placeNumber + "<br/>");

            // Кликаем с retry
            seleniumRetry.click(selectedPlace);
            log.info("👆 Кликнули на место: {}", placeNumber);
            Reporter.log("👆 Кликнули на место: " + placeNumber + "<br/>");

            info.setPlace(placeNumber);

            // Скриншот после клика
            helper.takeScreenshot("После_выбора_места_" + placeNumber);

        } catch (Exception e) {
            log.error("Ошибка при выборе места в вагоне {}", carriageNumber, e);
            Reporter.log("❌ Ошибка при выборе места: " + e.getMessage() + "<br/>");
            softly.fail("Ошибка при выборе места: " + e.getMessage());
        }
    }

    private String extractPlaceNumber(WebElement placeElement) {
        String placeId = placeElement.getAttribute("id");

        assertThat(placeId)
                .as("ID элемента места")
                .isNotEmpty()
                .contains("tier/place-");

        return placeId.replace("tier/place-", "");
    }

    private void collectTrainGeneralInfo(TrainInfo info, SoftAssertions softly) {
        log.debug("Начинаем сбор общей информации о поезде");

        safeSet(info::setArrivalStation, xpath.getArrivalStation(), "станция прибытия", softly);
        safeSet(info::setDepartureStation, xpath.getDepartureStation(), "станция отправления", softly);
        safeSet(info::setTrainNumber, xpath.getTrainNumber(), "номер поезда", softly);
        safeSet(info::setDepartureTime, xpath.getDepartureDateTime(), "время отправления", softly);
        safeSet(info::setDepartureAndArrivalCity, xpath.getDepartureAndArrivalCity(), "маршрут", softly);
        safeSet(info::setArrivalTime, xpath.getArrivalTime(), "время прибытия", softly);
        safeSet(info::setDateDeparture, xpath.getDepartureDateDate(), "дата отправления", softly);
        safeSet(info::setDateArrival, xpath.getArrivalDate(), "дата прибытия", softly);
        safeSet(info::setPrice, xpath.getPrice(), "цена", softly);
        safeSet(info::setTravelTime, xpath.getTravelTime(), "время в пути", softly);

        log.debug("Сбор общей информации завершен");
    }

    private void safeSet(Consumer<String> setter, String xpathExpression, String fieldName,
                         SoftAssertions softly) {
        try {
            String text = seleniumRetry.getText(By.xpath(xpathExpression));

            softly.assertThat(text)
                    .as("Поле '" + fieldName + "'")
                    .isNotEmpty();

            setter.accept(text);
            log.debug("Установлено поле '{}': {}", fieldName, text);

        } catch (Exception e) {
            log.warn("Не удалось получить {}: {}", fieldName, e.getMessage());
            softly.fail("Не удалось получить поле '" + fieldName + "': " + e.getMessage());
        }
    }

    @Step("Проверка наличия Shadow DOM")
    public boolean hasShadowRoot(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRoot = (WebElement) js.executeScript("return arguments[0].shadowRoot", element);
        boolean hasShadow = shadowRoot != null;

        assertThat(shadowRoot)
                .as("Shadow DOM элемент")
                .isNotNull();

        log.debug("Элемент {} Shadow DOM", hasShadow ? "имеет" : "не имеет");
        return hasShadow;
    }

    @Step("Сделать скриншот: {screenshotName}")
    public byte[] takeScreenshot(String screenshotName) {
        byte[] screenshot = helper.takeScreenshot(screenshotName);

        assertThat(screenshot)
                .as("Скриншот " + screenshotName)
                .isNotNull()
                .isNotEmpty();

        return screenshot;
    }
        }
