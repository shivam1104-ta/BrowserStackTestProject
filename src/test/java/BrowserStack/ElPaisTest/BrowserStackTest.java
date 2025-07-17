package BrowserStack.ElPaisTest;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;
import org.testng.annotations.Optional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import org.testng.ITestResult;


public class BrowserStackTest {

    private static final String USERNAME = "shivamtayal_W4Gwj7";
    private static final String ACCESS_KEY = "8vjpq3VXWJV6m4yVCsZ3";
    private static final String BROWSERSTACK_HUB_URL =
            "https://" + USERNAME + ":" + ACCESS_KEY + "@hub.browserstack.com/wd/hub";

    private WebDriver driver;

    static class Article {
        String titleEs;
        String contentEs;
        String titleEn;
        String imageUrl;
    }

    @Parameters({"os", "osVersion", "browserName", "browserVersion", "device", "realMobile"})
    @BeforeMethod
    public void setUp(
            @Optional("") String os,
            @Optional("") String osVersion,
            @Optional("") String browserName,
            @Optional("") String browserVersion,
            @Optional("") String device,
            @Optional("") String realMobile
    ) throws Exception {

        DesiredCapabilities caps = new DesiredCapabilities();
        Map<String, Object> bstackOptions = new HashMap<>();

        bstackOptions.put("projectName", "ElPaisScraperProject");
        bstackOptions.put("buildName", "Build 1.0");
        bstackOptions.put("sessionName", "Hamburger Menu Navigation");

        if (!device.isEmpty()) {
            bstackOptions.put("deviceName", device);
            bstackOptions.put("realMobile", realMobile);
        } else {
            if (!os.isEmpty()) bstackOptions.put("os", os);
            if (!osVersion.isEmpty()) bstackOptions.put("osVersion", osVersion);
            if (!browserName.isEmpty()) caps.setCapability("browserName", browserName);
            if (!browserVersion.isEmpty()) caps.setCapability("browserVersion", browserVersion);
        }

        caps.setCapability("bstack:options", bstackOptions);
        driver = new RemoteWebDriver(new URL(BROWSERSTACK_HUB_URL), caps);
    }

    @Test
    public void testScrapeViaHamburgerToOpinion() throws InterruptedException {
        driver.get("https://elpais.com");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Accept cookie popup
        try {
            WebElement aceptarBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Aceptar']")));
            aceptarBtn.click();
            System.out.println("Cookie popup accepted.");
        } catch (Exception e) {
            System.out.println("Cookie popup not found.");
        }

        // Hide banner if present
        try {
            WebElement didomiBanner = driver.findElement(By.id("didomi-notice"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.display='none';", didomiBanner);
            System.out.println("Banner hidden.");
        } catch (Exception e) {
            System.out.println("No Didomi banner.");
        }

        // Click hamburger menu
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn_open_hamburger")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuBtn);
            Thread.sleep(1500);
            System.out.println("Hamburger menu clicked.");
        } catch (Exception e) {
            System.out.println("Failed to click hamburger menu: " + e.getMessage());
        }

        // Click "Opinión" tab
        try {
            WebElement opinionTab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='OPINIÓN' or normalize-space()='Opinión']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opinionTab);
            Thread.sleep(2000);
            System.out.println("Navigated to Opinión section.");
        } catch (Exception e) {
            System.out.println("Failed to click Opinión tab: " + e.getMessage());
        }

        // Scrape articles
        List<WebElement> articleElements = driver.findElements(By.xpath("//article[p[@class='c_d']]"));
        int max = Math.min(articleElements.size(), 5);
        List<Article> articles = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            Article article = new Article();
            WebElement elem = articleElements.get(i);

            try {
                article.titleEs = elem.findElement(By.cssSelector("h2, h3, header.c_h")).getText().trim();
            } catch (NoSuchElementException e) {
                article.titleEs = "";
            }

            try {
                article.contentEs = elem.findElement(By.cssSelector("p.c_d")).getText().trim();
            } catch (NoSuchElementException e) {
                article.contentEs = "";
            }

            List<WebElement> imgTags = elem.findElements(By.tagName("img"));
            article.imageUrl = imgTags.isEmpty() ? null : imgTags.get(0).getAttribute("src");

            articles.add(article);
        }

        // Print results
        System.out.println("=== SPANISH ARTICLES (via menu) ===");
        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            System.out.println("Article #" + (i + 1));
            System.out.println("Title (ES): " + a.titleEs);
            System.out.println("Content (ES): " + a.contentEs);
            System.out.println("Image URL: " + a.imageUrl);
            System.out.println("------------------------");
        }
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (driver != null) {
            String status = "failed";
            String reason = "Test failed";

            if (result.getStatus() == ITestResult.SUCCESS) {
                status = "passed";
                reason = "Test passed";
            } else if (result.getStatus() == ITestResult.SKIP) {
                status = "skipped";
                reason = "Test skipped";
            } else if (result.getStatus() == ITestResult.FAILURE) {
                reason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Test failed";
            }

            // Report to BrowserStack
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\":\""
                                + status + "\", \"reason\": \"" + reason + "\"}}"
                );
            } catch (Exception e) {
                System.out.println("Failed to update status on BrowserStack: " + e.getMessage());
            }

            driver.quit();
        }
    }


    private void downloadImage(String urlStr, String fileName) {
        try (InputStream in = new URL(urlStr).openStream();
             FileOutputStream fos = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            System.out.println("Image saved: " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to download image: " + e.getMessage());
        }
    }
}