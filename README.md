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
- **`testngBrowserStack.xml`** – Manages parallel TestNG runs (desktop + mobile browsers).
- **`testngLocalSystem.xml`** – Manages local System TestNg runs.

---

## Installation

1. **Clone** or **download** this repository.  
2. Open a terminal in the project root.
3. Run:
   ```bash
   mvn clean install
This downloads the required dependencies (Selenium, TestNG, HttpClient, etc.) and compiles the project.






