package com.qdevelop.core.connect;


import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.Element;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QString;

/**
 * DBCP 数据库链接池
 * @author Janson Gu
 * @version 1.0
 *
 */
public class DBCPConnect implements IConnect {
	private BasicDataSource bds;
	public String database;

	public DBCPConnect(Element config){
		if(bds==null)init(config);
	}

	public void close(Connection conn) {
		try {
			if(conn!=null&&!conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public int getCanUseNum(){
		if(bds==null)return 0;
		return bds.getMaxActive() - bds.getNumActive();
	}

	public Connection getConnection()  throws QDevelopException{
		try {
			return bds.getConnection();
		} catch (SQLException e) {
			throw new QDevelopException(e,QString.append(QDate.getNow("yyyy-MM-dd HH:mm:ss"),"\t数据库链接[URL:",bds.getUrl(),",user:",bds.getUsername(),"]获取异常!"));
		}
	}

	@SuppressWarnings("deprecation")
	public void init(Element config) {
		if(bds!=null){
			shutdown();
		}
		bds = new BasicDataSource();
		try {
			
			bds.setUrl(config.elementText("driver-url").replaceAll("\t|\r|\n| ", ""));
			bds.setDriverClassName(config.elementText("driver-class").replaceAll("\t|\r|\n| ", ""));
			bds.setUsername(config.elementText("user-name").replaceAll("\t|\r|\n| ", ""));		
			bds.setPassword(config.elementText("password").replaceAll("\t|\r|\n| ", ""));
			bds.setMinIdle(Integer.parseInt(getValue(config.elementText("min-idle").replaceAll("\t|\r|\n| ", ""),"1")));
			int initSize = Integer.parseInt(getValue(config.elementText("init-idle").replaceAll("\t|\r|\n| ", ""),"1"));
			int maxIdle = Integer.parseInt(getValue(config.elementText("max-idle").replaceAll("\t|\r|\n| ", ""),"1"));
			bds.setInitialSize(initSize);
			bds.setMaxIdle(initSize);
			bds.setMaxWait(Integer.parseInt(getValue(config.elementText("max-wait").replaceAll("\t|\r|\n| ", ""),"1")));
			bds.setValidationQuery(config.elementText("test-query").replaceAll("\t|\r|\n", ""));
			bds.setTestOnBorrow(Boolean.parseBoolean(getValue(config.elementText("test-borrow").replaceAll("\t|\r|\n| ", ""),"false"))); 
			bds.setTestOnReturn(Boolean.parseBoolean(getValue(config.elementText("test-return").replaceAll("\t|\r|\n| ", ""),"false"))); 
			bds.setTestWhileIdle(Boolean.parseBoolean(getValue(config.elementText("test-while-idle").replaceAll("\t|\r|\n| ", ""),"false")));
			bds.setMaxActive(maxIdle);
			
			bds.setLogAbandoned(true);
			bds.setRemoveAbandoned(true);
			bds.setRemoveAbandonedTimeout(60);
//			bds.setPoolPreparedStatements(true);
//			bds.setMaxOpenPreparedStatements(maxIdle);
			parseDataBase(config.elementText("driver-url"));
		} catch (Exception e) {
			QLog.getInstance().systemError(append("数据库连接池[",config.attributeValue("index"),"]初始话失败！"),e);
		}
	}

	private void parseDataBase(String driverUrl){
		if(driverUrl.toLowerCase().indexOf("oracle")>-1){
			database = "ORACLE";
		}else if(driverUrl.toLowerCase().indexOf("mysql")>-1){
			database = "MYSQL";
		}
	}
	private String getValue(String val,String defaultVal){
		if(val==null)return defaultVal;
		return val; 
	}

	public void printInfo() {
		System.out.println(append(
				"数据库地址:\t",bds.getUrl(),"\n",
				"用     户     名:\t",bds.getUsername(),"\n",
				"活动的连接数:\t",bds.getNumActive(),"\n",
				"剩余连接数:\t",bds.getNumIdle(),"\n",
				"最大连接数:\t",bds.getMaxActive()
		));
	}
	
	public String monitor(){
		return append(bds.getUrl(),"|",bds.getUsername(),"|",bds.getNumActive(),"|",bds.getNumIdle(),"|",bds.getMaxActive());
	}
	@Override
	public void init() {
	}
	@Override
	public String getDataBase() {
		return database;
	} 
	private static String append(Object ... s){
		StringBuffer sb = new StringBuffer();
		for(Object _s:s)sb.append(_s);
		return sb.toString();
	}
	@Override
	public void shutdown() {
		try {
			if(bds!=null){
				bds.close();
			}
			bds = null;
		} catch (SQLException ex) {
		}
	}
}
