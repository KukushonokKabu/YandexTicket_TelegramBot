package core;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.annotations.*;
import ru.mydomain.Xpath;
import utils.reporters.DetailedTelegramReporter;
import utils.reporters.TelegramReporter;
import utils.generators.TestDataGenerator;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Listeners({io.qameta.allure.testng.AllureTestNg.class})
@Epic("Yandex Ticket Bot")
@Feature("Валидация локаторов")
public class BaseTest {

    protected static WebDriver driver;
    protected Xpath xpath;
    protected static WebDriverWait wait;
    private static long suiteStartTime;
    protected static BrowserMobProxy proxy;

    @BeforeSuite
    public void beforeSuite(ITestContext context){
        suiteStartTime = System.currentTimeMillis();
        TelegramReporter.sendSimpleMessage("\uD83D\uDE80 Запуск тестовой серии...");
    }

    @AfterSuite
    public void afterSuite(ITestContext context){
        long duration = System.currentTimeMillis() - suiteStartTime;


        // Отправка отчета в Telegram
        DetailedTelegramReporter.sendAllureDetailedReport(context, duration);
    }

    @BeforeClass
    public static void setUpClass(){
        if(driver == null){
            // === ТОЛЬКО БАЗОВАЯ ИНИЦИАЛИЗАЦИЯ БЕЗ PROXY ===
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            System.out.println("🚀 Драйвер инициализирован (Proxy будет запущен при необходимости)");
        }
    }

    // === ОТДЕЛЬНЫЙ МЕТОД ДЛЯ ЗАПУСКА PROXY ===
    @Step("Запуск BrowserMob Proxy для перехвата трафика")
    protected void startProxyIfNeeded() {
        if (proxy == null || !proxy.isStarted()) {
            try {
                proxy = new BrowserMobProxyServer();
                proxy.setTrustAllServers(true); // ДО start()!
                proxy.start(0);
                proxy.newHar("yandex-train-har");
                Allure.step("✅ BrowserMob Proxy запущен");
            } catch (Exception e) {
                Allure.step("⚠️ Не удалось запустить Proxy: " + e.getMessage());
            }
        }
    }



    @BeforeMethod
    public void setUpMethod(Method method){
        xpath = new Xpath();
        System.out.println("\uD83D\uDD27 Настройка для теста: "+ method.getName());
    }

    @AfterClass
    public static void tearDownClass(){
        if(proxy != null){
            proxy.stop();}
        if(driver != null){
            try {
                driver.quit();
                driver = null;
                wait = null;
                proxy = null;
                System.out.println("\uD83D\uDD1A Драйвер и Proxy закрыты после всех тестов");
            }
            catch (Exception e){
                System.err.println("Ошибка при закрытии драйвера: "+ e.getMessage());
            }
        }
    }

    // Методы для гибридного тестирования

    @Step("Перехват API  запросов для: {apiPattern}")
    protected List<HarEntry> captureApiCalls(String apiPattern){
        Har har = proxy.getHar();
        List<HarEntry>apiCalls = new ArrayList<>();

        for(HarEntry entry : har.getLog().getEntries()){
            if(entry.getRequest().getUrl().contains(apiPattern)){
                apiCalls.add(entry);
                Allure.step("\uD83D\uDCE1 Перехвачен API вызов: "+ entry.getRequest().getMethod()+" "+ entry.getRequest().getUrl());
            }
        }
        return apiCalls;
    }

    @Step("Выполнение API запроса: {url}")
    protected Response executeApiRequest(String url,String method, String body){
        return given()
                .contentType("application/json")
                .body(body)
                .when()
                .request(method,url);
    }

    @Step("Сравнение результатов API  и UI")
    protected void compareUiAndApiResults(List<WebElement>uiElements, Response apiResponse){
        try {
            int uiCount = uiElements.size();
            int apiCount = apiResponse.jsonPath().getList("").size();

            Allure.step(String.format("UI  результатов: %d, API результатов: %d",uiCount,apiCount));
        }
        catch (Exception e){
            Allure.step("⚠\uFE0F Сравнение не удалось: "+ e.getMessage());
        }
    }


    // Основные методы

    @Step("Проверка появления подсказок")
    protected void validateSuggestionsAppear(){
        try {
            boolean suggestionVisible = wait.until(driver ->{
                List<WebElement>suggestions = driver.findElements(By.xpath("//div[@class='EhCXF _274Q5']//div[@class='GxV0a']"));
                return suggestions.stream().anyMatch(WebElement::isDisplayed);
            });

            assertThat(suggestionVisible)
                    .as("✅ Подсказки должны появляться при вводе текста")
                    .isTrue();

            Allure.step("Подсказки успешно появились ");
        }
        catch (Exception e){
            Allure.step("❌ Ошибка при проверке подсказок"+ e.getMessage());
            throw e;
        }

    }

