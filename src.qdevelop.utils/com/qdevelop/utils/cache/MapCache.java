package com.qdevelop.utils.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;

@SuppressWarnings({"unchecked","rawtypes"})
public class MapCache extends ConcurrentHashMap implements ICache{
	private static MapCache _MemeryCache = new MapCache();
	public static MapCache getInstance(){
		return _MemeryCache;
	}

	private static final long serialVersionUID = 3176332909918694678L;

	@Override
	public Object getCache(String key, String config) {
		if(this.get(config)==null)return null;
//		if(QProperties.getInstance().isDebug)System.out.println(QString.append("===>Map Cached ",key," (",config,")"));
		return((Map)this.get(config)).get(key);
	}

	@Override
	public void initCache() {
		_MemeryCache.clear();
		_MemeryCache = null;
		_MemeryCache = new MapCache();
	}

	@Override
	public boolean isCached(String key, String config) {
		return getCache(key,config) == null?false:true;
	}

	@Override
	public void removeAllCache(String config) {
		if(this.get(config)!=null){
			((Map)this.get(config)).clear();
		}
	}
	
	public Map getCacheByConfig(String config){
		return (Map)this.get(config);
	}
	

	@Override
	public void removeCache(String key, String config) {
		if(this.get(config)!=null){
			if(QProperties.isDebug)System.out.println(QString.append("===>Remove Map Cache  ",key," (",config,")"));
			((Map)this.get(config)).remove(key);
		}
	}

	@Override
	public void setCache(String key, Object value, String config) {
		Map tmp = (Map)this.get(config);
		if(this.get(config)==null){
			tmp = new ConcurrentHashMap();
			this.put(config, tmp);
		}
		tmp.put(key, value);
	}

	@Override
	public Object getCache(String key) {
		return getCache(key,DEFAULT_CACHE_CONFIG);
	}

	@Override
	public boolean isCached(String key) {
		return isCached(key,DEFAULT_CACHE_CONFIG);
	}

	@Override
	public void removeAllCache() {
		removeAllCache(DEFAULT_CACHE_CONFIG);
	}

	@Override
	public void removeCache(String key) {
		removeCache(key,DEFAULT_CACHE_CONFIG);
		
	}

	@Override
	public void setCache(String key, Object value) {
		setCache(key,value,DEFAULT_CACHE_CONFIG);
	}

	@Override
	public boolean hasCacheByConfig(String config) {
		if(_MemeryCache.get(config)!=null)return true;
		return false;
	}
	
	
	public String getFormatterKey(String key){
		String tKey = (String)this.getCache(key, "formatter-value-key");
		if(tKey == null){
			tKey = new StringBuffer().append("__").append(key).toString();
			this.setCache(key, tKey, "formatter-value-key");
		}
		return tKey;
	}
}
