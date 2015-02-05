package com.svds.example.geolocation.sqlite;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.annotations.DatabaseFile;
import com.svds.example.geolocation.GeoLocationDAO;

/**
 * @author rmagnus
 */
public class SqliteGeoLocationDAO implements GeoLocationDAO {

	// register the driver 
    private static final String DRIVER_NAME = "org.sqlite.JDBC";
    private static final String TABLE_NAME = "geo_locations";
    
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
    	final String sql = "CREATE TABLE IF NOT EXISTS locations (ip_address TEXT PRIMARY KEY NOT NULL, longitude real, latitude real, isp_name TEXT, organisation TEXT)";
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
	public SqliteGeoLocationDAO() {}

	@Inject
	public SqliteGeoLocationDAO(@DatabaseFile String database) {
		this.database = database;
	}
	
	/**
	 * @see com.svds.example.accesslog.GeoLocationService#find(java.lang.String)
	 */
	@Override
	public GeoLocation find(String ipAddress) throws GeoLocationException {
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
    	final String sql = "SELECT ip_address, longitude,  latitude, isp_name, organisation FROM locations WHERE ip_address = ?";
    	try {
    	
	    	try {
	    		ps = conn.prepareStatement(sql);
	    		DbHelper.setString(1, ipAddress, ps);
	    		ResultSet rs = ps.executeQuery();
	    		while (rs.next()) {
	    			GeoLocation location = new GeoLocation();
	    			location.setIpAddress(ipAddress);
	    			location.setLatitude(DbHelper.getDouble("latitude",rs));
	    			location.setLongitude(DbHelper.getDouble("longitude",rs));
	    			location.setNameOfIsp(DbHelper.getString("isp_name",rs));
	    			location.setOrganization(DbHelper.getString("organisation",rs));
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
	public int save(GeoLocation location) throws GeoLocationException {
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
	    		ps = conn.prepareStatement("UPDATE locations SET longitude = ?, latitude = ?, isp_name = ?, organisation = ? WHERE ip_address = ?");
	    		DbHelper.setDouble(1, location.getLongitude(), ps);
	    		DbHelper.setDouble(2, location.getLatitude(), ps);
	    		DbHelper.setString(3, location.getNameOfIsp(), ps);
	    		DbHelper.setString(4, location.getOrganization(), ps);
	    		DbHelper.setString(5, location.getIpAddress(), ps);
	    		
	    		int rows = ps.executeUpdate();
	    		if (1 > rows) {
	    			ps = conn.prepareStatement("INSERT INTO locations(ip_address, longitude, latitude, isp_name, organisation) VALUES(?,?,?,?,?)");
	    			DbHelper.setString(1, location.getIpAddress(), ps);
	    			DbHelper.setDouble(2, location.getLongitude(), ps);
		    		DbHelper.setDouble(3, location.getLatitude(), ps);
		    		DbHelper.setString(4, location.getNameOfIsp(), ps);
		    		DbHelper.setString(5, location.getOrganization(), ps);
		    		rows = ps.executeUpdate();
	    		}
	    		return rows;
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
}
