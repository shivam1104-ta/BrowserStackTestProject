package BrowserStack.ElPaisTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class BrowserStackExampleTest {
	
	// Credentials
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
                // Scrape Spanish Title
                WebElement titleElem = artElem.findElement(By.cssSelector("h2, h3"));
                article.titleEs = titleElem.getText().trim();

                // Scrape Spanish Content Snippet
                WebElement contentElem = artElem.findElement(By.tagName("p"));
                article.contentEs = contentElem.getText().trim();

                // **Safely retrieve image without throwing an exception**
                List<WebElement> imgElements = artElem.findElements(By.tagName("img"));
                if (!imgElements.isEmpty()) {
                    // Optionally, use a more specific selector if available
                    article.imageUrl = imgElements.get(0).getAttribute("src");
                } else {
                    System.out.println("No image for article #" + (i + 1));
                    article.imageUrl = null; // Explicitly set to null for clarity
                }

            } catch (NoSuchElementException e) {
                System.out.println("Required element not found in article #" + (i + 1) + ": " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Could not parse article #" + (i + 1) + ": " + e.getMessage());
            }
            articles.add(article);
        }

        // 3. Print the scraped Spanish data
        System.out.println("=== SPANISH ARTICLES (BrowserStack) ===");
        for (int i = 0; i < articles.size(); i++) {
            Article art = articles.get(i);
            System.out.println("Article #" + (i + 1));
            System.out.println("Title (ES): " + art.titleEs);
            System.out.println("Content (ES): " + art.contentEs);
            System.out.println("Image URL: " + art.imageUrl);
            System.out.println("------------------------");
        }

        // (Optional) Download images locally
        for (int i = 0; i < articles.size(); i++) {
            String imageUrl = articles.get(i).imageUrl;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                downloadImage(imageUrl, "article_" + (i + 1) + ".jpg"); // Changed to (i + 1) for consistency
            }
        }

        // 4. Translate Titles using our RapidAPI-based TranslationService
        //    Make sure you've configured your 'TranslationService' class to use RapidAPI
        for (Article art : articles) {
            if (art.titleEs != null && !art.titleEs.isEmpty()) {
                // Spanish -> English
                art.titleEn = TranslationService.translateEStoEN(art.titleEs);
            } else {
                art.titleEn = "";
            }
        }

        // 5. Print translated titles
        System.out.println("=== TRANSLATED TITLES (EN) ===");
        for (int i = 0; i < articles.size(); i++) {
            System.out.println("Article #" + (i + 1) + " Title (EN): " + articles.get(i).titleEn);
        }

        // 6. Identify repeated words
        Map<String, Integer> wordCount = new HashMap<>();
        for (Article art : articles) {
            if (art.titleEn != null && !art.titleEn.isEmpty()) {
                String[] tokens = art.titleEn.split("\\s+");
                for (String token : tokens) {
                    String clean = token.toLowerCase().replaceAll("[^a-z0-9]", "");
                    if (!clean.isEmpty()) {
                        wordCount.put(clean, wordCount.getOrDefault(clean, 0) + 1);
                    }
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

    /**
     * Download an image from a given URL and save it locally.
     */
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
