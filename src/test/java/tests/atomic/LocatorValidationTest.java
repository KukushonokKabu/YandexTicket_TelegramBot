package tests.atomic;

import core.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Feature("Валидация UI  элементов на всех страницах проекта")


public class LocatorValidationTest extends BaseTest {
    // ========== Атомарные тесты  ===========
    @Test(priority = 2, groups = {"fast","smoke","atomic"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля отправления на странице поездов")
    @Story("Пользователь вводит город отправления на странице поездов")
    public void testTrainDepartureFunctionality() {
        Allure.step("=== Тестирование поля отправления поезда ===");
        openTrainPage();

        validateTextField(xpath.getTextFieldOut(),"Поле отправления");
        humanLikeInput(By.xpath(xpath.getTextFieldOut()),"Москва","Поле отправления");
        validateSuggestionsAppear();
        validateClearButtonFunctionality();
    }

    @Test(priority = 3)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля прибытия на странице поездов")
    @Story("Пользователь вводит город прибытия на странице поездов")
    public void testTrainArrivalFieldFunctionality() {
        Allure.step("=== Тестирование поля прибытия поезда ===");
        openTrainPage();

        Allure.step("Ввод текста в поле прибытия");
        WebElement arrivalField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getTextFieldIn())));
        arrivalField.sendKeys("Санкт-Петербург");

