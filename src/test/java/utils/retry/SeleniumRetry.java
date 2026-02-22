package utils.retry;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class SeleniumRetry {
    private static final Logger log = LoggerFactory.getLogger(SeleniumRetry.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final int maxAttempts;
    private final long delayMillis;

    private static final List<Class<? extends  Exception>> RETRYABLE_AXCEPTIONS = List.of(
            NoSuchElementException.class,
            StaleElementReferenceException.class,
            TimeoutException.class,
            ElementNotInteractableException.class,
            ElementClickInterceptedException.class
    );

    public SeleniumRetry(WebDriver driver){
        this(driver, 3,500);
    }

    public SeleniumRetry(WebDriver driver,int maxAttempts, long delayMillis){
        this.driver = driver;
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public <T> T retry(Supplier<T> function,String operationDescription) {
        int attempt = 1;
        Exception lastException = null;

        while (attempt <= maxAttempts){
            try {
                log.debug("Попытка №{}: {}", attempt,operationDescription);
                T result = function.get();

                if(attempt > 1){
                    log.info("Успешно после {} попыток: {}", attempt,operationDescription);
                }

                return result;
            }
            catch (Exception e){
                lastException = e;

                if(isRetryble(e)){
                    log.warn("Попытка №{} не удалась ({}): {}",
                            attempt,e.getClass().getSimpleName(),e.getMessage());

                    if(attempt < maxAttempts){
                        log.debug("Повтор через {} мс ..." , delayMillis);
                        sleep();
                    }
                } else {
                    log.error("Неповторяемая ошибка : {}", e.getMessage());
                    throw e;
                }
            }

            attempt ++;
        }

        throw new AssertionError(
                String.format("Операция '%s' не удалась после %d попыток. Последняя ошибка: %s",
                        operationDescription,maxAttempts,lastException.getMessage()),lastException
        );

    }

    private boolean isRetryble(Exception e){
        return RETRYABLE_AXCEPTIONS.stream()
                .anyMatch(ex -> ex.isAssignableFrom(e.getClass()));
    }

    private void sleep(){
        try {
            Thread.sleep(delayMillis);
        }
        catch (InterruptedException ie){
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during retry delay",ie);
        }
    }

    // Специализированные методы для Selenium

    public WebElement findElement(By locator){
        return retry(
                () -> {
                    try {
                        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                    } catch (Exception e){
                        return driver.findElement(locator);
                    }
                },
                "поиск элемента: " + locator
        );
    }

    public List<WebElement>findElements(By locator){
        return retry(
                () -> {
                    List<WebElement>elements = driver.findElements(locator);
                    if(elements.isEmpty()) {
                        throw new NoSuchElementException("Элементы не найдены: " + locator);
                    }
                    return elements;
                },
                "поиск элементов: " + locator
        );
    }

    public WebElement findElement(By locator, int index){
        return retry(
                () -> {
                    List<WebElement> elements = driver.findElements(locator);
                    if(elements.size() <= index){
                        throw new org.openqa.selenium.NoSuchElementException(
                                String.format("Элемент с индексом %d не найден.Всего: %d",index,elements.size())
                        );
                    }
                    return elements.get(index);
                },
                String.format("поиск элемента {%d} по локатору: %s" , index,locator)
        );
    }

    public void click(WebElement element){
        retry(
                ()->{
                    wait.until(ExpectedConditions.elementToBeClickable(element));
                    element.click();
                    return null;
                },
                "клик на элемент"
        );
    }

    public void click(By locator){
        WebElement element = findElement(locator);
        click(element);
    }

    public String getText(By locator){
        return retry(
                () -> {
                    WebElement element = findElement(locator);
                    return element.getText();
                },
                "получение текста: " + locator
        );
    }
}
