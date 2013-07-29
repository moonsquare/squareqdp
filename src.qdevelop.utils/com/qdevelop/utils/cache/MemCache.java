package com.qdevelop.utils.cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;

public class MemCache implements ICache{
	private QDevelopCacheIndex qdevelopCacheIndex;
	private MemCacheConfig cacheConfig;
	private MemcachedClient client;
	private static MemCache _MemCache = new MemCache();
	public MemCache(){
		initCache();
	}
	public static MemCache getInstance(){
		return _MemCache;
	}

	@Override
	public void initCache() {
		if(qdevelopCacheIndex!=null)qdevelopCacheIndex.clear();
		qdevelopCacheIndex = new QDevelopCacheIndex();
		cacheConfig =new MemCacheConfig();
		cacheConfig.reload();
		try {
			client = new XMemcachedClientBuilder(AddrUtil.getAddresses(cacheConfig.getServiceURL())).build();
			client.flushAll();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCached(String key) {
		return isCached(key,null);
	}

	@Override
	public void setCache(String key, Object value) {
		this.setCache(key, value, null);
	}

	@Override
	public Object getCache(String key) {
		return getCache(key,null);
	}



	@Override
	public boolean isCached(String key, String config) {
		try {
			return client.get(key)!=null;
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void setCache(String key, Object value, String config) {
		try {
			if(value instanceof Serializable)
				client.add(key, cacheConfig.getConfig(config), value);
			else 
				client.add(key, cacheConfig.getConfig(config), new SerializCacheBean(value));
			qdevelopCacheIndex.addCacheIndex(key, config);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object getCache(String key, String config) {
		try {
			Object val =  client.get(key);
			if(QProperties.isDebug)System.out.println("Cache ==> "+key);
			if(val instanceof SerializCacheBean){
				return ((SerializCacheBean)val).getValue();
			}else{ 
				return val;
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeCache(String key) {
		try {
			Map<String,List<String>> cacheIndex = qdevelopCacheIndex.findKeys(key);
			Iterator<String> itor = cacheIndex.keySet().iterator();
			String config;
			while(itor.hasNext()){
				config = itor.next();
				List<String> indexKeys = cacheIndex.get(config);
				for(String k:indexKeys){
					if(QProperties.isDebug)System.out.println(QString.append("===>Remove Index Cached ",k," (",config,")"));
					client.delete(k);
				}
				indexKeys.clear();
			}
			cacheIndex.clear();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeAllCache() {

	}
	
	@Override
	public void removeCache(String key, String config) {
		try {
			if(key.indexOf("@")>-1){
				key = key.replaceAll("@.+?\\|", "|").replaceAll("@.+?$|\\^.+\\^", "");
			}
			List<String> indexKeys = qdevelopCacheIndex.findKeys(config, key.split(";|\\|"));
			for(String k:indexKeys){
				if(QProperties.isDebug)System.out.println(QString.append("===>Remove Index Cached ",k," (",config,")"));
				client.delete(k);
			}
			indexKeys.clear();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeAllCache(String config) {

	}

	@Override
	public boolean hasCacheByConfig(String config) {
		return cacheConfig.hasConfig(config);
	}

}
