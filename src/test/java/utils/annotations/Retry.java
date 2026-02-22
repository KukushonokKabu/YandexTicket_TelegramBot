package utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для повторного выполнения теста при определенных исключениях
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Retry {
    /**
     * Количество попыток
     */
    int value() default 3;

    /**
     * Задержка между попытками в миллисекундах
     */
    long delay() default 1000;

    /**
     * Массив исключений, при которых нужно повторить тест
     */
    Class<? extends Throwable>[] retryFor() default {Exception.class};
}
