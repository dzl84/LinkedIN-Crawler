package com.daizhoulin.linkedincrawler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daizhoulin.linkedincrawler.data.FalconHireDatastore;


public class LinkedINCrawler {
	String username = null;
	String password = null;
	boolean isRecruiter = false;
	private ChromeDriver driver;
	private FalconHireDatastore datastore;
	private Logger logger = LoggerFactory.getLogger(LinkedINCrawler.class.getName());

	private static String BASEURL = "https://www.linkedin.com/";
    private static int PAGESIZE = 25;

	// Constructor
	public LinkedINCrawler() throws IOException {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-impl-side-painting");
        options.addArguments("incognito");
        options.addArguments("--disable-bundled-ppapi-flash");
        options.addArguments("--disable-extensions");
        options.addArguments("--proxy-server=http://proxy.vmware.com:3128");

		this.driver = new ChromeDriver(options);
		// Implicit wait 60s for every action
//		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
//		this.datastore = new FalconHireDatastore();
		this.loadConfig();
	}

	// Load the configure file
	private void loadConfig() throws IOException {
		Properties prop = new Properties();
		InputStream input = new FileInputStream("./config/linkedin.config");
		prop.load(input);
		this.username = prop.getProperty("username");
		this.password = prop.getProperty("password");
		this.isRecruiter = Boolean.getBoolean(prop.getProperty("isRecruiter"));
	}

	public boolean isLoggedIn() {
		logger.info("checking:  is logged in?");
		int ids = this.driver.findElements(By.id("session_key-login")).size();

		if (ids > 0)
			return false;
		return true;
	}

	public void login() {
		logger.info("Loging in...");
		WebElement username = this.driver.findElement(By.id("session_key-login"));
        String tmp = username.getText();
        if(tmp != "") username.clear();
        username.sendKeys(this.username);
        WebElement password = this.driver.findElement(By.id("session_password-login"));
        password.sendKeys(this.password);
        WebElement login_btn = this.driver.findElement(By.id("btn-primary"));
        login_btn.click();

		int ids = this.driver.findElements(By.id("cap-navlink")).size();
		if (ids > 0)
			logger.info("Loging in successfully.");
		// this.saveCookies();
	}

