package com.svds.example.geolocation.selenium.whatismyip;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.svds.example.accesslog.GeoLocation;


/**
 * @author rmagnus
 *
 */
public class IpAddressLookupGroup extends GeoLocation {

	private static final long serialVersionUID = 8464252235846024208L;

	private WebDriver driver;
	private String ipAddress;
	
	/**
	 * 
	 */
	public IpAddressLookupGroup() {}
	
	protected IpAddressLookupGroup(WebDriver driver) {
		this.driver = driver;
	}
	
	protected IpAddressLookupGroup(WebDriver driver, String ipAddress) {
		this.driver = driver;
		this.ipAddress = findIpAddress(ipAddress);
	}
	
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	public WebDriver getDriver() { return driver; }

	private static final Pattern REGEX_REMOVE_HOSTNAME_FROM_ADDRESS = Pattern.compile("^((?<hostname>.*?)\\/)?(?<address>.*)$", Pattern.CASE_INSENSITIVE);
	protected static String findIpAddress(String address) {
		if (StringUtils.isBlank(address)) {
			return null;
		}
		
		Matcher matcher = REGEX_REMOVE_HOSTNAME_FROM_ADDRESS.matcher(address.trim());
		if (matcher.find()) {
			return matcher.group("address");
		}
		else {
			return address.trim();
		}
	}
	
	public void setIpAddress(String address) {
		this.ipAddress = findIpAddress(address);
	}
	
	public void setIpAddress(InetAddress address) {
		this.ipAddress = null == address ? null : address.getHostAddress();
	}
	
	public String getIpAddress() { return ipAddress; }
	
	protected static String findSubElementByClassName(WebElement parent, String name) {
		if (null == parent) { return null; }
		if (StringUtils.isBlank(name)) { return null; }
		
		WebElement child = parent.findElement(By.className(name));
		if (null != child) {
			return child.getText();
		}
		return null;
	}
	
}
