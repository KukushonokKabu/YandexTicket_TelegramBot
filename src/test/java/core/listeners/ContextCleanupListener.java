package core.listeners;

import core.TestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ContextCleanupListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result){
        // Очищаем перед каждым тестом
        TestContext.getInstance().clear();
        System.out.println("Контекст очищен перед тестом: "  + result.getMethod().getMethodName());
    }
}
