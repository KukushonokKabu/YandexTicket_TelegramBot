import io.qameta.allure.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Валидация UI  элементов")


public class LocatorValidationTest extends BaseTest{
    //  WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Description("Проверка доступности основных элементов на странице")
    @Story("Пользователь открывает страницу поиска билетов")
    public void testInitialPageElements(){
        openTrainPage();
        WebDriverWait wait = createWait(10);

        Allure.step("Проверка заголовка страницы");
        assertThat(driver.getTitle())
                .as("Заголовок страницы должен содержать ключевые слова")
                .containsIgnoringCase("Яндекс")
                .containsIgnoringCase("билеты");

        Allure.step("Проверка основных полей ввода");
        validateTextField(xpath.getTextFieldOut(),"Поле отправления");
        validateTextField(xpath.getTextFieldIn(),"Поле прибытия");

        Allure.step("Проверка начального состояния полей");
        assertThat(getFieldValue(xpath.getTextFieldOut()))
                .as("Поле отправления не должно быть пустым при загрузке")
                .isNotEmpty();

        assertThat(getFieldValue(xpath.getTextFieldIn()))
                .as("Поле прибытия должно быть пустым при загрузке")
                .isNullOrEmpty();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля отправления")
    @Story("Пользователь вводит город отправления")
    public void testDepartureFunctionality(){
        openTrainPage();
        Allure.step("Ввод текста в поле отправления");
        WebElement departureField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getTextFieldOut())));
        // Очистка поля ввода города отправления
        WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getButtonClear())));
        clearButton.click();

        departureField.sendKeys("Москва");

        assertThat(departureField.getAttribute("value"))
                .as("Поле должно содержать введенный текст")
                .isEqualTo("Москва");

        Allure.step("Проверка появления подсказок");
        validateSuggestionsAppear();

        Allure.step("Проверка кнопки очистки");
        validateClearButtonFunctionality();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка функциональности поля прибытия")
    @Story("Пользователь вводит город прибытия")
    public void testArrivalFieldFunctionality(){
        openTrainPage();
        Allure.step("Ввод текста в поле прибытия ");
        WebElement arrivallField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getTextFieldIn())));
        arrivallField.sendKeys("Санкт-Петербург");

        assertThat(arrivallField.getAttribute("value"))
                .as("Поле должно содержать введенный текст")
                .isEqualTo("Санкт-Петербург");

        Allure.step("Проверка появления подсказок");
        validateSuggestionsAppear();
    }
    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка структуры подсказок")
    @Story("Система показывает подсказки при вводе")
    public void testSuggestionStructure(){
        openTrainPage();
        Allure.step("Ввод текста для появления подсказок");
        WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        WebElement clearButton = driver.findElement(By.xpath(xpath.getButtonClear()));
        clearButton.click();
        departureField.sendKeys("Москва");

        Allure.step("Ожидание подсказок");
        List<WebElement>suggestions = driver.findElements(By.xpath("//div[@class='EhCXF _274Q5']"));

        assertThat(suggestions)
                .as("Должны появиться подсказки")
                .isNotEmpty();

        Allure.step("Проверка содержания подсказок");
        suggestions.stream()
                .limit(3)
                .forEach(suggestion->{
                    String text = suggestion.getText();
                    assertThat(text)
                            .as("Текст подсказки не должен быть пустым")
                            .isNotBlank();

                    Allure.step("Подсказка :"+ text);
                });

    }
    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка кнопки очистки поля")
    @Story("Пользователь очищает поле ввода")
    public void testClearButtonFunctionality(){
        openTrainPage();

        Allure.step("Заполнение поля текстом");
        WebElement departureField = driver.findElement(By.xpath(xpath.getTextFieldOut()));
        departureField.sendKeys("Тестовый текст");

        Allure.step("Поиск и нажатие кнопки очистки");
        WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getButtonClear())));
        clearButton.click();

        Allure.step("Проверка очистки поля");
        assertThat(departureField.getAttribute("value"))
                .as("Поле должно быть очищено после нажатия кнопки")
                .isNullOrEmpty();

    }



    @Step("Проверка появления подсказок")
    private void validateSuggestionsAppear(){
        boolean suggestionVisible = wait.until(driver1 -> {
            List<WebElement> suggestions  = driver.findElements(By.xpath("//div[@class='EhCXF _274Q5']"));
            return  suggestions.stream().anyMatch(WebElement ::isDisplayed);
        });

        assertThat(suggestionVisible)
                .as("Подсказки должны появляться при вводе текста")
                .isTrue();

        Allure.step("Подсказки успешно появились");

        // Дописать остальные проверки
    }
    @Step("Проверка кнопки очистки")
    private void validateClearButtonFunctionality(){
        WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath.getButtonClear())));

        assertThat(clearButton.isDisplayed())
                .as("Кнопка очистки должна быть видимой")
                .isTrue();

        assertThat(clearButton.isEnabled())
                .as("Кнопка очистки должна быть активной")
                .isTrue();

        Allure.step("Кнопка очистки - OK(visible: true , enabled : true)");

    }
}
