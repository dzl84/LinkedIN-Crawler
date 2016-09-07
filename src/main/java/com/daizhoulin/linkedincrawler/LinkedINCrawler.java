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
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daizhoulin.linkedincrawler.data.FalconHireDatastore;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

public class LinkedINCrawler {
	String username = null;
	String password = null;
	boolean isRecruiter = false;
	private RemoteWebDriver driver;
	private Selenium selenium;
	private FalconHireDatastore datastore;
	private Logger logger = LoggerFactory.getLogger(LinkedINCrawler.class);

	private static String BASEURL = "https://www.linkedin.com/";

	// Constructor
	public LinkedINCrawler() throws IOException {
		this.driver = new FirefoxDriver();
		// Implicit wait 60s for every action
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		this.selenium = new WebDriverBackedSelenium(driver, BASEURL);
		this.datastore = new FalconHireDatastore();
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
		this.selenium.type("id=session_key-login", this.username);
		this.selenium.type("id=session_password-login", this.password);
		this.selenium.click("id=btn-primary");

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
		selenium.open("/recruiter/smartsearch");
		if (!this.isLoggedIn())
			this.login();

		try {
			this.addKeywords(keywords);
			this.waitTillResultsUpdated();
			this.addLocations(locations);
			this.waitTillResultsUpdated();
			this.saveSearchResults();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Add a location to the current search criteria
	private void addLocations(List<String> locations) {
		if (locations == null || locations.size() == 0)
			return;
		try {
			WebElement section = this.driver.findElementById("facet-location");
			this.selenium
					.click("css=#facet-location > div.facet-wrapper > ul.pills. > li.add-pills > button.add-pills-btn");

			for (String l : locations) {
				logger.info(l);
				Thread.sleep(1000);
				this.selenium.click("id=location-input");
				this.selenium.keyPress("id=location-input", l);
				Thread.sleep(10000);

				// Press the down arrow button
				this.selenium.keyPress("id=location-input", "\\40");
				Thread.sleep(1000);
				// Press the enter button
				this.selenium.keyPress("id=location-input", "\\13");
				
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
			this.selenium
					.click("css=#facet-keywords > div.facet-wrapper > ul.pills. > li.add-pills > button.add-pills-btn");

			Thread.sleep(1000);
			selenium.type("id=keywords-input", keywords);
			Thread.sleep(1000);
			// Press the enter button
			selenium.keyPress("id=keywords-input", "\\13");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Wait at max 10s for the results list to be updated
	private void waitTillResultsUpdated() throws InterruptedException {
		int max_waits = 10;
		for (int i = 0; i < max_waits; i++) {
			int count = this.driver.findElements(By.cssSelector("span[class='loading-spinner']")).size();

			if (count == 0)
				break;

			Thread.sleep(1000);
		}
	}

	private void saveSearchResults() {
		List<WebElement> results = this.driver.findElements(By.cssSelector("li[id^='search-result-']"));
		logger.info(results.size() + "");
		for (WebElement r : results) {
			String[] ids = r.getAttribute("id").split("-");
			String id = ids[ids.length -1];
			System.out.println("id: " + id);
			String imgLink = r.findElement(By.cssSelector("div[class='top-card'] > img")).getAttribute("src");
			String name = r.findElement(By.cssSelector("a[class='search-result-profile-link']")).getText();
			String proLink = r.findElement(By.cssSelector("a[class='search-result-profile-link']"))
					.getAttribute("href");
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

	// Click the next page link if it exists
	private void clickNextPage() {

	}

	public static void main(String[] args) throws Exception {
		LinkedINCrawler crawler = new LinkedINCrawler();
		String keywords = "software engineer";

		List<String> locations = new ArrayList<String>();
		locations.add("Shanghai City");
		locations.add("Shanghai Suburb");

		crawler.advRecrSearch(keywords, locations);

	}

}
