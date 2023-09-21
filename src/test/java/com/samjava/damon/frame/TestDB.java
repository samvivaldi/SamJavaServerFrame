package com.samjava.damon.frame;

import java.util.Properties;

public class TestDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties dbProperties = new Properties();
		
		dbProperties.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
		dbProperties.setProperty("url", "jdbc:oracle:thin:@10.1.110.110:1521:SAMCORP");
		dbProperties.setProperty("username", "DEVUSER");            
		dbProperties.setProperty("password", "samuser");
        
		dbProperties.setProperty("initialSize", "1");
		dbProperties.setProperty("maxWait", "1000");
//		dbProperties.setProperty("maxActive", "10");
		dbProperties.setProperty("maxIdle", "5");
		dbProperties.setProperty("minIdle", "1");
		dbProperties.setProperty("defaultAutoCommit", "false");

		try {
			DBManager.createDataSource(dbProperties, "devDB");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
