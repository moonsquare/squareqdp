package com.qdevelop.utils.cache;


import java.io.Serializable;
import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.ObjectExistsException;

import com.qdevelop.utils.QLog;
import com.qdevelop.utils.UtilsFactory;

public class EHCache implements ICache{
	private static EHCache _EHCache = new EHCache();
	public static EHCache getInstance(){return _EHCache;}
	public EHCache(){
		initCache();
	}
	private CacheManager manager ;

	@Override
	public Object getCache(String key, String config) {
		if(manager == null)return null;
		try {
			Cache cache = manager.getCache(config);
			if(cache == null)return null;
			Element element = cache.get(key);
			if(element!=null){
				Object obj =  element.getValue();
				if(obj instanceof SerializCacheBean){
					return ((SerializCacheBean)obj).getValue();
				}
				return obj;
			}
		} catch (Exception e) {
			QLog.getInstance().systemError(e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object getCache(String key) {
		return getCache(key,DEFAULT_CACHE_CONFIG);
	}

	@Override
	public void initCache() {
		try {
			if(manager!=null)manager.shutdown();
			URL config = UtilsFactory.source().getResource("ehcache.xml");
			manager = CacheManager.create(config);
		} catch (Exception e) {
			QLog.getInstance().systemError(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCached(String key, String config) {
		return getCache(key,config)==null?false:true;
	}

	@Override
	public boolean isCached(String key) {
		return isCached(key,DEFAULT_CACHE_CONFIG);
	}

	@Override
	public void removeAllCache(String config) {
		if(manager!=null && manager.getCache(config)!=null){
			try {
				manager.getCache(config).removeAll();
			} catch (Exception e) {
				QLog.getInstance().systemError(e.getMessage(), e);
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void removeAllCache() {
		removeAllCache(DEFAULT_CACHE_CONFIG);
	}

	@Override
	public void removeCache(String key, String config) {
		if(manager!=null && manager.cacheExists(config)){
			try {
				manager.getCache(config).remove(key);
			} catch (IllegalStateException e) {
				QLog.getInstance().systemError(e.getMessage(), e);
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void removeCache(String key) {
		removeCache(key,DEFAULT_CACHE_CONFIG);
	}

	@Override
	public void setCache(String key, Object value, String config) {
		if(manager==null)return;
		try {
			Cache cache = manager.getCache(config);
			if(cache == null){
				cache = new Cache(config, 1000, true, false, 86400, 86400); 
				manager.addCache(cache);
			}
			if(value instanceof Serializable)
				cache.put(new Element(key, (Serializable)value)); 
			else 
				cache.put(new Element(key, new SerializCacheBean(value)));
		} catch (Exception e) {
			QLog.getInstance().systemError(e.getMessage(), e);
			e.printStackTrace();
		} 
	}
	
	public void createCache(String config,int maxElementsInMemory,boolean eternal,boolean overflowToDisk,long timeToIdleSeconds,long timeToLiveSeconds){
		try {
			Cache cache = new Cache(config, maxElementsInMemory, eternal, overflowToDisk, timeToIdleSeconds, timeToLiveSeconds); 
			manager.addCache(cache);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ObjectExistsException e) {
			e.printStackTrace();
		} catch (CacheException e) {
			e.printStackTrace();
		}
	}
	
	public Cache getCacheRoot(String config){
		return manager.getCache(config);
	}

	@Override
	public void setCache(String key, Object value) {
		setCache(key,value,DEFAULT_CACHE_CONFIG);
	}
	
	protected  CacheManager getCacheManager(){
		return manager;
	}
	@Override
	public boolean hasCacheByConfig(String config) {
		if(manager!=null&&manager.getCache(config)!=null)return true;
		return false;
	}

}
