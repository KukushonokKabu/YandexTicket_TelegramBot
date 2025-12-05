package api.clients;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class ApiClient {
    private WebDriver driver;
    private WebDriverWait wait;
    protected static BrowserMobProxy proxy;

    public ApiClient(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // ========== БАЗОВЫЕ МЕТОДЫ ==========

    @Step("Перехват API запросов для: {apiPattern}")
    public List<HarEntry> captureApiCalls(String apiPattern) {
        if (proxy == null || !proxy.isStarted()) {
            Allure.step("⚠️ Proxy не запущен, перехват невозможен");
            return new ArrayList<>();
        }

        Har har = proxy.getHar();
        List<HarEntry> apiCalls = new ArrayList<>();

        for (HarEntry entry : har.getLog().getEntries()) {
            if (entry.getRequest().getUrl().contains(apiPattern)) {
                apiCalls.add(entry);
                Allure.step("📡 Перехвачен API вызов: " +
                        entry.getRequest().getMethod() + " " +
                        entry.getRequest().getUrl());
            }
        }

        Allure.step("📡 Найдено API вызовов: " + apiCalls.size());
        return apiCalls;
    }

    // ========== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ==========

    @Step("Выполнение API запроса: {method} {url}")
    public Response executeApiRequest(String url, String method, String body) {
        Allure.step("Выполнение " + method + " запроса на " + url);

        return given()
                .contentType("application/json")
                .body(body)
                .when()
                .request(method, url);
    }

    @Step("Выполнение GET запроса: {url}")
    public Response executeGetRequest(String url) {
        return given()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    @Step("Выполнение POST запроса: {url}")
    public Response executePostRequest(String url, String body) {
        return given()
                .contentType("application/json")
                .body(body)
                .when()
                .post(url);
    }

    @Step("Проверка статуса ответа")
    public void validateResponseStatus(Response response, int expectedStatus) {
        int actualStatus = response.getStatusCode();
        if (actualStatus != expectedStatus) {
            throw new AssertionError("Ожидался статус " + expectedStatus +
                    ", получен " + actualStatus);
        }
        Allure.step("✅ Статус ответа корректный: " + actualStatus);
    }
}