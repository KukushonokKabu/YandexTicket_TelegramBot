package pages;

import core.BaseTest;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.models.TrainInfo;

/*
 * Page Object  класс для работы со страницей результатов поиска поездов.
 * Инкапсулирует все взаимодействия с элементами страницы результатов.
 */
public class ResultsPage extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;

    public ResultsPage(WebDriver driver,WebDriverWait wait){
        this.driver = driver;
        this.wait = wait;
    }

    // Ожидание загрузки страницы результатов

    @Step("Ожидание загрузки страницы результатов")
 public void waitForResultsToLoad(){
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.getPlatz())));
        }
        catch (Exception e){
            throw new RuntimeException("На странице нет поездов с доступными плацкартами: "+ e.getMessage());
        }
    }

    // Получение количества найденных поездов
    @Step("Проверяем количество найденных поездов")
    public int getTrainCount(){
         return driver.findElements(By.xpath(xpath.getPlatz())).size();
    }

    // Собираем информацию о случайном поезде по индексу

    @Step("Получение информации о поезде по индексу :{index}")
    public TrainInfo getTrainInfoByIndex(int index){

        return null;
    }


}
