package com.qdevelop.web.handler;

import java.io.File;
import java.util.Properties;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QSource;


public class LogSourceReader {
	private static  LogSourceReader _LogSourceReader = new LogSourceReader();
	public static LogSourceReader getInstance(){return _LogSourceReader;}
	
	public LogSourceReader() throws QDevelopException{
		try {
			prop = new Properties();
//			FileInputStream fis = ;  
//				new FileInputStream("qdevelop.log.properties");
			prop.load(QSource.getInstance().getSourceAsStream("qdevelop.log.properties"));
		} catch (Exception e) {
			throw new QDevelopException(e);
		} 
	}
	private Properties prop;
	
	public File getLoginLog() throws QDevelopException{
		return new File(prop.getProperty("log4j.appender.security.file")); 
	}
	
}
