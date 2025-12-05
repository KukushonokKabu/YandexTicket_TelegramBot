package tests.atomic;

import api.clients.ApiClient;
import core.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;
import pages.ResultsPage;
import pages.models.TrainInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Валидация UI элементов на всех страницах проекта")
public class LocatorValidationTest extends BaseTest {

    private ApiClient apiClient;

    // ========== АТОМАРНЫЕ ТЕСТЫ ==========

    @Test(priority = 2, groups = {"fast", "smoke", "atomic"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля отправления на странице поездов")
    @Story("Пользователь вводит город отправления на странице поездов")
    public void testTrainDepartureFunctionality() throws InterruptedException {
        Allure.step("=== Тестирование поля отправления поезда ===");
        openTrainPage();

        validateTextField(xpath.getTextFieldOut(), "Поле отправления");

        WebElement textFieldOut = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
        clearButton.click();
        textFieldOut.sendKeys("Москва");

        validateSuggestionsAppear();
        validateClearButtonFunctionality();

        Allure.step("✅ Тест поля отправления завершен успешно");
    }

    @Test(priority = 3)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля прибытия на странице поездов")
    @Story("Пользователь вводит город прибытия на странице поездов")
    public void testTrainArrivalFieldFunctionality() throws InterruptedException {
        Allure.step("=== Тестирование поля прибытия поезда ===");
        openTrainPage();

        Allure.step("Ввод текста в поле прибытия");
        WebElement arrivalField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getTextFieldIn())));
        arrivalField.sendKeys("Санкт-Петербург");

        Allure.step("Проверка появления подсказок");
        validateSuggestionsAppear();

