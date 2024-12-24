# El Pais Scraper & Translation Tests

This repository contains a **Selenium** + **TestNG** project that:

- **Scrapes** the [El País Opinion](https://elpais.com/opinion) section (first 5 articles).
- **Translates** Spanish titles to English using the **Rapid Translate Multi Traduction** API (via RapidAPI).
- **Checks** for repeated words in the translated titles.
- **Runs** in parallel on **BrowserStack** (desktop + mobile devices) or locally via a local WebDriver setup.

---

## Features

1. **Scrape Articles** – Collect each article’s title, snippet, and image (if any).  
2. **Translation** – Convert Spanish titles to English using `TranslationService`.  
3. **Word Frequency** – Identify words that appear more than twice across all translated titles.  
4. **BrowserStack Parallel** – Launch tests simultaneously on multiple environments (Windows, macOS, iOS, Android).

---

## Prerequisites

1. **Java 8+**  
2. **Maven** (latest recommended)  
3. **RapidAPI Key** for the [Rapid Translate Multi Traduction API](https://rapidapi.com/).  
4. **BrowserStack** account (only if you plan to run cross‐browser tests).

---

## Project Structure

ElPaisScraperProject ├── pom.xml ├── README.md ├── src │ └── test │ └── java │ └── BrowserStack │ └── ElPaisTest │ ├── ElPaisScraperTest.java (Local test) │ ├── TranslationService.java (RapidAPI-based translator) │ └── BrowserStackExampleTest.java (BrowserStack parallel test) └── testng.xml

markdown
Copy code

- **`ElPaisScraperTest.java`** – A basic TestNG class for local testing.  
- **`BrowserStackExampleTest.java`** – Demonstrates parallel cross‐browser testing on BrowserStack.  
- **`TranslationService.java`** – Calls the Rapid Translate API.  
- **`testng.xml`** – Manages parallel TestNG runs (desktop + mobile browsers).

---

## Installation

1. **Clone** or **download** this repository.  
2. Open a terminal in the project root.
3. Run:
   ```bash
   mvn clean install
This downloads the required dependencies (Selenium, TestNG, HttpClient, etc.) and compiles the project.

Configuration
Rapid Translate API Key
In TranslationService.java, find:
java
Copy code
private static final String RAPID_API_KEY = "YOUR_RAPIDAPI_KEY";
Replace "YOUR_RAPIDAPI_KEY" with your actual key from RapidAPI.
BrowserStack Credentials
In BrowserStackExampleTest.java, look for:
java
Copy code
private static final String USERNAME = "YOUR_BROWSERSTACK_USERNAME";
private static final String ACCESS_KEY = "YOUR_BROWSERSTACK_ACCESS_KEY";
Replace these with your real BrowserStack username & access key.
Running Locally
If you only want a local run (e.g., using ChromeDriver):

bash
Copy code
mvn clean test
This typically executes tests in ElPaisScraperTest.java.
Scrapes up to 5 articles, prints their Spanish titles/content, downloads images, calls TranslationService for each Spanish title, and prints repeated words in English.
Running in Parallel on BrowserStack
Edit testng.xml to specify multiple <test> blocks (Windows, macOS, iPhone, Android, etc.). An example is already included:
xml
Copy code
<suite name="ElPaisBrowserStackSuite" parallel="tests" thread-count="5">
    <test name="Chrome_Win10">
      <parameter name="os" value="Windows"/>
      <parameter name="osVersion" value="10"/>
      <parameter name="browserName" value="Chrome"/>
      <parameter name="browserVersion" value="latest"/>
      <classes>
        <class name="BrowserStack.ElPaisTest.BrowserStackExampleTest"/>
      </classes>
    </test>
    <!-- More <test> blocks for Firefox, Safari, iPhone, iQOO Neo 7, etc. -->
</suite>
Ensure you have configured your BrowserStackExampleTest with the correct credentials and device/OS parameters.
Run:
bash
Copy code
mvn clean test -DsuiteXmlFile=testng.xml
Check the BrowserStack Automate Dashboard for your sessions. You should see 5 concurrent tests (or however many you defined) scraping El País in real time.
Common Issues
RapidAPI 403 / 429 – “Not subscribed” or “Too many requests.”

Verify your subscription plan.
Ensure you’re not exceeding free tier limits.
Invalid BrowserStack Device – If “iQOO Neo 7” is not in BrowserStack’s official list, you’ll get an error.

Use a device from the BrowserStack device list.
Legacy Capabilities Warning – If Selenium complains about “Sending the following invalid capabilities: [build, name, os],” you must place them inside bstack:options rather than top-level. (Our code already follows this pattern.)

License
(Optional: Add your license details here, e.g., MIT, Apache-2.0, or remove this section if not applicable.)

Enjoy Cross-Browser Testing!
You now have a complete setup for scraping El País articles, translating them via a public API, and verifying them in parallel on BrowserStack across various devices and browsers. Feel free to modify or extend the tests as needed!






