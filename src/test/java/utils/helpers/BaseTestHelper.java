package utils.helpers;

import core.TestContext;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Вспомогательный класс с общими методами для тестов
 */
public class BaseTestHelper {

    protected WebDriver driver;
    protected WebDriverWait wait;

    /**
     * Конструктор с зависимостями
     */
    public BaseTestHelper(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // ========== ОСНОВНЫЕ ДЕЙСТВИЯ ==========

    @Step("Клик по элементу: {elementName}")
    public void clickElement(By locator, String elementName) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            Allure.step("✅ Успешно кликнули на: " + elementName);
        } catch (Exception e) {
            Allure.step("❌ Ошибка клика на " + elementName + ": " + e.getMessage());
            throw e;
        }
    }

    @Step("Ввод текста '{text}' в поле: {fieldName}")
    public void inputText(By locator, String text, String fieldName) {
        try {
            WebElement field = driver.findElement(locator);
            field.clear();
            field.sendKeys(text);
            Allure.step("✅ Успешно ввели '" + text + "' в поле: " + fieldName);
        } catch (Exception e) {
            Allure.step("❌ Ошибка ввода текста в " + fieldName + ": " + e.getMessage());
            throw e;
        }
    }

    @Step("Ввод текста '{text}' с эмуляцией человеческого поведения, в поле: {fieldName}")
    public void humanLikeInput(By locator, String text, String fieldName) throws InterruptedException {
        try {
            WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

            // Кликаем перед вводом (имитация пользователя)
            field.click();
            Thread.sleep(200);

            // Очистка по-человечески
            field.sendKeys(Keys.CONTROL + "a");
            field.sendKeys(Keys.BACK_SPACE);
            Thread.sleep(300);

            // Медленный ввод
            for (char c : text.toCharArray()) {
                field.sendKeys(String.valueOf(c));
                Thread.sleep(150 + ThreadLocalRandom.current().nextInt(100));
            }

            Allure.step("✅ Человеко-подобный ввод в: " + fieldName);
        } catch (Exception e) {
            Allure.step("❌ Ошибка человеко-подобного ввода: " + e.getMessage());
            throw e;
        }
    }

    // ========== ПРОВЕРКИ И ВАЛИДАЦИЯ ==========

    @Step("Проверка появления подсказок")
    public void validateSuggestionsAppear(String suggestionXpath) throws InterruptedException {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(suggestionXpath)));
            Thread.sleep(2000);

            List<WebElement> elements = driver.findElements(By.xpath(suggestionXpath));
            if (elements.size() < 3) {
                throw new AssertionError("Должно быть найдено минимум 3 элемента, найдено: " + elements.size());
            }

            Allure.step("✅ Подсказки успешно появились (" + elements.size() + " элементов)");
        } catch (Exception e) {
            Allure.step("❌ Ошибка при проверке подсказок: " + e.getMessage());
            throw e;
        }
    }

    @Step("Проверка функциональности поля очистки")
    public void validateClearButtonFunctionality(String buttonXpath) {
        try {
            WebElement clearButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath(buttonXpath))
            );

            if (!clearButton.isDisplayed()) {
                throw new AssertionError("Кнопка очистки должна быть видимой");
            }

            if (!clearButton.isEnabled()) {
                throw new AssertionError("Кнопка очистки должна быть активной");
            }

            Allure.step("✅ Кнопка очистки - OK (visible: true, enabled: true)");
        } catch (Exception e) {
            Allure.step("❌ Ошибка при проверке кнопки очистки: " + e.getMessage());
            throw e;
        }
    }

    @Step("Проверка текстового поля: {fieldName}")
    public void validateTextField(String fieldXpath, String fieldName) {
        try {
            WebElement field = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath(fieldXpath))
            );
            boolean isDisplayed = field.isDisplayed();
            boolean isEnabled = field.isEnabled();
            String currentValue = field.getAttribute("value");

            if (!isDisplayed) {
                throw new AssertionError(fieldName + " должен быть видимым");
            }

            if (!isEnabled) {
                throw new AssertionError(fieldName + " должен быть доступен для ввода");
            }

            Allure.step(String.format("%s - visible: %s, enabled: %s, value: '%s'",
                    fieldName, isDisplayed, isEnabled, currentValue));
        } catch (Exception e) {
            Allure.step("❌ Ошибка проверки поля " + fieldName + ": " + e.getMessage());
            throw e;
        }
    }

    // ========== УТИЛИТЫ ==========

    @Step("Ожидание загрузки страницы")
    public void waitForPageLoad() {
        try {
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
            Allure.step("✅ Страница полностью загружена");
        } catch (Exception e) {
            System.out.println("⚠️ Страница загружена, но readyState не complete");
        }
    }

    @Step("Очистка поля")
    public void clearField(String fieldXpath) {
        try {
            WebElement field = driver.findElement(By.xpath(fieldXpath));
            field.clear();
            field.sendKeys(Keys.CONTROL + "a");
            field.sendKeys(Keys.DELETE);
            Allure.step("✅ Поле очищено");
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка очистки поля: " + e.getMessage());
        }
    }

    @Step("Получение значения поля")
    public String getFieldValue(String fieldXpath) {
        try {
            WebElement field = driver.findElement(By.xpath(fieldXpath));
            String value = field.getAttribute("value");
            Allure.step("📝 Значение поля: '" + value + "'");
            return value;
        } catch (Exception e) {
            Allure.step("⚠️ Ошибка получения значения поля: " + e.getMessage());
            return null;
        }
    }

    @Step("Сделать скриншот: {screenshotName}")
    public byte[] takeScreenshot(String screenshotName) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            // Сохраняем скриншот в контекст
            TestContext.setScreenshot(screenshot);
            Allure.step("📸 Скриншот сохранен: " + screenshotName);
            return screenshot;
        } catch (Exception e) {
            System.err.println("❌ Не удалось сделать скриншот: " + e.getMessage());
            return new byte[0];
        }
    }

    @Step("Прокрутка к элементу")
    public void scrollToElement(WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            Thread.sleep(500);
            Allure.step("✅ Прокрутка к элементу выполнена");
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка прокрутки: " + e.getMessage());
        }
    }

    @Step("Ожидание изменения URL")
    public void waitForUrlChange(String initialUrl) {
        try {
            Allure.step("⏳ Ожидание изменения URL");
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

    @Step("Получение текущего URL")
    public String getCurrentUrlWithLog() {
        String url = driver.getCurrentUrl();
        Allure.step("🌐 Текущий URL: " + url);
        return url;
    }
}