import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.annotations.*;
import ru.mydomain.Xpath;
import utils.DetailedTelegramReporter;
import utils.TelegramReporter;
import utils.TestDataGenerator;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@Listeners({io.qameta.allure.testng.AllureTestNg.class})
@Epic("Yandex Ticket Bot")
@Feature("Валидация локаторов")
public class BaseTest {

    protected static WebDriver driver;
    protected Xpath xpath;
    protected static WebDriverWait wait;
    private static long suiteStartTime;

    @BeforeSuite
    public void beforeSuite(ITestContext context){
        suiteStartTime = System.currentTimeMillis();
        TelegramReporter.sendSimpleMessage("\uD83D\uDE80 Запуск тестовой серии...");
    }

    @AfterSuite
    public void afterSuite(ITestContext context){
        long duration = System.currentTimeMillis() - suiteStartTime;


        // Отправка отчета в Telegram
        // TelegramReporter.sendTestReport(passed,failed,skipped,duration);
        //TelegramReporter.sendDetailedReport(context, duration);
        DetailedTelegramReporter.sendAllureDetailedReport(context, duration);
    }

    @BeforeClass
    public static void setUpClass(){
        if(driver ==null){
            // Автоматическая настройка ChromeDriver  через WebDriverManager
            WebDriverManager.chromedriver().setup();

            ChromeOptions options =  new ChromeOptions();
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            // Для отладки можно отключить headless-режим
            boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless","true"));
            if(isHeadless){
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080");
            }

            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver,Duration.ofSeconds(15));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

            //Allure  метаданные
            Allure.label("testType","locator-validation");
            Allure.label("browser","chrome");
            Allure.label("feature","Yandex Ticket");


            System.out.println("\uD83D\uDE80 Драйвер инициализирован для всех тестов");

        }
    }

    @BeforeMethod
    public void setUpMethod(Method method){
        xpath = new Xpath();
        System.out.println("\uD83D\uDD27 Настройка для теста: "+ method.getName());
    }

    @AfterClass
    public static void tearDownClass(){
        if(driver != null){
            try {
                driver.quit();
                driver = null;
                wait = null;
                System.out.println("\uD83D\uDD1A Драйвер закрыт после всех тестов");
            }
            catch (Exception e){
                System.err.println("Ошибка при закрытии драйвера: "+ e.getMessage());
            }
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
//    @Step("Проверка кнопки поиска 2")
//    protected void testingSearchButton(){
//    }
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
    @Step("Проверка структуры подсказок")
    protected void validateSuggestionStructure(){
        try {
            List<WebElement>suggestions = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@class='EhCXF _274Q5']//div[@class='GxV0a']")));

            assertThat(suggestions)
                    .as("Должны появиться подсказки")
                    .isNotEmpty();

            // Проверяем первые три подсказки
            suggestions.stream()
                    .limit(3)
                    .forEach(suggestion ->{
                        String text = suggestion.getText();
                        assertThat(text)
                                .as("Текст подсказки не должен быть пустым")
                                .isNotBlank();
                        Allure.step("Подсказка :"+ text);
                    });
            Allure.step("✅ Структура подсказок корректна , найдено: "+ suggestions.size()+ " элементов");
        }
        catch (Exception e){
            Allure.step("❌ Ошибка при получении структуры подсказок :"+  e.getMessage());
            throw e;
        }
    }
    //============== Вспомогательные методы ================

    @Step("Клик по элементу : + {elementName}")
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
         //   field.clear();
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

//            // Tab  для потери фокуса
//            field.sendKeys(Keys.TAB);
//            Thread.sleep(500);

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
           // field.clear();
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
            String targetDate = dateData.get("date");
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
                    String targetDate = dateData.get("date");
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
                        //  closeCalendarIfOpen();
                        continue;
                    }

                    WebElement dateElement = dateElements.get(0);

                    // Проверяем что дата не заблокирована
                    if (!dateElement.isEnabled() ||
                            dateElement.getAttribute("class").contains("disabled") ||
                            dateElement.getAttribute("aria-disabled") != null) {
                        Allure.step("Дата " + displayDate + " недоступна, пробуем другую");
                    //    closeCalendarIfOpen();
                        continue;
                    }

                    // Кликаем по дате
                    dateElement.click();

                    // Проверяем что календарь закрылся
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                            By.xpath("//div[contains(@class, 'calendar-container')]")
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
     * Ожидает что URL содержит определенную строку
     */
    protected void waitForUrlContains(String expectedText) {
        try {
            Allure.step("Ожидание что URL содержит: " + expectedText);
            wait.until(ExpectedConditions.urlContains(expectedText));
            Allure.step("✅ URL содержит: " + expectedText);
        } catch (Exception e) {
            Allure.step("❌ URL не содержит '" + expectedText + "': " + e.getMessage());
            throw e;
        }
    }

    /**
     * Ожидает что URL соответствует регулярному выражению
     */
    protected void waitForUrlMatches(String regex) {
        try {
            Allure.step("Ожидание что URL соответствует паттерну: " + regex);
            wait.until(ExpectedConditions.urlMatches(regex));
            Allure.step("✅ URL соответствует паттерну");
        } catch (Exception e) {
            Allure.step("❌ URL не соответствует паттерну: " + e.getMessage());
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

    /**
     * Проверяет что текущий URL содержит определенные параметры
     */
    protected void assertUrlContainsParams(Map<String, String> expectedParams) {
        String currentUrl = driver.getCurrentUrl();
        Allure.step("Проверка параметров URL: " + currentUrl);

        for (Map.Entry<String, String> entry : expectedParams.entrySet()) {
            assertThat(currentUrl)
                    .as("URL должен содержать параметр " + entry.getKey())
                    .contains(entry.getKey() + "=" + entry.getValue());
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
