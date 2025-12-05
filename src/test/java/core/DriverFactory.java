package core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

/**
 * Фабрика для создания и управления WebDriver
 */
public class DriverFactory {

    private static WebDriver driver;

    /**
     * Получить экземпляр драйвера (Singleton)
     */
    public static WebDriver getDriver() {
        if (driver == null) {
            driver = createDriver(true); // По умолчанию headless
        }
        return driver;
    }

    /**
     * Создать новый драйвер с указанными параметрами
     */
    public static WebDriver createDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeDriver chromeDriver = new ChromeDriver(createChromeOptions(headless));
        System.out.println("🚀 Драйвер создан (headless: " + headless + ")");
        return chromeDriver;
    }

    /**
     * Настройка опций Chrome
     */
    private static ChromeOptions createChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        options.addArguments(
                "--ignore-certificate-errors",
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                "--window-size=1920,1080"
        );

        options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
      //  options.setCapability(CapabilityType.AC, true);

        return options;
    }

    /**
     * Полностью закрыть драйвер
     */
    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
                driver = null;
                System.out.println("🔒 Драйвер полностью закрыт");
            } catch (Exception e) {
                System.err.println("⚠️ Ошибка при закрытии драйвера: " + e.getMessage());
            }
        }
    }

    /**
     * Закрыть текущее окно браузера
     */
    public static void closeDriver() {
        if (driver != null) {
            try {
                driver.close();
                System.out.println("📱 Текущее окно браузера закрыто");
            } catch (Exception e) {
                System.err.println("⚠️ Ошибка при закрытии окна: " + e.getMessage());
            }
        }
    }

    /**
     * Получить текущий драйвер (может быть null)
     */
    public static WebDriver getCurrentDriver() {
        return driver;
    }

    /**
     * Проверить, инициализирован ли драйвер
     */
    public static boolean isDriverInitialized() {
        return driver != null;
    }
}