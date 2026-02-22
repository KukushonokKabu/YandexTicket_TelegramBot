package core;

import pages.models.TrainInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контекст тестирования для хранения общих данных между тестами
 */
public class TestContext {
    private static final Logger log = LoggerFactory.getLogger(TestContext.class);

    private static TestContext instance;
    private TrainInfo lastCollectedTrainInfo;
    private String testSpecificData;
    private byte[] lastScreenshot;

    private TestContext() {}

    public static synchronized TestContext getInstance() {
        if (instance == null) {
            instance = new TestContext();
        }
        return instance;
    }

    public void clear() {
        this.lastCollectedTrainInfo = null;
        this.testSpecificData = null;
        this.lastScreenshot = null;
        log.debug("Контекст теста очищен");
    }

    // Геттеры и сеттеры
    public TrainInfo getLastCollectedTrainInfo() { return lastCollectedTrainInfo; }
    public void setLastCollectedTrainInfo(TrainInfo info) { this.lastCollectedTrainInfo = info; }
    public String getTestSpecificData() { return testSpecificData; }
    public void setTestSpecificData(String data) { this.testSpecificData = data; }
    public byte[] getLastScreenshot() { return lastScreenshot; }
    public void setLastScreenshot(byte[] screenshot) { this.lastScreenshot = screenshot; }

    // Статические методы
    public static TrainInfo getTrainInfo() { return getInstance().getLastCollectedTrainInfo(); }
    public static void setTrainInfo(TrainInfo info) { getInstance().setLastCollectedTrainInfo(info); }
    public static String getData() { return getInstance().getTestSpecificData(); }
    public static void setData(String data) { getInstance().setTestSpecificData(data); }
    public static byte[] getScreenshot() { return getInstance().getLastScreenshot(); }
    public static void setScreenshot(byte[] screenshot) { getInstance().setLastScreenshot(screenshot); }
}