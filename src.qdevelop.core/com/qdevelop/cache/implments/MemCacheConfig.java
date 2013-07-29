package com.qdevelop.cache.implments;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import com.qdevelop.cache.interfaces.ITacties;
import com.qdevelop.cache.tactics.SecondTactiesImpl;
import com.qdevelop.utils.QClass;
import com.qdevelop.utils.QSource;
import com.qdevelop.utils.files.IQFileLoader;

public class MemCacheConfig extends HashMap<String,Integer> implements IQFileLoader{
	private static MemCacheConfig _MemCacheConfig = new MemCacheConfig();
	public static MemCacheConfig getInstance(){return _MemCacheConfig;} 
	protected Properties props = new Properties();
	private static final long serialVersionUID = 6478783437541099391L;
	public static Integer defaultTimer = 2592000;//one month!
	public static Integer ONE_MONTH = 2592000;
	public static Integer ONE_DAY = 86400;
	public static int tmpCacheTimer = 60;
	public Integer maxWaitTimer,maxPool;
	public boolean isDevModel;
	private String memcachedServiceURL ;
	@SuppressWarnings("unchecked")
	public MemCacheConfig(){
		try {
			this.put("default", 86400);
			props.load(QSource.getInstance().getSourceAsStream("memcached.properties"));
			memcachedServiceURL = props.getProperty("memcachedServiceURL");
			if(memcachedServiceURL==null)memcachedServiceURL = "115.182.92.241:12000";
			maxWaitTimer = props.getProperty("memcache_max_wait")==null?1000:Integer.parseInt(props.getProperty("memcache_max_wait"));
			defaultTimer = props.getProperty("config_default")==null?0:Integer.parseInt(props.getProperty("config_default"));

			maxPool = props.getProperty("memcache_max_pool")==null?1000:Integer.parseInt(props.getProperty("memcache_max_pool"));
			isDevModel = props.getProperty("memcache_dev")==null?true:Boolean.parseBoolean(props.getProperty("memcache_dev"));
			
			this.put("s_short", new Integer(3600));
			this.put("s_1min", new Integer(60));
			this.put("s_5min", new Integer(300));
			this.put("s_1hour", new Integer(3600));
			this.put("s_1day", new Integer(86400));
			
			Enumeration<String> keys = (Enumeration<String>) props.propertyNames();
			while (keys.hasMoreElements()) {
				String key =  keys.nextElement();
				if(key.startsWith("config_")){
					this.put(key.substring(7), Integer.parseInt(props.getProperty(key)));
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Integer getConfig(String config){
		if(config == null || this.get(config)==null)return defaultTimer;
		if(config.equals("tmp"))return tmpCacheTimer;
		return this.get(config);
	}

	public boolean hasConfig(String config){
		return this.get(config)!=null;
	}
	public String getServiceURL(){
		return this.memcachedServiceURL;
	}
	
	/**
	 * 
	 * TODO 加载二级缓存自定义缓存策略 
	 * 
	 * @return
	 */
	public ITacties getSendCondCacheTactiesImpl(){
		String implClass =props.getProperty("second_tacties_class");
		if(implClass == null)return new SecondTactiesImpl();
		ITacties _ITacties = (ITacties) QClass.getInstanceClass(implClass);
		if(_ITacties==null)_ITacties = new SecondTactiesImpl();
		return _ITacties;
	}
	
	@Override
	public void reload() {
		super.clear();
		 _MemCacheConfig = new MemCacheConfig();
	}
}
