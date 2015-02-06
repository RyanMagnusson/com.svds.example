package com.svds.example.geolocation.selenium.whatismyip;

import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Test_When_Using_Selenium_To_Query_WhatIsMyIpAddress {
	private static final Logger log = LogManager.getLogger();
	private WebDriver driver;
	private InetAddress address;
	private IpAddressLookupPage page;
	
	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		address = InetAddress.getByName("104.13.194.44");	
		page = new IpAddressLookupPage(driver).open();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void test() {
		IpAddressLookupPage result = page.submitQuery(address);
		assertNotNull(result);
		
		log.info(result.getIp2LocationResults());
		log.info(result.getIpAddressLabsResults());
		
	}

}
