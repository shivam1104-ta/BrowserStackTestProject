package BrowserStack.ElPaisTest;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;

public class ElPaisScraperTest {

    private WebDriver driver;

    static class Article {
        String titleEs;
        String contentEs;
        String titleEn;
        String imageUrl;
    }

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testScrapeElPaisOpinion() {
        driver.get("https://elpais.com");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));


        //  Click hamburger menu
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btn_open_hamburger")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", menuBtn);
            wait.until(ExpectedConditions.elementToBeClickable(menuBtn));

            try {
                menuBtn.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuBtn);
            }

            Thread.sleep(1000);  // Ensure the menu expands
            System.out.println("Hamburger menu clicked.");
        } catch (Exception e) {
            System.out.println("Hamburger menu not found or not clickable: " + e.getMessage());
        }

        //  Navigate to Opinión tab
        try {
            Thread.sleep(2000);  // Wait for the menu to render
            WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='OPINIÓN'] | //a[normalize-space()='Opinión']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", opinionLink);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opinionLink);
            System.out.println("Clicked on Opinión tab.");
        } catch (Exception e) {
            System.out.println("Opinión tab not found: " + e.getMessage());
        }

        // Scrape the first 5 articles
        List<WebElement> articleElements = driver.findElements(By.xpath("//article[p[@class='c_d']]"));
        int max = Math.min(articleElements.size(), 5);
        List<Article> articles = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            WebElement artElem = articleElements.get(i);
            Article article = new Article();

            try {
                // Extract title
                try {
                    WebElement titleElem = artElem.findElement(By.cssSelector("h2, h3, header.c_h"));
                    article.titleEs = titleElem.getText().trim();
                } catch (NoSuchElementException e) {
                    article.titleEs = "";
                }

                // Extract content
                try {
                    WebElement contentElem = artElem.findElement(By.cssSelector("p.c_d"));
                    article.contentEs = contentElem.getText().trim();
                } catch (NoSuchElementException e) {
                    article.contentEs = "";
                }

                // Extract image
                List<WebElement> imgElements = artElem.findElements(By.tagName("img"));
                article.imageUrl = imgElements.isEmpty() ? null : imgElements.get(0).getAttribute("src");

            } catch (Exception e) {
                System.out.println("Error parsing article #" + (i + 1) + ": " + e.getMessage());
            }

            articles.add(article);
        }

        //  Display article details
        System.out.println("=== SPANISH ARTICLES ===");
        for (int i = 0; i < articles.size(); i++) {
            Article art = articles.get(i);
            System.out.println("Article #" + (i + 1));
            System.out.println("Title (ES): " + art.titleEs);
            System.out.println("Content (ES): " + art.contentEs);
            System.out.println("Image URL: " + art.imageUrl);
            System.out.println("------------------------");
        }

        //  Download images
        for (int i = 0; i < articles.size(); i++) {
            String imageUrl = articles.get(i).imageUrl;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                downloadImage(imageUrl, "article_" + (i + 1) + ".jpg");
            }
        }

        //  Translate article titles
        for (Article art : articles) {
            if (art.titleEs != null && !art.titleEs.isEmpty()) {
                art.titleEn = TranslationService.translateEStoEN(art.titleEs);
            } else {
                art.titleEn = "";
            }
        }

        //  Display translated titles
        System.out.println("=== TRANSLATED TITLES (EN) ===");
        for (int i = 0; i < articles.size(); i++) {
            System.out.println("Article #" + (i + 1) + " Title (EN): " + articles.get(i).titleEn);
        }

        //  Word frequency analysis
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

        // Assertion
        Assert.assertFalse(articles.isEmpty(), "No articles found!");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
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