	// Save browser cookies to a local file
	private void saveCookies() {
		try {
			File file = new File("/tmp/remoteWD/cookies");
			// Delete old file if exists
			file.delete();
			file.createNewFile();
			FileWriter fileWrite = new FileWriter(file);
			BufferedWriter Bwrite = new BufferedWriter(fileWrite);
			// loop for getting the cookie information
			for (Cookie ck : this.driver.manage().getCookies()) {
				Bwrite.write((ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
						+ ck.getExpiry() + ";" + ck.isSecure()));
				Bwrite.newLine();
			}
			Bwrite.flush();
			Bwrite.close();
			fileWrite.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Load browser cookies from a local file
	private void loadCookies() {
		try {

			File file = new File("/tmp/remoteWD/cookies");
			if (!file.exists())
				return;
			FileReader fileReader = new FileReader(file);
			BufferedReader Buffreader = new BufferedReader(fileReader);
			String strline;
			while ((strline = Buffreader.readLine()) != null) {
				StringTokenizer token = new StringTokenizer(strline, ";");
				while (token.hasMoreTokens()) {
					String name = token.nextToken();
					String value = token.nextToken();
					String domain = token.nextToken();
					String path = token.nextToken();
					Date expiry = null;

					String val;
					if (!(val = token.nextToken()).equals("null")) {
						expiry = new Date(val);
					}
					Boolean isSecure = new Boolean(token.nextToken()).booleanValue();
					Cookie ck = new Cookie(name, value, domain, path, expiry, isSecure);
					this.driver.manage().addCookie(ck); // This will add
														// the stored
														// cookie to
														// your current
														// session
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Conduct a search as a recruiter
	public void advRecrSearch(String keywords, List<String> locations) {

		// Open recruiter view
		logger.info("Open recruiter search page");
		this.driver.get(BASEURL + "/recruiter/smartsearch");
        //this.loadCookies();
        this.driver.navigate().refresh();
		if (!this.isLoggedIn()) {
            this.login();
            this.waitLogin();
            this.chooseProfile();
            //this.saveCookies();
            this.addKeywords(keywords);
//            this.waitTillResultsUpdated();
            //this.addLocations(locations);
            this.waitTillResultsUpdated();
            //this.saveSearchResults();
            this.hasNextPage();
            this.openPage(2);
        }
	}

    private void waitLogin() {
        WebDriverWait wait = new WebDriverWait(this.driver, 60);
        wait.until(ExpectedConditions.textToBe(By.className("nav-link"), "SHARE FEEDBACK"));
        logger.info("logged in");
    }

    private void waitLocationTypeAhead2Present(String location){
        logger.info("Waiting location " + location + " to be present.");

        WebDriverWait wait = new WebDriverWait(this.driver, 60);
//        WebElement element = this.driver.findElement(By.xpath("//div[@id='tt-behavior92']"));
//
//        List<WebElement> s = element.findElements(By.tagName("p"));
//        for(WebElement a : s) {
//            logger.info(a.getText());
//        }
        wait.until(ExpectedConditions.textToBe(By.xpath("//div[@id='tt-behavior92']/div/p[1]"), location));
    }



    private void chooseProfile(){
        WebElement login_btn = this.driver.findElement(By.className("btn-primary"));
        login_btn.click();
    }

	// Add a location to the current search criteria
	private void addLocations(List<String> locations) {
		if (locations == null || locations.size() == 0)
			return;
		try {
			WebElement btn = this.driver.findElement(By.cssSelector("#facet-location > div.facet-wrapper > ul.pills > li.add-pills > button.add-pills-btn"));
			btn.click();
            WebElement input = this.driver.findElement(By.id("location-input"));

			for (String l : locations) {
				logger.info(l);
				Thread.sleep(1000);
				input.sendKeys(l);
				Thread.sleep(10000);
                this.waitLocationTypeAhead2Present(l);
				// Press the down arrow button
				input.sendKeys(Keys.ARROW_DOWN);
				Thread.sleep(500);
                input.sendKeys(Keys.ENTER);
				// Press the enter button


			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// check if the suggestions match the input
	private boolean suggestionMatched(WebElement section, String input) {
		List<WebElement> suggestions = section.findElements(By.xpath(".//ul[class='suggestions']/li/button"));
		logger.info(suggestions.size() + "");
		if (suggestions.size() == 0)
			suggestions = section.findElements(By.cssSelector("p[id^='tt-behavior*']"));
		if (suggestions.size() == 0)
			return false;
		String sugText = suggestions.get(0).getText();
		logger.info(sugText);
		if (sugText.toLowerCase().startsWith(input.toLowerCase()))
			return true;
		return false;
	}

	// Add a keyword to the search criteria
	private void addKeywords(String keywords) {
		if (keywords == null || keywords.length() == 0)
			return;
		try {
			WebElement btn = this.driver.findElement(By.cssSelector("#facet-keywords > div.facet-wrapper > ul.pills > li.add-pills > button.add-pills-btn"));
            logger.info(btn.toString());
		    btn.click();

			Thread.sleep(1000);
			WebElement input = this.driver.findElement(By.id("keywords-input"));
            input.sendKeys(keywords);
			Thread.sleep(1000);
			// Press the enter button
			input.sendKeys(Keys.ENTER);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Wait at max 10s for the results list to be updated
	private void waitTillResultsUpdated() {
        WebDriverWait wait = new WebDriverWait(this.driver, 60);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("span[class='loading-spinner']")));
        logger.info("results loaded");
//		int max_waits = 10;
//		for (int i = 0; i < max_waits; i++) {
//			int count = this.driver.findElements(By.cssSelector("span[class='loading-spinner']")).size();
//
//			if (count == 0)
//				break;
//
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
	}

	private void saveSearchResults() {
		List<WebElement> results = this.driver.findElements(By.cssSelector("li[id^='search-result-']"));
		logger.info(results.size() + "");
		for (WebElement r : results) {
			String[] ids = r.getAttribute("id").split("-");
			String id = ids[ids.length -1];
            if ("undefined".equals(id)) continue;
			System.out.println("id: " + id);
			String imgLink = r.findElement(By.cssSelector("div.top-card > img")).getAttribute("src");
			String name = r.findElement(By.cssSelector("a[class='search-result-profile-link']")).getText();
			String proLink = r.findElement(By.cssSelector("a[class='search-result-profile-link']"))
					.getAttribute("href");
            System.out.println("ProLink: " + proLink);
			String headline = r.findElement(By.cssSelector("p[class='headline']")).getText();
			String curTitle = "";
			int idx = headline.indexOf("at");
			
			if (idx > 0)
				curTitle = headline.substring(0, idx);
			else
				idx = headline.indexOf("At");
			String curCompany = headline.substring(idx + 2,headline.length());
			System.out.println("Title: " + curTitle);
			System.out.println("Company: " + curCompany);
			String loc_ind = r.findElement(By.cssSelector("p[class='location']")).getText();
			idx = loc_ind.indexOf("•");
			
			String curLocation = loc_ind.substring(0, idx);
			String curIndustry = loc_ind.substring(idx + 2, loc_ind.length());
			System.out.println(curLocation);
			System.out.println(curIndustry);
			logger.info("Saving candidate " + name);
			//datastore.saveCandidate(id, name, imgLink, proLink, curTitle, curCompany, curLocation, curIndustry);
			
		}
	}

    private void openPage(int page_num) {
        String cur_loc = this.driver.getCurrentUrl();
        int start = cur_loc.indexOf("start=");
        int next_and = cur_loc.indexOf("&", start);
        int cur_start_idx = 0;
        if (next_and < 0)
            cur_start_idx = Integer.valueOf(cur_loc.substring(start + 6));
        else
            cur_start_idx = Integer.valueOf(cur_loc.substring(start + 6, next_and));
        int next_start_idx = PAGESIZE * (page_num - 1);
        if (next_start_idx == cur_start_idx) {
            logger.info("Already on page " + page_num);
            return;
        }
        String next_loc = cur_loc.replace("start=" + cur_start_idx, "start=" + next_start_idx);
        this.driver.get(next_loc);
    }

	// Click the next page link if it exists
	private void clickNextPage() {
        List<WebElement> page_links = this.driver.findElements(By.cssSelector("#pagination > div > ul > li > a"));
        int length = page_links.size();
        WebElement last_link = page_links.get(length - 1);
        last_link.click();
	}

    private boolean hasNextPage() {
        List<WebElement> page_links = this.driver.findElements(By.cssSelector("#pagination > div > ul > li > a"));
        int length = page_links.size();
        WebElement last_link = page_links.get(length - 1);
        logger.info(last_link.getText());
        if (last_link.getText().equals("Next Page")) {
            logger.info("Has next page");
            return true;
        }
        return false;
    }

	public static void main(String[] args) throws Exception {
		LinkedINCrawler crawler = new LinkedINCrawler();
		String keywords = "software engineer";

		List<String> locations = new ArrayList<String>();
		locations.add("Shanghai City, China");
		locations.add("Shanghai Suburb, China");

		crawler.advRecrSearch(keywords, locations);

	}

}
