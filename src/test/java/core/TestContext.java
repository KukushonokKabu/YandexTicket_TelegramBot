package core;

import pages.models.TrainInfo;

/**
 * Контекст тестирования для хранения общих данных между тестами
 */
public class TestContext {
    private static TestContext instance;
    private TrainInfo lastCollectedTrainInfo;
    private String testSpecificData;
    private byte[] lastScreenshot;

    private TestContext() {
        // Приватный конструктор для Singleton
    }

    /**
     * Получить экземпляр контекста (Singleton)
     */
    public static synchronized TestContext getInstance() {
        if (instance == null) {
            instance = new TestContext();
        }
        return instance;
    }

    /**
     * Очистить контекст перед новым тестом
     */
    public void clear() {
        this.lastCollectedTrainInfo = null;
        this.testSpecificData = null;
        this.lastScreenshot = null;
        System.out.println("🧹 Контекст теста очищен");
    }

    // Геттеры и сеттеры
    public TrainInfo getLastCollectedTrainInfo() {
        return lastCollectedTrainInfo;
    }

    public void setLastCollectedTrainInfo(TrainInfo lastCollectedTrainInfo) {
        this.lastCollectedTrainInfo = lastCollectedTrainInfo;
    }

    public String getTestSpecificData() {
        return testSpecificData;
    }

    public void setTestSpecificData(String testSpecificData) {
        this.testSpecificData = testSpecificData;
    }

    public byte[] getLastScreenshot() {
        return lastScreenshot;
    }

    public void setLastScreenshot(byte[] lastScreenshot) {
        this.lastScreenshot = lastScreenshot;
    }

    // ========== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ СОВМЕСТИМОСТИ ==========

    /**
     * Статический метод для получения информации о поезде
     * (совместимость со старым кодом)
     */
    public static TrainInfo getTrainInfo() {
        return getInstance().getLastCollectedTrainInfo();
    }

    /**
     * Статический метод для установки информации о поезде
     * (совместимость со старым кодом)
     */
    public static void setTrainInfo(TrainInfo trainInfo) {
        getInstance().setLastCollectedTrainInfo(trainInfo);
    }

    /**
     * Статический метод для получения данных теста
     * (совместимость со старым кодом)
     */
    public static String getData() {
        return getInstance().getTestSpecificData();
    }

    /**
     * Статический метод для установки данных теста
     * (совместимость со старым кодом)
     */
    public static void setData(String data) {
        getInstance().setTestSpecificData(data);
    }

    /**
     * Статический метод для получения скриншота
     * (совместимость со старым кодом)
     */
    public static byte[] getScreenshot() {
        return getInstance().getLastScreenshot();
    }

    /**
     * Статический метод для установки скриншота
     * (совместимость со старым кодом)
     */
    public static void setScreenshot(byte[] screenshot) {
        getInstance().setLastScreenshot(screenshot);
    }
}