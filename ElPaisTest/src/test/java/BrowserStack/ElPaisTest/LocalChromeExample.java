// Example: local ChromeDriver
package BrowserStack.ElPaisTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class LocalChromeExample {
    @Test
    public void testLocalChrome() {
        WebDriver driver = new ChromeDriver();
        driver.get("https://elpais.com/opinion");
        driver.quit();
    }
}
