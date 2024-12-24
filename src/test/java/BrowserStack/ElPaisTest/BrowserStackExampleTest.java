package BrowserStack.ElPaisTest;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.*;

public class BrowserStackExampleTest {
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
        bstackOptions.put("buildName", "1.0");
        bstackOptions.put("sessionName", "BrowserStackExampleTest");

        if (device != null && !device.isEmpty()) {
            bstackOptions.put("deviceName", device);
            if (realMobile != null && !realMobile.isEmpty()) {
                bstackOptions.put("realMobile", realMobile);
            }
        } else {
            if (os != null && !os.isEmpty()) {
                bstackOptions.put("os", os);
            }
            if (osVersion != null && !osVersion.isEmpty()) {
                bstackOptions.put("osVersion", osVersion);
            }
            
            if (browserName != null && !browserName.isEmpty()) {
                caps.setCapability("browserName", browserName);
            }
            if (browserVersion != null && !browserVersion.isEmpty()) {
                caps.setCapability("browserVersion", browserVersion);
            }
        }
        caps.setCapability("bstack:options", bstackOptions);

        driver = new RemoteWebDriver(new URL(BROWSERSTACK_HUB_URL), caps);
    }

    @Test
    public void testScrapeElPaisOpinionOnBrowserStack() {
        driver.get("https://elpais.com/opinion");

        List<WebElement> articleElements = driver.findElements(By.cssSelector("article"));
        int max = Math.min(articleElements.size(), 5);

        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            WebElement artElem = articleElements.get(i);
            Article article = new Article();

            try {
                WebElement titleElem = artElem.findElement(By.cssSelector("h2, h3"));
                article.titleEs = titleElem.getText().trim();

                WebElement contentElem = artElem.findElement(By.tagName("p"));
                article.contentEs = contentElem.getText().trim();

                try {
                    WebElement imgElem = artElem.findElement(By.tagName("img"));
                    article.imageUrl = imgElem.getAttribute("src");
                } catch (NoSuchElementException e) {
                    System.out.println("No image for article #" + (i + 1));
                }

            } catch (Exception e) {
                System.out.println("Could not parse article #" + (i + 1) + ": " + e.getMessage());
            }
            articles.add(article);
        }

        System.out.println("=== SPANISH ARTICLES (BrowserStack) ===");
        for (int i = 0; i < articles.size(); i++) {
            System.out.println("Article #" + (i + 1));
            System.out.println("Title (ES): " + articles.get(i).titleEs);
            System.out.println("Content (ES): " + articles.get(i).contentEs);
            System.out.println("Image URL: " + articles.get(i).imageUrl);
            System.out.println("------------------------");
        }

        for (Article art : articles) {
            if (art.titleEs != null && !art.titleEs.isEmpty()) {
                art.titleEn = TranslationService.translateEStoEN(art.titleEs);
            } else {
                art.titleEn = "";
            }
        }

        // Print translated titles
        System.out.println("=== TRANSLATED TITLES (EN) ===");
        for (int i = 0; i < articles.size(); i++) {
            System.out.println("Article #" + (i + 1) + " Title (EN): " + articles.get(i).titleEn);
        }

        // Identify repeated words
        Map<String, Integer> wordCount = new HashMap<>();
        for (Article art : articles) {
            String[] tokens = art.titleEn.split("\\s+");
            for (String token : tokens) {
                String clean = token.toLowerCase().replaceAll("[^a-z0-9]", "");
                if (!clean.isEmpty()) {
                    wordCount.put(clean, wordCount.getOrDefault(clean, 0) + 1);
                }
            }
        }

        System.out.println("=== REPEATED WORDS (>2) ===");
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > 2) {
                System.out.println("'" + entry.getKey() + "' appears " + entry.getValue() + " times.");
            }
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
