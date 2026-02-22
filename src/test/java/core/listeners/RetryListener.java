package core.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.Reporter;
import utils.annotations.Retry;

import java.util.Arrays;

/**
 * Listeners для повторного выполнения упавших тестов
 */
public class RetryListener implements IRetryAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(RetryListener.class);

    private int retryCount = 0;
    private int maxRetryCount = 0;
    private long delay = 1000;
    private Class<?  extends Throwable>[] retryFor;

    @Override
    public boolean retry(ITestResult result){
        if(retryCount == 0){
            initializeRetrySettings(result);
        }

        if(retryCount < maxRetryCount){
            Throwable throwable = result.getThrowable();

            if(isRetryble(throwable)){
                retryCount++;

                String message = String.format(
                        "Тест '%s' упал. Попытка №%d из %d. Ошибка: %s",
                        result.getName(),retryCount,maxRetryCount,throwable.getMessage()
                );
                log.warn(message);
                Reporter.log(message + "<br/>");

                try {
                    Thread.sleep(delay);
                }
                catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    log.error("Прерывание во вовремя задержки", e);
                }
                return true;
            }
        }

        if(retryCount > 0){
            String message = retryCount == maxRetryCount
                    ? String.format("Тест '%s' провалился после %d попыток", result.getName(),maxRetryCount)
                    : String.format("Тест '%s' провалился с неповторяемой ошибкой", result.getName());

            log.error(message);
            Reporter.log(message + "<br/>");
        }
        return false;
    }

    private void initializeRetrySettings(ITestResult result){
        Retry retry = result.getMethod().getConstructorOrMethod().getMethod()
                .getAnnotation(Retry.class);

        if(retry == null){
            retry = result.getTestClass().getRealClass()
                    .getAnnotation(Retry.class);
        }

        if(retry != null){
            this.maxRetryCount = retry.value();
            this.delay = retry.delay();
            this.retryFor = retry.retryFor();
        }
        else {
            this.maxRetryCount = 0;
        }
    }

    private boolean isRetryble(Throwable t){
        if(retryFor == null || retryFor.length == 0){
            return false;
        }

        // Если указан Exception.class - повторяем при любых исключениях
        if(retryFor.length == 1 && retryFor[0] == Exception.class){
            return true;
        }

        return Arrays.stream(retryFor)
                .anyMatch(exceptionClass -> exceptionClass.isAssignableFrom(t.getClass()));
    }
}
