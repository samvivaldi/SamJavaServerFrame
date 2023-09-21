package com.samjava.damon.frame;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;


public abstract class DBManager {
	
	private static HashMap<String, DataSource> hmDB = new HashMap<String, DataSource>();

	public synchronized static void createDataSource(Properties dbProperties, String dbSourceName) throws Exception {
		
		if (hmDB.get(dbSourceName) != null) {
			throw new Exception("Aleady Exist DataSource Name");
		}
		
		hmDB.put(dbSourceName, BasicDataSourceFactory.createDataSource(dbProperties));
		
		
		BasicDataSource bds = (BasicDataSource)hmDB.get(dbSourceName);
		System.out.println("=============" + dbSourceName + " info DB ===================");
		System.out.println("====> db URL : " + bds.getUrl()); 
		System.out.println("====> DefaultAutoCommit : " + bds.getDefaultAutoCommit()); 
		System.out.println("====> InitialSize " + bds.getInitialSize()); 
		System.out.println("====> getMaxWaitMillis" +  bds.getMaxWaitDuration().toMillis()); 
		
		System.out.println("====> maxActive " +  bds.getMaxTotal()); 
		System.out.println("====> maxIdle" +  bds.getMaxIdle()); 
		System.out.println("====> minIdle" +  bds.getMinIdle ()); 
		System.out.println("=====================================================");
		
	}
	
	
	public static DataSource getDataSource(String dbSourceName) {
		return (DataSource)hmDB.get(dbSourceName);
	}
	
	public static Connection getConnection(String dbSourceName) throws SQLException {
		return hmDB.get(dbSourceName).getConnection();
	}	
	
	private DBManager() {
		;;
	}
}