    @Step("Проверка функциональности поля очистки")
    protected void validateClearButtonFunctionality(){
        try {
            WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getButtonClear())));

            assertThat(clearButton.isDisplayed())
                    .as("Кнопка очистки должна быть видимой")
                    .isTrue();

            assertThat(clearButton.isEnabled())
                    .as("Кнопка очистки должна быть активноой")
                    .isTrue();

            Allure.step("✅ Кнопка очистки - OK (visible : true , enabled : true)");
        }
        catch (Exception e){
            Allure.step("❌ Ошибка при проверке кнопки очистки: "+ e.getMessage());
            throw e;
        }
    }

    //============== Вспомогательные методы ================

    @Step("Клик по элементу : {elementName}")
    protected void clickElement(By locator, String elementName){
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            Allure.step("✅ Успешно кликнули на : "+ elementName);
        }
        catch (Exception e){
            Allure.step("❌ Ошибка клика на "+ elementName + ":"+ e.getMessage());
            throw e;
        }
    }

    @Step("Ввод текста '{text}'  в поле : {fieldName}")
    protected void inputText(By locator, String text,String fieldName){
        try {
            WebElement field = driver.findElement(locator);
            field.clear();
            field.sendKeys(text);
            Allure.step("✅ Успешно ввели '"+ text+ "' в поле: "+ fieldName);
        }
        catch (Exception e){
            Allure.step("❌ Ошибка ввода текста в "+  fieldName+ ": "+ e.getMessage());
            throw e;
        }
    }
    @Step("Ввод текста '{text}'с эмуляцией человеческого поведения , в поле: {fieldName}")
    protected void humanLikeInput(By locator,String text,String fieldName){
        try {
            WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

            // Кликаем перед вводом(имитация пользователя)
            field.click();
            Thread.sleep(200);

            // Очистка по-человечески
            field.sendKeys(Keys.CONTROL+ "a");
            field.sendKeys(Keys.BACK_SPACE);
            Thread.sleep(300);

            //Медленный ввод
            for(char c : text.toCharArray()){
                field.sendKeys(String.valueOf(c));
                Thread.sleep(150 + ThreadLocalRandom.current().nextInt(100));
            }

            Allure.step("✅ Человеко-подобный ввод в :"+ fieldName);
        }
        catch (Exception e){
            Allure.step("❌  Ошибка человеко-подобного ввода: "+ e.getMessage());
        }
    }
    protected void openTrainPage(){
        driver.get("https://travel.yandex.ru/trains/");
        waitForPageLoad();
        Allure.step("Открыта страница поиска билетов");
    }

    @Step("Получение значения поля : {fieldXpath}")
    protected String getFieldValue(String fieldXpath){
        try {
            WebElement field = driver.findElement(By.xpath(fieldXpath));
            String value = field.getAttribute("value");
            Allure.step("Значение поля :'"+ value+"'");
            return value;
        }
        catch (Exception e){
            Allure.step("Ошибка получения значения поля :"+ e.getMessage());
            return null;
        }
    }

    @Step("Проверка текстового поля : {fieldName}")
    protected void validateTextField(String fieldXpath, String fieldName){
        try {
            WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(fieldXpath)));
            boolean isDisplayed = field.isDisplayed();
            boolean isEnabled = field.isEnabled();
            String currentValue = field.getAttribute("value");

            assertThat(isDisplayed)
                    .as(fieldName + " должен быть видимым")
                    .isTrue();
            assertThat(isEnabled)
                    .as(fieldName+ " должен быть доступен для ввода ")
                    .isTrue();

            Allure.step(String.format("$s - visible: %s, enabled: %s, value: '%s'",
                    fieldName,isDisplayed,isEnabled,currentValue));
        }
        catch (Exception e){
            Allure.step("Ошибка проверки поля :"+ fieldName+ ": "+ e.getMessage());
            throw e;
        }
    }
    protected void waitForPageLoad(){
        try {
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
        }
        catch (Exception e){
            System.out.println("⚠\uFE0F  Страница загружен но readyState не complete");
        }
    }

    protected void clearField(String fieldXpath){
        try {
            WebElement field = driver.findElement(By.xpath(fieldXpath));
            field.clear();
            field.sendKeys(Keys.CONTROL+ "a");
            field.sendKeys(Keys.DELETE);
            Allure.step("Поле очищено");
        }
        catch (Exception e){
            System.err.println("Ошибка очистки поля :"+ e.getMessage());
        }
    }

    protected void selectDateInCalendar(){
        try {
            Allure.step("=== Выбор даты в календаре ===");

            // Генерируем тестовую дату и xpath
            Map<String,String> dateData = TestDataGenerator.getDateAndXpath();
            String calendarXpath = dateData.get("xpath");
            String displayDate = dateData.get("displayDate");

            Allure.step("Сгенерированная дата для  выбора: "+ displayDate);
            Allure.step("Xpath элемента: "+ calendarXpath);

            // Клик по полю календаря чтоб открыть его
            Allure.step("Открытие календаря");
            clickElement(By.xpath(xpath.getCalendar()),"Поле календаря");

            Allure.step("Ожидание загрузки календаря");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("\"//div[contains(@class, 'calendar') or contains(@data-qa, 'calendar')]")));

            // Поиск и клик по нужной дате
            Allure.step("Поиск элемента даты: "+ displayDate);
            WebElement dateElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(calendarXpath)));

            //  Проверяем что дата доступна для выбора (не заблокирована)
            assertThat(dateElement.isEnabled())
                    .as("Дата "+ displayDate+ "должна быть доступна для выбора")
                    .isTrue();

            assertThat(dateElement.isDisplayed())
                    .as("Дата "+ displayDate + " должна быть видимой")
                    .isTrue();

            Allure.step("Клик по дате: "+ displayDate);
            dateElement.click();

            // Проверяем что календарь успешно закрылся
            Allure.step("Ожидание закрытия календаря");
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[contains(@class, 'calendar-open')]")));
            Allure.step("✅ Дата успешно выбрана: "+ displayDate);
        }
        catch (Exception e){
            Allure.step("❌ Ошибка выбора даты: "+ e.getMessage());
            throw e;
        }
    }
    protected void selectDateInCalendarWithValidation() {
        try {
            Allure.step("=== ВЫБОР ДАТЫ С ПРОВЕРКОЙ ДОСТУПНОСТИ ===");

            // Пытаемся выбрать дату несколько раз если первая недоступна
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    Map<String, String> dateData = TestDataGenerator.getDateAndXpath();
                    String calendarXpath = dateData.get("xpath");
                    String displayDate = dateData.get("displayDate");

                    Allure.step(String.format("Попытка %d: выбор даты %s", attempt, displayDate));

                    // Открываем календарь
                    clickElement(By.xpath(xpath.getCalendar()), "Поле календаря");

                    // Ждем появления элементов календаря
                    wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//div[contains(@data-qa, 'calendar-day-')]")
                    ));

                    // Проверяем существует ли элемент даты
                    List<WebElement> dateElements = driver.findElements(By.xpath(calendarXpath));
                    if (dateElements.isEmpty()) {
                        Allure.step("Дата " + displayDate + " не найдена, пробуем другую");
                        continue;
                    }

                    WebElement dateElement = dateElements.get(0);

                    // Проверяем что дата не заблокирована
                    if (!dateElement.isEnabled() ||
                            dateElement.getAttribute("class").contains("disabled") ||
                            dateElement.getAttribute("aria-disabled") != null) {
                        Allure.step("Дата " + displayDate + " недоступна, пробуем другую");
                        continue;
                    }

                    // Кликаем по дате
                    dateElement.click();

                    // Проверяем что календарь закрылся
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                            By.xpath("//div[starts-with(@class,'TravelPopup')]")
                    ));

                    Allure.step("✅ Успешно выбрана дата: " + displayDate);
                    return;

                } catch (Exception e) {
                    Allure.step("❌ Попытка " + attempt + " не удалась: " + e.getMessage());
                  //  closeCalendarIfOpen();
                    if (attempt == 3) throw e;
                }
            }

            throw new RuntimeException("Не удалось выбрать доступную дату после 3 попыток");

        } catch (Exception e) {
            Allure.step("❌ Критическая ошибка выбора даты: " + e.getMessage());
            throw e;
        }
    }
    /**
     * Ожидает изменения URL от исходного
     */
    protected void waitForUrlChange(String initialUrl) {
        try {
            Allure.step("Ожидание изменения URL");
            wait.until(driver -> {
                String currentUrl = driver.getCurrentUrl();
                boolean changed = !currentUrl.equals(initialUrl);
                if (!changed) {
                    Allure.step("Текущий URL еще не изменился: " + currentUrl);
                }
                return changed;
            });
            Allure.step("✅ URL успешно изменился");
        } catch (Exception e) {
            Allure.step("❌ URL не изменился в течение ожидания: " + e.getMessage());
            throw e;
        }
    }


    /**
     * Получает текущий URL и логирует в Allure
     */
    protected String getCurrentUrlWithLog() {
        String url = driver.getCurrentUrl();
        Allure.step("Текущий URL: " + url);
        return url;
    }

    /**
     * Ожидает полной загрузки страницы по URL
     */
    protected void waitForPageUrlToBe(String expectedUrl) {
        try {
            Allure.step("Ожидание перехода на URL: " + expectedUrl);
            wait.until(ExpectedConditions.urlToBe(expectedUrl));
            Allure.step("✅ Успешный переход на целевой URL");
        } catch (Exception e) {
            Allure.step("❌ Не удалось перейти на целевой URL: " + e.getMessage());
            throw e;
        }
    }

    @Attachment(value = "Screenshot {screenshotName}", type = "image/png")
    protected byte [] takeScreenshot(String screenshotName){
        try {
return ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
        }
        catch (Exception e){
            System.err.println("Не удалось делать скриншот: "+ e.getMessage());
            return new byte[0];
        }
    }



}
