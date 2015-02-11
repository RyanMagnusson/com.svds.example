package com.svds.example.geolocation.sqlite;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.geolocation.HybridGeoLocation;
import com.svds.example.geolocation.HybridGeoLocationDAO;
import com.svds.example.geolocation.selenium.whatismyip.Ip2LocationResults;
import com.svds.example.geolocation.selenium.whatismyip.IpAddressLabsResults;

/**
 * @author rmagnus
 */
public class SqliteHybridGeoLocationDAO implements HybridGeoLocationDAO {

	// register the driver 
    private static final String DRIVER_NAME = "org.sqlite.JDBC";
    private static final String TABLE_NAME = "hybrid";
    
    private volatile String database;
    public String getDatabase() {
    	String name = database;
    	if (StringUtils.isBlank(name)) {
    		synchronized(this) {
    			name = database;
    			if (StringUtils.isBlank(name)) {
    				database = name = "geolocation.sqlite";
    			}
    			
    			// if the path provided is a directory
    			// use the default name there
    			File f = new File(name);
    	    	if (f.exists() && f.isDirectory()) {
    	    		if (!name.endsWith(File.pathSeparator)) {
    	    			name += File.pathSeparator;
    	    		}
    	    		database = name = name + "geolocation.sqlite";
    	    	}
    		}
    	}
    	return name;
    }
    
    private volatile Class<?> driverClass;
    public Class<?> getDriver() throws GeoLocationException {
    	Class<?> dClazz = driverClass;
    	if (null == dClazz) {
    		synchronized(this) {
    			dClazz = driverClass;
    			if (null == dClazz) {
    				try {
						driverClass = dClazz = Class.forName(DRIVER_NAME);
					} 
    				catch (ClassNotFoundException e) {
    					throw new GeoLocationException("Unable to find the driver class: " + DRIVER_NAME,e);
					}
    			}
    		}
    	}
    	return dClazz;
    }
    
    private volatile Boolean tableExists = null;
    public boolean doesTableExist() throws GeoLocationException {
    	Boolean result = tableExists;
    	if (null == result) {
    		synchronized(this) {
    			result = tableExists;
    			if (null == result) {
    				createTable();
    				tableExists = result = true;
    			}
    		}
    	}
    	return result;
    }
    
    public String getURL() {
    	return "jdbc:sqlite:" + getDatabase();
    }

    int iTimeout = 30;
    
