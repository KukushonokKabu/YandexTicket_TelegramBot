package core.listeners;

import core.TestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestLifecycleListener  implements ITestListener {

    @Override
    public void onTestStart(ITestResult result){
        System.out.println("\n === ТЕСТ СТАРТ === " + result.getMethod().getMethodName());
        System.out.println("Контекст до теста: " + TestContext.getTrainInfo());
    }

//    @Override
//    public void onTestSucces(ITestResult result){
//        System.out.println("\n === ТЕСТ УСПЕШЕН === " + result.getMethod().getMethodName());
//        System.out.println("Контекст после теста: " + TestContext.getTrainInfo());
//        System.out.println("Данные: " + TestContext.getData());
//    }

    @Override
    public void onTestFailure(ITestResult result){
        System.out.println("\n === ТЕСТ УПАЛ === " + result.getMethod().getMethodName());
        System.out.println("Контекст после теста: "  + TestContext.getTrainInfo());
        System.out.println("Данные: " + TestContext.getData());
        System.out.println("Ошибка: " + result.getThrowable());
    }
}
