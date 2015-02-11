/**
 * 
 */
package com.svds.example.geolocation.selenium.whatismyip;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author rmagnus
 *
 */
public class Ip2LocationResults extends IpAddressLookupGroup {
	
	private static final long serialVersionUID = -2606511420638338724L;
	private Logger logger = LogManager.getLogger();
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
		final String ipAddress = findSubElementByClassName(divGroup, "the-lookup-ip");
		super.setIpAddress(ipAddress);
		
		final String latitude = findSubElementByClassName(divGroup, "the-latitude");
		if (StringUtils.isNotBlank(latitude)) {
			if (NumberUtils.isNumber(latitude)) {
				BigDecimal decimal = new BigDecimal(latitude, MathContext.DECIMAL64);
				super.setLatitude(decimal);
			}
			else {
				logger.info("The found latitude for address: " + ipAddress + " is not a number: " + latitude);
			}
		}
		
		final String longitude = findSubElementByClassName(divGroup, "the-longitude");
		if (StringUtils.isNotBlank(longitude)) {
			if (NumberUtils.isNumber(longitude)) {
				BigDecimal decimal = new BigDecimal(longitude, MathContext.DECIMAL64);
				super.setLongitude(decimal);
			}
			else {
				logger.info("The found longitude for address: " + ipAddress + " is not a number: " + longitude);
			}
		}
		
		final String isp = findSubElementByClassName(divGroup, "the-isp");
		if (StringUtils.isBlank(isp)) {
			logger.info("The found ISP name for address: " + ipAddress + " was empty or whitespace.");
		}
		else {
			super.setNameOfIsp(isp);
		}
		
		city = findSubElementByClassName(divGroup, "the-city");
		region = findSubElementByClassName(divGroup, "the-region");
		country = findSubElementByClassName(divGroup, "the-country-code");
		postalCode = findSubElementByClassName(divGroup, "the-postal-code");
		timezone = findSubElementByClassName(divGroup, "the-time-zone");
		
		return this;
	}
	
	private String city;
	private String region;
	private String country;
	private String postalCode;
	private String timezone;
	private String latitude;
	private String longitude;
	
	public WebDriver getDriver() {
		return driver;
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

	public String getTimezone() {
		return timezone;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}



	public static class GsonAdapater implements JsonSerializer<Ip2LocationResults> {

		@Override
		public JsonElement serialize(Ip2LocationResults results, Type type, JsonSerializationContext context) {
			if (null == results) { return JsonNull.INSTANCE; }
			
			JsonObject json = new JsonObject();
			json.addProperty("city", results.getCity());
			json.addProperty("country", results.getCountry());
			json.addProperty("ipAddress", results.getIpAddress());
			json.addProperty("isp", results.getNameOfIsp());
			if (null != results.getLatitude()) {
				json.addProperty("latitude", results.getLatitude());
			}
			else {
				json.addProperty("latitude", results.latitude);
			}
			json.addProperty("longitude", results.getLongitude());
			if (null != results.getLatitude()) {
				json.addProperty("longitude", results.getLongitude());
			}
			else {
				json.addProperty("longitude", results.longitude);
			}
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