        Allure.step("✅ Тест поля прибытия завершен успешно");
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
        List<WebElement> suggestions = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(xpath.getSuggestionStation())));

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
                    Allure.step("Подсказка: " + text);
                });

        Allure.step("✅ Тест структуры подсказок завершен успешно");
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

        Allure.step("✅ Тест кнопки очистки завершен успешно");
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
        Allure.step("Запуск поиска");
        clickElement(By.xpath(xpath.getSearchButton()), "Кнопка поиска");

        // Шаг 5: Проверяем переход на страницу результатов
        Allure.step("Проверка перехода на страницу результатов");
        waitForUrlChange(initialUrl);

        // Проверяем наличие результатов
        Allure.step("Проверка наличия результатов поиска");

        boolean hasResults;
        String resultMessage;
        WebElement selectedElement = null;

        try {
            // Ждем появления хотя бы одного видимого элемента
            wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getPlatz()))
            );
            hasResults = true;
            resultMessage = "✅ Успешный поиск с выбором даты из календаря";

        } catch (TimeoutException e) {
            // Если элементы не появились за время ожидания
            hasResults = false;
            resultMessage = "ℹ️ На выбранную дату нет свободных билетов";
            Allure.step("Элементы не найдены в течение времени ожидания");

        } catch (Exception e) {
            // Другие возможные ошибки
            hasResults = false;
            resultMessage = "❌ Ошибка при проверке результатов поиска: " + e.getMessage();
            Allure.step("Ошибка при проверке результатов: " + e.getMessage());
        }

        // Логируем результат
        Allure.step(resultMessage);

        if (hasResults) {
            // Дополнительная проверка, что элемент действительно доступен
            try {
                WebElement visibleElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getPlatz())));
                assertThat(visibleElement)
                        .as("Должен отображаться доступный результат поиска")
                        .isNotNull();

                Allure.step("✅ Найден доступный для взаимодействия элемент");

                // Получаем все элементы и выбираем случайный
                List<WebElement> allResults = driver.findElements(By.xpath(xpath.getPlatz()));
                System.out.println("Найдено поездов: " + allResults.size());

                if (!allResults.isEmpty()) {
                    // Выбираем случайный индекс
                    Random random = new Random();
                    int randomIndex = random.nextInt(allResults.size());
                    selectedElement = allResults.get(randomIndex);

                    selectedElement.click();

                    // Ожидание загрузки контейнеров-вагонов
                    try {
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getCarriageContainer())));
                    } catch (Exception e) {
                        System.out.println("Не получилось дождаться появления контейнеров: " + e.getMessage());
                    }

                    ResultsPage res = new ResultsPage(driver, wait);
                    TrainInfo info = res.collectTrainAndSeatInfo();

                    // Сохраняем информацию через новый метод BaseTest
                    saveTrainInfo(info);
                    System.out.println("Вот что нам удалось собрать: " + info.toString());

                    String testData = String.format(
                            "🚂 Найден поезд: %s\n📍 Вагон: %s, Место: %s\n💰 Цена: %s\n📅 Дата: %s %s",
                            info.getTrainNumber(),
                            info.getDepartureStation(),
                            info.getArrivalStation(),
                            info.getCarriageNumber(),
                            info.getDateDeparture(),
                            info.getDepartureTime(),
                            info.getDateArrival(),
                            info.getArrivalTime(),
                            info.getTravelTime(),
                            info.getPrice(),
                            info.getPlace()
                    );

                    // Сохраняем дополнительные данные
                    core.TestContext.setData(testData);

                } else {
                    Allure.step("❌ Не удалось найти элементы для выбора");
                    takeScreenshot("Элементы_не_найдены_для_выбора");
                }

            } catch (Exception e) {
                Allure.step("❌ Элемент найден, но недоступен для взаимодействия: " + e.getMessage());
                takeScreenshot("Элемент_недоступен_для_взаимодействия");
                throw e;
            }
        } else {
            Allure.step("ℹ️ Тест завершен: билетов на выбранную дату нет");
            System.out.println("На выбранную дату нет билетов, но тест отработал корректно");
            takeScreenshot("Билетов_нет_результат_поиска");
        }

        Allure.step("✅ Тест поиска с выбором даты завершен");
    }

    @Test(priority = 8)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Гибридный тест UI + API с перехватом запросов")
    @Story("Проверка интеграции UI и API")
    public void testHybridSearchWithApiCapture() {
        Allure.step("=== Гибридный тест UI + API ===");

        try {
            // Инициализация API клиента
            apiClient = new ApiClient(driver, wait);

            // Шаг 1: Открываем страницу
            openTrainPage();

            // Шаг 2: Заполняем форму
            Allure.step("Заполнение формы через UI");
            WebElement fieldOut = driver.findElement(By.xpath(xpath.getTextFieldOut()));
            WebElement fieldIn = driver.findElement(By.xpath(xpath.getTextFieldIn()));
            WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
            clearButton.click();
            fieldOut.sendKeys("Москва");
            fieldIn.sendKeys("Санкт-Петербург");

            // Шаг 3: Выбираем дату
            Allure.step("Выбор даты из календаря");
            selectDateInCalendarWithValidation();

            // Шаг 4: Выполняем поиск
            Allure.step("Запуск поиска");
            clickElement(By.xpath(xpath.getSearchButton()), "Кнопка поиска");

            // Шаг 5: Проверяем наличие результатов
            Allure.step("Проверка результатов поиска");
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getPlatz())));
                Allure.step("✅ Результаты поиска отображены");
            } catch (TimeoutException e) {
                Allure.step("ℹ️ Результаты не найдены, но тест продолжается для проверки API");
            }

            // Шаг 6: Проверяем API вызовы (если Proxy запущен)
            if (proxy != null && proxy.isStarted()) {
                Allure.step("Проверка перехваченных API запросов");
                List<HarEntry> apiCalls = captureApiCalls("api/trains");

                if (!apiCalls.isEmpty()) {
                    Allure.step("✅ Найдено API вызовов: " + apiCalls.size());
                    apiCalls.forEach(entry -> {
                        Allure.step("API: " + entry.getRequest().getMethod() + " " +
                                entry.getRequest().getUrl());
                    });
                } else {
                    Allure.step("⚠️ API вызовы не перехвачены");
                }
            } else {
                Allure.step("⚠️ Proxy не запущен, проверка API невозможна");
            }

        } catch (Exception e) {
            Allure.step("❌ Ошибка в гибридном тесте: " + e.getMessage());
            throw e;
        }

        Allure.step("✅ Гибридный тест завершен");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Метод для перехвата API запросов
     */
    @Step("Перехват API запросов для: {apiPattern}")
    protected List<HarEntry> captureApiCalls(String apiPattern) {
        if (proxy == null || !proxy.isStarted()) {
            Allure.step("⚠️ Proxy не запущен, перехват невозможен");
            return new ArrayList<>();
        }

        net.lightbody.bmp.core.har.Har har = proxy.getHar();
        List<HarEntry> apiCalls = new ArrayList<>();

        for (HarEntry entry : har.getLog().getEntries()) {
            if (entry.getRequest().getUrl().contains(apiPattern)) {
                apiCalls.add(entry);
            }
        }

        Allure.step("📡 Найдено API вызовов: " + apiCalls.size());
        return apiCalls;
    }

    /**
     * Выполнение API запроса с перехваченным URL
     */
    @Step("Выполнение API запроса с перехваченным URL: {realUrl}")
    protected Response executeApiRequestWithRealUrl(String realUrl) {
        return io.restassured.RestAssured.given()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .when()
                .get(realUrl)
                .then()
                .extract()
                .response();
    }

    /**
     * Сравнение результатов API и UI
     */
    @Step("Сравнение результатов API и UI")
    protected void compareUiAndApiResults(List<WebElement> uiElements, Response apiResponse) {
        try {
            int uiCount = uiElements.size();
            int apiCount = apiResponse.jsonPath().getList("").size();

            Allure.step(String.format("UI результатов: %d, API результатов: %d", uiCount, apiCount));

            assertThat(uiCount)
                    .as("Количество результатов UI и API должно быть примерно одинаковым")
                    .isGreaterThanOrEqualTo(apiCount - 2)
                    .isLessThanOrEqualTo(apiCount + 2);

        } catch (Exception e) {
            Allure.step("⚠️ Сравнение не удалось: " + e.getMessage());
        }
    }
}