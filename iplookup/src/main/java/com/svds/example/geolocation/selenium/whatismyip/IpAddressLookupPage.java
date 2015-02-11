package com.svds.example.geolocation.selenium.whatismyip;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author rmagnus
 */
public class IpAddressLookupPage {

	private WebDriver driver;
	
	/**
	 * Default empty constructor
	 */
	public IpAddressLookupPage() {}
	
	public IpAddressLookupPage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	public IpAddressLookupPage open() {
		driver.get("http://www.whatismyip.com/ip-address-lookup/");
		return this;
	}
	
	@FindBy(name="IP")
	private WebElement searchBar;
	
	@FindBy(name="GL")
	private WebElement button;
	
	private Ip2LocationResults ip2LocationResults;
	public Ip2LocationResults getIp2LocationResults() { return ip2LocationResults; }
	
	private IpAddressLabsResults ipAddressLabsResults;
	public IpAddressLabsResults getIpAddressLabsResults() { return ipAddressLabsResults; }
	
	public IpAddressLookupPage submitQuery(InetAddress address) {
		if (null == address) { return this; }
		return submitQuery(address.toString());
	}
	
	public IpAddressLookupPage submitQuery(String address) {
		if (StringUtils.isBlank(address)) { return this; }
		
		final String query = address.replaceAll("^\\/+", "").replaceAll("\\/+$", "").trim();
		
		searchBar.clear();
		searchBar.sendKeys(query);
		
		//WebDriverWait wait = new WebDriverWait(driver, 3000);
		button.click();
		
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.MINUTES);
		
		IpAddressLookupPage page = new IpAddressLookupPage(driver);
		page.ip2LocationResults = new Ip2LocationResults(driver).load();
		page.ipAddressLabsResults = new IpAddressLabsResults(driver).load();
		return page;
	}
	
}
