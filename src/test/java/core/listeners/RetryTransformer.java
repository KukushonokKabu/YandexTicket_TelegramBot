package core.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import utils.annotations.Retry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Transformer для добавления RetryListener к тестам с аннотацией  @Retry
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod){
        if(testMethod != null){
            Retry retry = testMethod.getAnnotation(Retry.class);
            if(retry == null && testClass != null){
                retry  = (Retry) testClass.getAnnotation(Retry.class);
            }

            if(retry != null){
                annotation.setRetryAnalyzer(RetryListener.class);
            }
        }
    }
}
