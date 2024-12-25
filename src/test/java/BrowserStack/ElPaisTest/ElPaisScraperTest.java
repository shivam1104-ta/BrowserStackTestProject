package BrowserStack.ElPaisTest;

import org.testng.annotations.*;
import org.testng.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.*;
import java.util.NoSuchElementException;
import java.io.*;
import java.net.URL;


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
    }

    @Test
    public void testScrapeElPaisOpinion() {
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
                List<WebElement> imgElements = artElem.findElements(By.tagName("img"));
                if (!imgElements.isEmpty()) {
                    article.imageUrl = imgElements.get(0).getAttribute("src");
                } else {
                    System.out.println("No image for article #" + (i + 1));
                    article.imageUrl = null; 
                }

            } catch (NoSuchElementException e) {
                System.out.println("Required element not found in article #" + (i + 1) + ": " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Could not parse article #" + (i + 1) + ": " + e.getMessage());
            }
            articles.add(article);
        }

        System.out.println("=== SPANISH ARTICLES ===");
        for (int i = 0; i < articles.size(); i++) {
            Article art = articles.get(i);
            System.out.println("Article #" + (i + 1));
            System.out.println("Title (ES): " + art.titleEs);
            System.out.println("Content (ES): " + art.contentEs);
            System.out.println("Image URL: " + art.imageUrl);
            System.out.println("------------------------");
        }

        for (int i = 0; i < articles.size(); i++) {
            String imageUrl = articles.get(i).imageUrl;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                downloadImage(imageUrl, "article_" + (i + 1) + ".jpg"); 
            }
        }
        for (Article art : articles) {
            if (art.titleEs != null && !art.titleEs.isEmpty()) {
                // Spanish -> English
                art.titleEn = TranslationService.translateEStoEN(art.titleEs);
            } else {
                art.titleEn = "";
            }
        }

        System.out.println("=== TRANSLATED TITLES (EN) ===");
        for (int i = 0; i < articles.size(); i++) {
            System.out.println("Article #" + (i + 1) + " Title (EN): " + articles.get(i).titleEn);
        }

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
