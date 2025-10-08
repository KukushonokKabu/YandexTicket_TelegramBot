import io.qameta.allure.*;
import kotlin.jvm.Strictfp;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Валидация UI  элементов на всех страницах проекта")


public class LocatorValidationTest extends BaseTest{
    @Test(priority = 1)
    @Severity(SeverityLevel.BLOCKER)
    @Description("Проверка доступности основных элементов на странице ")
    @Story("Пользователь открывает страницу поиска ж/д билетов")
    public void testTrainPageElements(){
        Allure.step("===Тестирование страницы поездов===");
        openTrainPage();

        Allure.step("Проверка заголовка страницы поездов");
        assertThat(driver.getTitle())
                .as("Заголовок страницы должен содержать ключевые слова")
                .containsIgnoringCase("Яндекс")
                .containsIgnoringCase("билеты");

        Allure.step("Проверка основных полей ввода на странице поездов");
        validateTextField(xpath.getTextFieldOut(),"Поле отправления (поезда)");
        validateTextField(xpath.getTextFieldIn(),"Поле прибытия (поезда)");

        Allure.step("Проверка начального состояния полей на странице поездов");
        assertThat(getFieldValue(xpath.getTextFieldOut()))
                .as("Поле отправления не должно быть пустым при загрузке")
                .isNotEmpty();
        assertThat(getFieldValue(xpath.getTextFieldIn()))
                .as("Поле прибытия должно быть пустым при загрузке")
                .isNullOrEmpty();

        Allure.step("Проверка дополнительных элементов на странице поездов");
        assertThat(driver.findElement(By.xpath(xpath.getCalendar())).isDisplayed())
                .as("Календарь должен быть видимым")
                .isTrue();

        assertThat(driver.findElement(By.xpath(xpath.getSearchButton())).isEnabled())
                .as("Кнопка поиска должна быть активной")
                .isTrue();
    }

    @Test(priority = 2)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля отправления на странице поездов")
    @Story("Пользователь вводит город отправления на странице поездов")
    public void testTrainDepartureFunctionality(){
        Allure.step("=== Тестирование поля отправления поезда ===");
        openTrainPage();

        Allure.step("Ввод текста в поле отправления");
        WebElement departureField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getTextFieldOut())));

        // Очистка поля
        clearField(xpath.getTextFieldOut());
        departureField.sendKeys("Москва");

        assertThat(departureField.getAttribute("value"))
                .as("Поле должно содержать введенный  текст")
                .isEqualTo("Москва");

        Allure.step("Проверка появления подсказок");
        validateSuggestionsAppear();

        Allure.step("Проверка кнопки очистки");
        validateClearButtonFiunctionality();
    }
    @Test(priority = 3)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля прибытия на странице поездов")
    @Story("Пользователь вводит город прибытия на странице поездов")
    public void testTrainArrivalFieldFunctionality(){
        Allure.step("=== Тестирование поля прибытия поезда ===");
        openTrainPage();

        Allure.step("Ввод текста в поле прибытия");
        WebElement arrivalField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getTextFieldIn())));
        arrivalField.sendKeys("Санкт-Петербург");

        Allure.step("Проверка появления подсказок");
        validateSuggestionAppear();
    }
    @Test(priority = 4)
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка структуры подсказок на странице поездов")
    @Story("Система показывает подсказки при вводе на странице поездов")
    public void testTrainSuggestionStructure(){
        Allure.step("=== Тестирование подсказок ===");
        openTrainPage();

        Allure.step("Ввод текста для появления подсказок");
        WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        clearField(xpath.getTextFieldOut());
        departureField.sendKeys("Москва");

        Allure.step("Ожидание и проверка подсказок");
        List<WebElement>suggestions = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@class='EhCXF _274Q5']//div[@class='GxV0a']")));

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
                    Allure.step("Подсказка :"+ text);
                });
    }

    @Test(priority = 5)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка кнопки очистки поля на странице поездов")
    @Story("Пользователь очищает поле ввода на странице поездов")
    public void testTrainClearButtonFunctionality(){
        Allure.step("=== Тестирование кнопки очистки поля для ввода города отправления");
        openTrainPage();

        Allure.step("Заполнение поля текстом");
        WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        clearField(xpath.getTextFieldOut());
        departureField.sendKeys("Тестовый текст");

        Allure.step("Поиск и нажатие кнопки очистки");
        WebElement clearButton  = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getButtonClear())));
        clearButton.click();

        Allure.step("Проверка очистки поля");
        assertThat(departureField.getAttribute("value"))
                .as("Поле должно быть очищено после нажатия кнопки")
                .isNullOrEmpty();


    }


}