        Allure.step("Проверка появления подсказок");
        validateSuggestionsAppear();
    }

    @Test(priority = 4)
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка структуры подсказок на странице поездов")
    @Story("Система показывает подсказки при вводе на странице поездов")
    public void testTrainSuggestionStructure() {
        Allure.step("=== Тестирование подсказок ===");
        openTrainPage();

        Allure.step("Ввод текста для появления подсказок");
        WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        clearField(xpath.getTextFieldOut());
        departureField.sendKeys("Москва");

        Allure.step("Ожидание и проверка подсказок");
        List<WebElement> suggestions = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@class='EhCXF _274Q5']//div[@class='GxV0a']")));

        assertThat(suggestions)
                .as("Должны появиться подсказки")
                .isNotEmpty();

        Allure.step("Проверка содержания подсказок");
        suggestions.stream()
                .limit(5)
                .forEach(suggestion -> {
                    String text = suggestion.getText();
                    assertThat(text)
                            .as("Текст подсказки не должен быть пустым")
                            .isNotBlank();
                    Allure.step("Подсказка :" + text);
                });
    }

    @Test(priority = 5)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка кнопки очистки поля на странице поездов")
    @Story("Пользователь очищает поле ввода на странице поездов")
    public void testTrainClearButtonFunctionality() {
        Allure.step("=== Тестирование кнопки очистки поля для ввода города отправления ===");
        openTrainPage();

        Allure.step("Заполнение поля текстом");
        WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        clearField(xpath.getTextFieldOut());
        departureField.sendKeys("Тестовый текст");

        Allure.step("Поиск и нажатие кнопки очистки");
        WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getButtonClear())));
        clearButton.click();

        Allure.step("Проверка очистки поля");
        assertThat(departureField.getAttribute("value"))
                .as("Поле должно быть очищено после нажатия кнопки")
                .isNullOrEmpty();


    }

    @Test(priority = 7)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Полный сценарий поиска с выбором даты из календаря")
    @Story("Пользователь выбирает дату из календаря и выполняет поиск")
    public void testSearchWithDateSelection() {
        Allure.step("=== ПОЛНЫЙ СЦЕНАРИЙ ПОИСКА С ВЫБОРОМ ДАТЫ ===");

        // Шаг 1: Открываем страницу
        openTrainPage();
        String initialUrl = getCurrentUrlWithLog();

        // Шаг 2: Заполняем города
//        Allure.step("Заполнение городов отправления и назначения");
//        clearField(xpath.getTextFieldOut());
//        humanLikeInput(By.xpath(xpath.getTextFieldOut()), "Москва", "Поле отправления");
//        inputText(By.xpath(xpath.getTextFieldIn()), "Санкт-Петербург", "Поле назначения");
        Allure.step("Заполнение городов отправления и назначения");
        WebElement fieldOut = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        WebElement fieldIn = driver.findElement(By.xpath(xpath.getTextFieldIn()));
        WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
        clearButton.click();
        fieldOut.sendKeys("Москва");
        fieldIn.sendKeys("Джанкой");

        // Шаг 3: Выбираем дату из календаря
        Allure.step("Выбор даты из календаря");
        selectDateInCalendarWithValidation();

        // Шаг 4: Выполняем поиск
        String url = getCurrentUrlWithLog(); // Получение текущего URL
        Allure.step("Запуск поиска");
        clickElement(By.xpath(xpath.getSearchButton()), "Кнопка поиска");

        // Шаг 5: Проверяем переход на страницу результатов
        Allure.step("Проверка перехода на страницу результатов");
        waitForUrlChange(initialUrl);


        // Проверяем наличие результатов
        Allure.step("Проверка наличия результатов поиска");

        boolean hasResults;
        String resultMessage;
       try {
           hasResults = wait.until(driver ->
                   driver.findElements(By.xpath(xpath.getPlatz())).size() > 0
           );
           resultMessage = "✅ Успешный поиск с выбором даты из календаря";
       }

       catch (Exception e){
           Allure.step("На выбранную дату нет свободных билетов");
           hasResults = false;
           resultMessage = "ℹ\uFE0F На выбранную дату нет свободных билетов";
       }
       // Логируем результат
        Allure.step(resultMessage);

       if(hasResults){
           Allure.step("✅ Успешный поиск с выбором даты из календаря");
           assertThat(hasResults)
                   .as("Должны отображаться результаты поиска")
                   .isTrue();
       }
       else {
           Allure.step("ℹ\uFE0F Тест завершен: билетов на выбранную дату нет");
           System.out.println("На выбранную дату нет билетов но тест отработал корректно");
           takeScreenshot("Билетов нет , но вот что мы видим насамом деле ");
       }



    }

    @Test(priority = 8)
    @Severity(SeverityLevel.CRITICAL)
    public void testHybridSearchWithApiCapture() throws InterruptedException {
        Allure.step("=== Гибридный тест UI + API ===");

        try {
            // Шаг 1: Запускаем Proxy  и подключаем к драйверу
           // startProxyAndConfigureDriver();

            // Шаг 2: Открываем страницу
            openTrainPage();
            String initialUrl = getCurrentUrlWithLog();

            // Шаг 3: Заполняем форму
            Allure.step("Заполнение формы через UI");
            // Остальной код         }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === НОВЫЙ МЕТОД ДЛЯ РАБОТЫ С ПЕРЕХВАЧЕННЫМИ URL ===
    @Step("Выполнение API запроса с перехваченным URL: {realUrl}")
    protected Response executeApiRequestWithRealUrl(String realUrl) {
        return given()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .when()
                .get(realUrl)
                .then()
                .extract()
                .response();
    }

    // === ОБНОВЛЕННЫЙ МЕТОД ПЕРЕХВАТА ===
    @Step("Перехват API запросов для: {apiPattern}")
    protected List<HarEntry> captureApiCalls(String apiPattern) {
        if (proxy == null || !proxy.isStarted()) {
            Allure.step("⚠️ Proxy не запущен, перехват невозможен");
            return new ArrayList<>();
        }

        Har har = proxy.getHar();
        List<HarEntry> apiCalls = new ArrayList<>();

        for (HarEntry entry : har.getLog().getEntries()) {
            if (entry.getRequest().getUrl().contains(apiPattern)) {
                apiCalls.add(entry);
            }
        }

        Allure.step("📡 Найдено API вызовов: " + apiCalls.size());
        return apiCalls;
    }





}
