package core;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import net.lightbody.bmp.BrowserMobProxy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.*;
import pages.models.TrainInfo;
import ru.mydomain.Xpath;
import utils.helpers.BaseTestHelper;
import utils.helpers.CalendarService;
import utils.reporters.DetailedTelegramReporter;
import utils.reporters.TelegramReporter;
import utils.retry.SeleniumRetry;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Random;


@Listeners({io.qameta.allure.testng.AllureTestNg.class})
@Epic("Yandex Ticket Bot")
@Feature("Базовый класс тестов")
public abstract class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    // ========== ОСНОВНЫЕ ПОЛЯ ==========
    protected SeleniumRetry seleniumRetry;
    protected Random random = new Random();
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Xpath xpath;
    protected BaseTestHelper helper;
    protected CalendarService calendarService;
    protected static BrowserMobProxy proxy;

    private static long suiteStartTime;

    // ========== ANNOTATION METHODS ==========

    @BeforeSuite
    public void beforeSuite(ITestContext context) {
        suiteStartTime = System.currentTimeMillis();
        TelegramReporter.sendSimpleMessage("🚀 Запуск тестовой серии...");
        System.out.println("📊 Начало тестовой серии");
    }

    @BeforeClass
    public void setUpClass() {
        // Инициализация драйвера
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Инициализация вспомогательных классов
        xpath = new Xpath();
        helper = new BaseTestHelper(driver, wait);
        calendarService = new CalendarService(driver, wait, xpath);

        System.out.println("✅ Драйвер и вспомогательные классы инициализированы");
    }

    @BeforeMethod
    public void setUpMethod(Method method) {
        // Очищаем контекст перед каждым тестом
        TestContext.getInstance().clear();
        seleniumRetry = new SeleniumRetry(driver, 3, 500);
        log.info("▶️ Запуск теста: {}", method.getName());
    }

    @AfterClass
    public void tearDownClass() {
        // Останавливаем Proxy если запущен
        if (proxy != null && proxy.isStarted()) {
            proxy.stop();
            System.out.println("🔒 Proxy остановлен");
        }

        // Закрываем драйвер
        DriverFactory.quitDriver();
        System.out.println("🔒 Драйвер закрыт");
    }

    @AfterSuite
    public void afterSuite(ITestContext context) {
        long duration = System.currentTimeMillis() - suiteStartTime;
        System.out.println("📊 Завершение тестовой серии, длительность: " + duration + "ms");

        // Отправка отчета в Telegram
        DetailedTelegramReporter.sendCompleteReport(
                context,
                duration,
                TestContext.getScreenshot()
        );
    }

    // ========== БАЗОВЫЕ МЕТОДЫ ДЛЯ ВСЕХ ТЕСТОВ ==========

    @Step("Открыть страницу поиска поездов")
    protected void openTrainPage() {
        driver.get("https://travel.yandex.ru/trains/");
        helper.waitForPageLoad();
        Allure.step("✅ Открыта страница поиска билетов");
    }

    // ========== ДЕЛЕГИРОВАНИЕ К HELPER ==========

    @Step("Клик по элементу: {elementName}")
    protected void clickElement(By locator, String elementName) {
        helper.clickElement(locator, elementName);
    }

    @Step("Ввод текста '{text}' в поле: {fieldName}")
    protected void inputText(By locator, String text, String fieldName) {
        helper.inputText(locator, text, fieldName);
    }

    @Step("Человеко-подобный ввод '{text}' в поле: {fieldName}")
    protected void humanLikeInput(By locator, String text, String fieldName) throws InterruptedException {
        helper.humanLikeInput(locator, text, fieldName);
    }


    @Step("Сделать скриншот: {screenshotName}")
    protected byte[] takeScreenshot(String screenshotName) {
        return helper.takeScreenshot(screenshotName);
    }

    @Step("Ожидание загрузки страницы")
    protected void waitForPageLoad() {
        helper.waitForPageLoad();
    }

    @Step("Ожидание изменения URL")
    protected void waitForUrlChange(String initialUrl) {
        helper.waitForUrlChange(initialUrl);
    }

    @Step("Получение текущего URL")
    protected String getCurrentUrlWithLog() {
        return helper.getCurrentUrlWithLog();
    }

    // ========== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ ==========

    @Step("Проверка появления подсказок")
    protected void validateSuggestionsAppear() throws InterruptedException {
        helper.validateSuggestionsAppear(xpath.getSuggestionStation());
    }

    @Step("Проверка функциональности кнопки очистки")
    protected void validateClearButtonFunctionality() {
        helper.validateClearButtonFunctionality(xpath.getButtonClear());
    }

    @Step("Проверка текстового поля: {fieldName}")
    protected void validateTextField(String fieldXpath, String fieldName) {
        helper.validateTextField(fieldXpath, fieldName);
    }

    @Step("Выбор даты в календаре")
    protected void selectDateInCalendarWithValidation() {
        calendarService.selectDateInCalendarWithValidation();
    }

    @Step("Очистка поля")
    protected void clearField(String fieldXpath) {
        helper.clearField(fieldXpath);
    }

    @Step("Получение значения поля")
    protected String getFieldValue(String fieldXpath) {
        return helper.getFieldValue(fieldXpath);
    }

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ДАННЫМИ ==========

    /**
     * Сохранить информацию о поезде в контекст
     */
    protected void saveTrainInfo(pages.models.TrainInfo info) {
// Сохраняем в TestContext
        TestContext.setTrainInfo(info);
        TestContext.setData(formatForTelegram(info));

        // Только одно информативное сообщение
        log.info("✅ Данные сохранены: поезд {}, вагон {}, место {}",
                info.getTrainNumber(),
                info.getCarriageNumber(),
                info.getPlace());
    }

    private String formatForTelegram(TrainInfo info){
        return String.format(
                "🚂 Поезд: %s\n📍 Вагон: %s\n💺 Место: %s\n💰 Цена: %s\n📅 Отправление: %s %s",
                info.getTrainNumber(),
                info.getCarriageNumber(),
                info.getPlace(),
                info.getPrice(),
                info.getDateDeparture(),
                info.getDepartureTime()
        );
    }

    private String formatForAllure(TrainInfo info){
        return String.format(
                "Поезд: %s\nМаршрут: %s → %s\nВагон: %s\nМесто: %s\n" +
                        "Отправление: %s %s\nПрибытие: %s %s\nВремя в пути: %s\nЦена: %s",
                info.getTrainNumber(),
                info.getDepartureStation(),
                info.getArrivalStation(),
                info.getCarriageNumber(),
                info.getPlace(),
                info.getDateDeparture(),
                info.getDepartureTime(),
                info.getDateArrival(),
                info.getArrivalTime(),
                info.getTravelTime(),
                info.getPrice()

        );
    }

    /**
     * Получить сохраненную информацию о поезде
     */
    protected pages.models.TrainInfo getSavedTrainInfo() {
        return TestContext.getTrainInfo();
    }

    /**
     * Получить сохраненные данные теста
     */
    protected String getSavedTestData() {
        return TestContext.getData();
    }

    // Вспомогательный метод для клика с retry
    protected void clickWithRetry(By locator, String description) {
        try {
            seleniumRetry.click(locator);
            log.info("Клик на {}", description);
        } catch (Exception e) {
            log.error("Не удалось кликнуть на {}", description);
            takeScreenshot("Ошибка_клика_" + description);
            throw e;
        }
    }

    // Вспомогательный метод для ввода текста
    protected void sendKeysWithRetry(By locator, String text, String description) {
        try {
            WebElement element = seleniumRetry.findElement(locator);
            element.clear();
            element.sendKeys(text);
            log.info("Введен текст в {}", description);
        } catch (Exception e) {
            log.error("Не удалось ввести текст в {}", description);
            takeScreenshot("Ошибка_ввода_" + description);
            throw e;
        }
    }
}