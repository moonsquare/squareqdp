package com.qdevelop.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class AbstractProperties {
	protected Properties props = new Properties();
	public void loadProperties(String propertiesFile){
		try {
			InputStream in = new BufferedInputStream (QSource.getInstance().getSourceAsStream(propertiesFile));
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getValue(String key){
		return props.getProperty (key);
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<String> getFullKeys(){
		return (Enumeration<String>) props.propertyNames();
	}
}