    public boolean checkTableExists() throws GeoLocationException {
    	getDriver();
    	
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = DriverManager.getConnection(getURL());
    	}
    	catch (SQLException e) {
    		throw new GeoLocationException("SQLException trying to open a connection to the database",e)
    						.setContextValue("url", getURL())
    						.setContextValue("sqlState", e.getSQLState())
    						.setContextValue("sqlError", e.getErrorCode())
    						.setContextValue("message", ExceptionUtils.getMessage(e))
    						;
    	}
    	//final String sql = "CREATE TABLE IF NOT EXISTS locations (ip_address text, longitude real, latitude real, isp_name text, organisation text)";
    	final String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=? UNION SELECT name FROM sqlite_temp_master WHERE type='table' AND name = ?";
    	try {
	    	try {
		    	ps = conn.prepareStatement(sql);
		    	DbHelper.setString(1, TABLE_NAME, ps);
		    	DbHelper.setString(2, TABLE_NAME, ps);
		    	ResultSet rs = ps.executeQuery();
		    	while(rs.next()) {
		    		final String name = DbHelper.getString("name", rs);
		    		return StringUtils.isNotBlank(name);
		    	}
		    	return false;
	    	}
	    	catch (SQLException e) {
	    		throw new GeoLocationException("SQLException trying to create the locations table",e)
				.setContextValue("url", getURL())
				.setContextValue("sqlState", e.getSQLState())
				.setContextValue("sqlError", e.getErrorCode())
				.setContextValue("sql", sql)
				.setContextValue("message", ExceptionUtils.getMessage(e))
				;
	    	}
    	}
    	finally {
    		DbHelper.close(conn);
    	}
    }
    
    public int createTable() throws GeoLocationException {
    	getDriver();
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = DriverManager.getConnection(getURL());
    	}
    	catch (SQLException e) {
    		throw new GeoLocationException("SQLException trying to open a connection to the database",e)
    						.setContextValue("url", getURL())
    						.setContextValue("sqlState", e.getSQLState())
    						.setContextValue("sqlError", e.getErrorCode())
    						.setContextValue("message", ExceptionUtils.getMessage(e))
    						;
    	}
    	final String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(ip_address TEXT PRIMARY KEY NOT NULL"
    			         + ", mm_longitude real, mm_latitude real, mm_isp_name TEXT, mm_organisation TEXT"
    			         + ", ipinfo_longitude real, ipinfo_latitude real, ipinfo_isp_name TEXT, ipinfo_organisation TEXT"
    			         + ", ip2loc_longitude real, ip2loc_latitude real, ip2loc_isp_name TEXT, ip2loc_organisation TEXT"
    			         + ", ip2loc_city TEXT, ip2loc_region TEXT, ip2loc_country TEXT, ip2loc_postal_code TEXT, ip2loc_timezone TEXT"
    			         + ", iplabs_longitude real, iplabs_latitude real, iplabs_isp_name TEXT, iplabs_organisation TEXT"
    			         + ", iplabs_city TEXT, iplabs_region TEXT, iplabs_country TEXT, iplabs_postal_code TEXT)";
    	try {
	    	try {
		    	ps = conn.prepareStatement(sql);
		    	return ps.executeUpdate();
	    	}
	    	catch (SQLException e) {
	    		throw new GeoLocationException("SQLException trying to create the locations table",e)
				.setContextValue("url", getURL())
				.setContextValue("sqlState", e.getSQLState())
				.setContextValue("sqlError", e.getErrorCode())
				.setContextValue("sql", sql)
				.setContextValue("message", ExceptionUtils.getMessage(e))
				;
	    	}
    	}
    	finally {
    		DbHelper.close(conn);
    	}
    }
    	
	/**
	 * 
	 */
	public SqliteHybridGeoLocationDAO() {}

	@Inject
	public SqliteHybridGeoLocationDAO(@Named("hybrid db") String database) {
		this.database = database;
	}
	
	/**
	 * @see com.svds.example.accesslog.GeoLocationService#find(java.lang.String)
	 */
	@Override
	public HybridGeoLocation get(String ipAddress) throws GeoLocationException {
		if (StringUtils.isBlank(ipAddress)) { return null; }
		
		doesTableExist();
		Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = DriverManager.getConnection(getURL());
    	}
    	catch (SQLException e) {
    		throw new GeoLocationException("SQLException trying to open a connection to the database",e)
    						.setContextValue("url", getURL())
    						.setContextValue("sql:state", e.getSQLState())
    						.setContextValue("sql:error", e.getErrorCode())
    						.setContextValue("message", ExceptionUtils.getMessage(e))
    						;
    	}
    	final String sql = "SELECT ip_address, mm_longitude , mm_latitude , mm_isp_name, mm_organisation"
    			         + ", ipinfo_longitude , ipinfo_latitude , ipinfo_isp_name , ipinfo_organisation"
    			         + ", ip2loc_longitude , ip2loc_latitude , ip2loc_isp_name , ip2loc_organisation"
    			         + ", ip2loc_city , ip2loc_region , ip2loc_country , ip2loc_postal_code , ip2loc_timezone"
    			         + ", iplabs_longitude , iplabs_latitude , iplabs_isp_name , iplabs_organisation"
    			         + ", iplabs_city , iplabs_region , iplabs_country , iplabs_postal_code FROM " + TABLE_NAME + " WHERE ip_address = ?";
    	try {
    	
	    	try {
	    		ps = conn.prepareStatement(sql);
	    		DbHelper.setString(1, ipAddress, ps);
	    		ResultSet rs = ps.executeQuery();
	    		while (rs.next()) {
	    			HybridGeoLocation location = new HybridGeoLocation();
	    			location.setIpAddress(ipAddress);
	    			Double mmLongitude = DbHelper.getDouble("mm_longitude", rs);
	    			Double mmLatitude = DbHelper.getDouble("mm_latitude", rs);
	    			String mmIspName = DbHelper.getString("mm_isp_name", rs);
	    			String mmOrganization = DbHelper.getString("mm_organisation", rs);
	    			if (null != mmLongitude
	    					|| null != mmLatitude
	    					|| StringUtils.isNotBlank(mmIspName)
	    					|| StringUtils.isNotBlank(mmOrganization)) {
	    				GeoLocation maxMind = new GeoLocation();
	    				maxMind.setIpAddress(ipAddress);
	    				maxMind.setLatitude(mmLatitude);
	    				maxMind.setLongitude(mmLongitude);
	    				maxMind.setNameOfIsp(mmIspName);
	    				maxMind.setOrganization(mmOrganization);
	    				location.setMaxmindData(maxMind);
	    			}
	    			
	    			Double ipinfoLongitude = DbHelper.getDouble("ipinfo_longitude", rs);
	    			Double ipinfoLatitude = DbHelper.getDouble("ipinfo_latitude", rs);
	    			String ipinfoIspName = DbHelper.getString("ipinfo_isp_name", rs);
	    			String ipinfoOrganization = DbHelper.getString("ipinfo_organisation", rs);
	    			if (null != ipinfoLongitude
	    					|| null != ipinfoLatitude
	    					|| StringUtils.isNotBlank(ipinfoIspName)
	    					|| StringUtils.isNotBlank(ipinfoOrganization)) {
	    				GeoLocation ipinfo = new GeoLocation();
	    				ipinfo.setIpAddress(ipAddress);
	    				ipinfo.setLatitude(ipinfoLatitude);
	    				ipinfo.setLongitude(ipinfoLongitude);
	    				ipinfo.setNameOfIsp(ipinfoIspName);
	    				ipinfo.setOrganization(ipinfoOrganization);
	    				location.setIpInfoData(ipinfo);
	    			}
	    			
	    			Double ip2Longitude = DbHelper.getDouble("ip2loc_longitude", rs);
	    			Double ip2Latitude = DbHelper.getDouble("ip2loc_latitude", rs);
	    			String ip2IspName = DbHelper.getString("ip2loc_isp_name", rs);
	    			String ip2Organisation = DbHelper.getString("ip2loc_organisation", rs);
	    			String ip2City = DbHelper.getString("ip2loc_city", rs);
	    			String ip2Region = DbHelper.getString("ip2loc_region", rs);
	    			String ip2Country = DbHelper.getString("ip2loc_country", rs);
	    			String ip2PostalCode = DbHelper.getString("ip2loc_postal_code", rs);
	    			String ip2Timezone = DbHelper.getString("ip2loc_timezone", rs);
	    			if (null != ip2Longitude
	    					|| null != ip2Latitude
	    					|| StringUtils.isNotBlank(ip2IspName)
	    					|| StringUtils.isNotBlank(ip2Organisation)
	    					|| StringUtils.isNotBlank(ip2City)
	    					|| StringUtils.isNotBlank(ip2Region)
	    					|| StringUtils.isNotBlank(ip2Country)
	    					|| StringUtils.isNotBlank(ip2PostalCode)
	    					|| StringUtils.isNotBlank(ip2Timezone)) {
	    				Ip2LocationResults ip2 = new Ip2LocationResults();
	    				ip2.setIpAddress(ipAddress);
	    				ip2.setLatitude(ip2Longitude);
	    				ip2.setLongitude(ip2Latitude);
	    				ip2.setNameOfIsp(ip2IspName);
	    				ip2.setOrganization(ip2Organisation);
	    				ip2.setCity(ip2City);
	    				ip2.setRegion(ip2Region);
	    				ip2.setCountry(ip2Country);
	    				ip2.setPostalCode(ip2PostalCode);
	    				ip2.setTimezone(ip2Timezone);
	    				location.setIp2LocationData(ip2);
	    			}
	    			
	    			Double ipLabLongitude = DbHelper.getDouble("iplabs_longitude", rs);
	    			Double ipLabLatitude = DbHelper.getDouble("iplabs_latitude", rs);
	    			String ipLabIspName = DbHelper.getString("iplabs_isp_name", rs);
	    			String ipLabOrganisation = DbHelper.getString("iplabs_organisation", rs);
	    			String ipLabCity = DbHelper.getString("iplabs_city", rs);
	    			String ipLabRegion = DbHelper.getString("iplabs_region", rs);
	    			String ipLabCountry = DbHelper.getString("iplabs_country", rs);
	    			String ipLabPostalCode = DbHelper.getString("iplabs_postal_code", rs);
	    			if (null != ipLabLongitude
	    					|| null != ipLabLatitude
	    					|| StringUtils.isNotBlank(ipLabIspName)
	    					|| StringUtils.isNotBlank(ipLabOrganisation)
	    					|| StringUtils.isNotBlank(ipLabCity)
	    					|| StringUtils.isNotBlank(ipLabRegion)
	    					|| StringUtils.isNotBlank(ipLabCountry)
	    					|| StringUtils.isNotBlank(ipLabPostalCode)) {
	    				IpAddressLabsResults ipLab = new IpAddressLabsResults();
	    				ipLab.setIpAddress(ipAddress);
	    				ipLab.setLatitude(ipLabLongitude);
	    				ipLab.setLongitude(ipLabLatitude);
	    				ipLab.setNameOfIsp(ipLabIspName);
	    				ipLab.setOrganization(ipLabOrganisation);
	    				ipLab.setCity(ipLabCity);
	    				ipLab.setRegion(ipLabRegion);
	    				ipLab.setCountry(ipLabCountry);
	    				ipLab.setPostalCode(ipLabPostalCode);
	    				location.setIpAddressLabsData(ipLab);
	    			}
	    			return location;
	    		}
	    		return null;
	    	}
	    	catch (SQLException e) {
	    		throw new GeoLocationException("Error trying to query the database",e)
	    						.setContextValue("url", getURL())
	    						.setContextValue("sqlState", e.getSQLState())
	    						.setContextValue("sqlError", e.getErrorCode())
	    						.setContextValue("message", ExceptionUtils.getMessage(e))
	    						;
	    	}
    	}
	    finally {
	    	DbHelper.close(conn);
	    }
	}

	static class DbHelper {
		
		public static Double setDouble(int index, BigDecimal value, PreparedStatement ps) throws SQLException {
			if (null == value) {
				ps.setNull(index, java.sql.Types.DOUBLE);
				return null;
			}
			
			Double dbl = value.doubleValue();
			ps.setDouble(index, dbl);
			return dbl;
		}
		
		public static String setString(int index, String value, PreparedStatement ps) throws SQLException {
			if (null == value) {
				ps.setNull(index, java.sql.Types.CHAR);
				return null;
			}
			
			ps.setString(index, value);
			return value;
		}
		
		public static String getString(String column, ResultSet rs) throws SQLException {
			String value = rs.getString(column);
			return rs.wasNull() ? null : value;
		}
		
		public static Double getDouble(String column, ResultSet rs) throws SQLException {
			Double value = rs.getDouble(column);
			return rs.wasNull() ? null : value;
		}
		
		public static void close(Connection connection) {
			if (null == connection) { return; }
			
			try {
				connection.close();
			}
			catch (SQLException e) {
				/* eat it */
			}
		}
	}
	
	/**
	 * @see com.svds.example.geolocation.GeoLocationDAO#save(com.svds.example.accesslog.GeoLocation)
	 */
	@Override
	public int save(HybridGeoLocation location) throws GeoLocationException {
		if (null == location) { return 0; }
		
		doesTableExist();
		Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = DriverManager.getConnection(getURL());
    	}
    	catch (SQLException e) {
    		throw new GeoLocationException("SQLException trying to open a connection to the database",e)
    						.setContextValue("url", getURL())
    						.setContextValue("sql:state", e.getSQLState())
    						.setContextValue("sql:error", e.getErrorCode())
    						.setContextValue("message", ExceptionUtils.getMessage(e))
    						;
    	}
    	
    	try {
    	
	    	try {
	    		ps = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET mm_longitude = ?, mm_latitude = ?, mm_isp_name = ?, mm_organisation = ?"
   			         + ", ipinfo_longitude = ?= ?, ipinfo_latitude = ?, ipinfo_isp_name = ?, ipinfo_organisation = ?"
                     + ", ip2loc_longitude = ?, ip2loc_latitude = ?, ip2loc_isp_name = ?, ip2loc_organisation = ?"
   			         + ", ip2loc_city = ?, ip2loc_region = ?, ip2loc_country = ?, ip2loc_postal_code = ?, ip2loc_timezone = ?"
   			         + ", iplabs_longitude = ?, iplabs_latitude = ?, iplabs_isp_name = ?, iplabs_organisation = ?"
   			         + ", iplabs_city = ?, iplabs_region = ?, iplabs_country = ?, iplabs_postal_code = ? WHERE ip_address = ?");
	    		DbHelper.setDouble(1, null == location.getMaxmindData() ? null : location.getMaxmindData().getLongitude(), ps);
	    		DbHelper.setDouble(2, null == location.getMaxmindData() ? null : location.getMaxmindData().getLatitude(), ps);
	    		DbHelper.setString(3, null == location.getMaxmindData() ? null : location.getMaxmindData().getNameOfIsp(), ps);
	    		DbHelper.setString(4, null == location.getMaxmindData() ? null : location.getMaxmindData().getOrganization(), ps);
	    		
	    		DbHelper.setDouble(5, null == location.getIpInfoData() ? null : location.getIpInfoData().getLongitude(), ps);
	    		DbHelper.setDouble(6, null == location.getIpInfoData() ? null : location.getIpInfoData().getLatitude(), ps);
	    		DbHelper.setString(7, null == location.getIpInfoData() ? null : location.getIpInfoData().getNameOfIsp(), ps);
	    		DbHelper.setString(8, null == location.getIpInfoData() ? null : location.getIpInfoData().getOrganization(), ps);
	    		
	    		DbHelper.setDouble(9, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getLongitude(), ps);
	    		DbHelper.setDouble(10, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getLatitude(), ps);
	    		DbHelper.setString(11, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getNameOfIsp(), ps);
	    		DbHelper.setString(12, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getOrganization(), ps);
	    		DbHelper.setString(13, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getCity(), ps);
	    		DbHelper.setString(14, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getRegion(), ps);
	    		DbHelper.setString(15, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getCountry(), ps);
	    		DbHelper.setString(16, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getPostalCode(), ps);
	    		DbHelper.setString(17, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getTimezone(), ps);
	    		
	    		DbHelper.setDouble(18, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getLongitude(), ps);
	    		DbHelper.setDouble(19, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getLatitude(), ps);
	    		DbHelper.setString(20, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getNameOfIsp(), ps);
	    		DbHelper.setString(21, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getOrganization(), ps);
	    		DbHelper.setString(22, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getCity(), ps);
	    		DbHelper.setString(23, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getRegion(), ps);
	    		DbHelper.setString(24, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getCountry(), ps);
	    		DbHelper.setString(25, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getPostalCode(), ps);

	    		DbHelper.setString(26, location.getIpAddress(), ps);
	    		
	    		int rows = ps.executeUpdate();
	    		if (1 > rows) {
	    			ps = conn.prepareStatement("INSERT INTO " + TABLE_NAME + "(ip_address, mm_longitude , mm_latitude , mm_isp_name, mm_organisation"
	    			         + ", ipinfo_longitude , ipinfo_latitude , ipinfo_isp_name , ipinfo_organisation"
	    			         + ", ip2loc_longitude , ip2loc_latitude , ip2loc_isp_name , ip2loc_organisation"
	    			         + ", ip2loc_city , ip2loc_region , ip2loc_country , ip2loc_postal_code , ip2loc_timezone"
	    			         + ", iplabs_longitude , iplabs_latitude , iplabs_isp_name , iplabs_organisation"
	    			         + ", iplabs_city , iplabs_region , iplabs_country , iplabs_postal_code) "
	    			         + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
	    			
	    			
	    			DbHelper.setString(1, location.getIpAddress(), ps);
	    			DbHelper.setDouble(2, null == location.getMaxmindData() ? null : location.getMaxmindData().getLongitude(), ps);
		    		DbHelper.setDouble(3, null == location.getMaxmindData() ? null : location.getMaxmindData().getLatitude(), ps);
		    		DbHelper.setString(4, null == location.getMaxmindData() ? null : location.getMaxmindData().getNameOfIsp(), ps);
		    		DbHelper.setString(5, null == location.getMaxmindData() ? null : location.getMaxmindData().getOrganization(), ps);
		    		
		    		DbHelper.setDouble(6, null == location.getIpInfoData() ? null : location.getIpInfoData().getLongitude(), ps);
		    		DbHelper.setDouble(7, null == location.getIpInfoData() ? null : location.getIpInfoData().getLatitude(), ps);
		    		DbHelper.setString(8, null == location.getIpInfoData() ? null : location.getIpInfoData().getNameOfIsp(), ps);
		    		DbHelper.setString(9, null == location.getIpInfoData() ? null : location.getIpInfoData().getOrganization(), ps);
		    		
		    		DbHelper.setDouble(10, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getLongitude(), ps);
		    		DbHelper.setDouble(11, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getLatitude(), ps);
		    		DbHelper.setString(12, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getNameOfIsp(), ps);
		    		DbHelper.setString(13, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getOrganization(), ps);
		    		DbHelper.setString(14, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getCity(), ps);
		    		DbHelper.setString(15, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getRegion(), ps);
		    		DbHelper.setString(16, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getCountry(), ps);
		    		DbHelper.setString(17, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getPostalCode(), ps);
		    		DbHelper.setString(18, null == location.getIp2LocationData() ? null : location.getIp2LocationData().getTimezone(), ps);
		    		
		    		DbHelper.setDouble(19, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getLongitude(), ps);
		    		DbHelper.setDouble(20, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getLatitude(), ps);
		    		DbHelper.setString(21, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getNameOfIsp(), ps);
		    		DbHelper.setString(22, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getOrganization(), ps);
		    		DbHelper.setString(23, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getCity(), ps);
		    		DbHelper.setString(22, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getRegion(), ps);
		    		DbHelper.setString(25, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getCountry(), ps);
		    		DbHelper.setString(26, null == location.getIpAddressLabsData() ? null : location.getIpAddressLabsData().getPostalCode(), ps);

		    		rows = ps.executeUpdate();
	    		}
	    		return rows;
	    	}
	    	catch (SQLException e) {
	    		throw new GeoLocationException("Error trying to update the database",e)
	    						.setContextValue("url", getURL())
	    						.setContextValue("sqlState", e.getSQLState())
	    						.setContextValue("sqlError", e.getErrorCode())
	    						.setContextValue("message", ExceptionUtils.getMessage(e))
	    						;
	    	}
    	}
	    finally {
	    	DbHelper.close(conn);
	    }
    }

	@Override
	public HybridGeoLocation delete(String address) throws GeoLocationException {
		if (StringUtils.isBlank(address)) { return null; }
		
		HybridGeoLocation result = get(address);
		
		if (null == result) { return null; }
		
		doesTableExist();
		Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = DriverManager.getConnection(getURL());
    	}
    	catch (SQLException e) {
    		throw new GeoLocationException("SQLException trying to open a connection to the database",e)
    						.setContextValue("url", getURL())
    						.setContextValue("sql:state", e.getSQLState())
    						.setContextValue("sql:error", e.getErrorCode())
    						.setContextValue("message", ExceptionUtils.getMessage(e))
    						;
    	}
    	
    	try {
    	
	    	try {
	    		ps = conn.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE ip_address = ?");
	    		DbHelper.setString(1, address, ps);
	    		ps.executeUpdate();
	    	}
	    	catch (SQLException e) {
	    		throw new GeoLocationException("Error trying to query the database",e)
	    						.setContextValue("url", getURL())
	    						.setContextValue("sqlState", e.getSQLState())
	    						.setContextValue("sqlError", e.getErrorCode())
	    						.setContextValue("message", ExceptionUtils.getMessage(e))
	    						;
	    	}
    	}
	    finally {
	    	DbHelper.close(conn);
	    }
		
		return result;
	}
}
