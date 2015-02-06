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
	
	
	public static class Ip2LocationResults {
		
		private WebDriver driver;
		public Ip2LocationResults() {}
		
		public Ip2LocationResults(WebDriver driver) {
			this.driver = driver;
			PageFactory.initElements(driver, this);
		}
		
		@FindBy(className="ip-lookup-info")
		private WebElement divGroup;
		public Ip2LocationResults load() {
			if (null == divGroup) {
				divGroup = driver.findElement(By.className("ip-lookup-info"));
			}
			ipAddress = findSubElementByClassName(divGroup, "the-lookup-ip");
			city = findSubElementByClassName(divGroup, "the-city");
			region = findSubElementByClassName(divGroup, "the-region");
			country = findSubElementByClassName(divGroup, "the-country-code");
			postalCode = findSubElementByClassName(divGroup, "the-postal-code");
			isp = findSubElementByClassName(divGroup, "the-isp");
			timezone = findSubElementByClassName(divGroup, "the-time-zone");
			latitude = findSubElementByClassName(divGroup, "the-latitude");
			longitude = findSubElementByClassName(divGroup, "the-longitude");
			return this;
 		}
		
		static String findSubElementByClassName(WebElement parent, String name) {
			if (null == parent) { return null; }
			if (StringUtils.isBlank(name)) { return null; }
			
			WebElement child = parent.findElement(By.className(name));
			if (null != child) {
				return child.getText();
			}
			return null;
		}
		
		private String ipAddress;
		private String city;
		private String region;
		private String country;
		private String postalCode;
		private String isp;
		private String timezone;
		private String latitude;
		private String longitude;
		
		public WebDriver getDriver() {
			return driver;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public String getCity() {
			return city;
		}

		public String getRegion() {
			return region;
		}

		public String getCountry() {
			return country;
		}

		public String getPostalCode() {
			return postalCode;
		}

		public String getIsp() {
			return isp;
		}

		public String getTimezone() {
			return timezone;
		}

		public String getLatitude() {
			return latitude;
		}

		public String getLongitude() {
			return longitude;
		}
		
		public static class GsonAdapater implements JsonSerializer<Ip2LocationResults> {

			@Override
			public JsonElement serialize(Ip2LocationResults results, Type type, JsonSerializationContext context) {
				if (null == results) { return JsonNull.INSTANCE; }
				
				JsonObject json = new JsonObject();
				json.addProperty("city", results.getCity());
				json.addProperty("country", results.getCountry());
				json.addProperty("ipAddress", results.getIpAddress());
				json.addProperty("isp", results.getIsp());
				json.addProperty("latitude", results.getLatitude());
				json.addProperty("longitude", results.getLongitude());
				json.addProperty("postalCode", results.getPostalCode());
				json.addProperty("region", results.getRegion());
				json.addProperty("timezone", results.getTimezone());
				return json;
			}
		}
		
		private static final Gson gson = new GsonBuilder()
												.disableHtmlEscaping()
												.serializeNulls()
												.registerTypeAdapter(Ip2LocationResults.class, new Ip2LocationResults.GsonAdapater())
												.create();
		
		@Override
		public String toString() {
			return gson.toJson(this);
		}
	}
	
	public static class IpAddressLabsResults {
		
		private WebDriver driver;
		public IpAddressLabsResults() {}
		
		public IpAddressLabsResults(WebDriver driver) {
			this.driver = driver;
			PageFactory.initElements(driver, this);
		}
		
		static String findSubElementByClassName(WebElement parent, String name) {
			if (null == parent) { return null; }
			if (StringUtils.isBlank(name)) { return null; }
			
			WebElement child = parent.findElement(By.className(name));
			if (null != child) {
				return child.getText();
			}
			return null;
		}
		
		public IpAddressLabsResults load() {
			if (null == divGroup) {
				divGroup = driver.findElement(By.className("ip-lookup-info"));
			}
			ipAddress = findSubElementByClassName(divGroup, "the-lookup-ip");
			city = findSubElementByClassName(divGroup, "the-city");
			region = findSubElementByClassName(divGroup, "the-region");
			country = findSubElementByClassName(divGroup, "the-country-code");
			postalCode = findSubElementByClassName(divGroup, "the-postal-code");
			isp = findSubElementByClassName(divGroup, "the-isp");
			latitude = findSubElementByClassName(divGroup, "the-latitude");
			longitude = findSubElementByClassName(divGroup, "the-longitude");
			return this;
 		}
		
		@FindBy(className="ip-lookup-info-lab")
		private WebElement divGroup;
		
		private String ipAddress;
		private String city;
		private String region;
		private String country;
		private String postalCode;
		private String isp;
		private String latitude;
		private String longitude;
		
		public WebDriver getDriver() {
			return driver;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public String getCity() {
			return city;
		}

		public String getRegion() {
			return region;
		}

		public String getCountry() {
			return country;
		}

		public String getPostalCode() {
			return postalCode;
		}

		public String getIsp() {
			return isp;
		}

		public String getLatitude() {
			return latitude;
		}

		public String getLongitude() {
			return longitude;
		}
		
		public static class GsonAdapater implements JsonSerializer<IpAddressLabsResults> {

			@Override
			public JsonElement serialize(IpAddressLabsResults results, Type type, JsonSerializationContext context) {
				if (null == results) { return JsonNull.INSTANCE; }
				
				JsonObject json = new JsonObject();
				json.addProperty("city", results.getCity());
				json.addProperty("country", results.getCountry());
				json.addProperty("ipAddress", results.getIpAddress());
				json.addProperty("isp", results.getIsp());
				json.addProperty("latitude", results.getLatitude());
				json.addProperty("longitude", results.getLongitude());
				json.addProperty("postalCode", results.getPostalCode());
				json.addProperty("region", results.getRegion());
				return json;
			}
		}
		
		private static final Gson gson = new GsonBuilder()
												.disableHtmlEscaping()
												.serializeNulls()
												.registerTypeAdapter(IpAddressLabsResults.class, new IpAddressLabsResults.GsonAdapater())
												.create();
		
		@Override
		public String toString() {
			return gson.toJson(this);
		}
	}
}
