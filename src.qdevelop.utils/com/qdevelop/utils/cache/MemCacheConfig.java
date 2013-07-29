package com.qdevelop.utils.cache;

import java.util.Enumeration;
import java.util.HashMap;

import com.qdevelop.utils.AbstractProperties;
import com.qdevelop.utils.files.IQFileLoader;

public class MemCacheConfig extends HashMap<String,Integer> implements IQFileLoader{
	private AbstractProperties ap = new AbstractProperties(); 
	private static final long serialVersionUID = 6478783437541099391L;
	private Integer defaultTimer = 86400;
	private String memcachedServiceURL ;
	public MemCacheConfig(){
		this.put("default", 86400);
		ap.loadProperties("memcached.properties");
		memcachedServiceURL = ap.getValue("memcachedServiceURL");
		if(memcachedServiceURL==null)memcachedServiceURL = "115.182.92.241:12000"; 

		Enumeration<String> keys = ap.getFullKeys();
		while (keys.hasMoreElements()) {
			String key =  keys.nextElement();
			if(key.startsWith("config_")){
				this.put(key.substring(7), Integer.parseInt(ap.getValue(key)));
			}
		}
	}

	public Integer getConfig(String config){
		if(config == null || this.get(config)==null)return defaultTimer;
		return this.get(config);
	}

	public boolean hasConfig(String config){
		return this.get(config)!=null;
	}
	public String getServiceURL(){
		return this.memcachedServiceURL;
	}
	@Override
	public void reload() {

	}
}
