package utils.helpers;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.mydomain.Xpath;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для работы с календарем
 */
public class CalendarService {

    private WebDriver driver;
    private WebDriverWait wait;
    private Xpath xpath;
    private BaseTestHelper helper;

    public CalendarService(WebDriver driver, WebDriverWait wait, Xpath xpath) {
        this.driver = driver;
        this.wait = wait;
        this.xpath = xpath;
        this.helper = new BaseTestHelper(driver, wait);
    }

    @Step("Выбор даты в календаре из доступных")
    public void selectDateInCalendarWithValidation() {
        try {
            Allure.step("📅 Выбор даты в календаре из доступных");

            // Открываем календарь
            helper.clickElement(By.xpath(xpath.getCalendar()), "Поле календаря");

            // Ждём появления календаря
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(xpath.getPriceElements())
            ));

            // Ищем все доступные даты с ценами
            List<WebElement> availableDates = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.xpath(xpath.getPriceElements())
                    )
            );

            if (availableDates.isEmpty()) {
                throw new RuntimeException("Нет доступных дат для выбора");
            }

            Allure.step("Найдено доступных дат: " + availableDates.size());

            // Выбираем случайную доступную дату
            int dateIndex = ThreadLocalRandom.current().nextInt(availableDates.size());
            WebElement selectedDateCell = availableDates.get(dateIndex);

            Allure.step("Кликаем по выбранной дате");
            selectedDateCell.click();

            Allure.step("✅ Дата успешно выбрана");

        } catch (Exception e) {
            Allure.step("❌ Ошибка выбора даты: " + e.getMessage());
            throw e;
        }
    }

    @Step("Получение значения из поля календаря отправления")
    public String getCalendarFieldValue() {
        try {
            WebElement triggerValue = driver.findElement(
                    By.xpath(xpath.getCalendarDepartureValue())
            );
            String value = triggerValue.getText().trim();
            Allure.step("📅 Значение поля календаря: '" + value + "'");
            return value;
        } catch (Exception e) {
            Allure.step("⚠️ Не удалось получить значение поля календаря: " + e.getMessage());
            return "";
        }
    }
}