package com.qdevelop.utils.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;

/**
 * 索引缓存<br>
 * 将缓存的KEY做索引，可以实现模糊删除缓存
 * @author Janson.Gu
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class QIndexCache implements ICache{
	byte[] lock = new byte[0];
	private static QIndexCache _QIndexCache = new QIndexCache();
	private QDevelopCacheIndex qdevelopCacheIndex = new QDevelopCacheIndex();
	
	public static QIndexCache getInstance(){
		return _QIndexCache;
	}

	private CacheManager getCacheManager(){
		return EHCache.getInstance().getCacheManager();
	}

	@Override
	public Object getCache(String key, String config) {
		CacheManager manager = getCacheManager();
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
		qdevelopCacheIndex.clear();
		qdevelopCacheIndex = new QDevelopCacheIndex();
	}
	

	@Override
	public boolean isCached(String key, String config){
		CacheManager manager = getCacheManager();
		if(manager == null)return false;
		Cache cache = manager.getCache(config);
		if(cache == null)return false;
		try {
			if(cache.get(key) == null){
				qdevelopCacheIndex.removeIndex(key, config);
				return false;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (CacheException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean isCached(String key) {
		return isCached(key,DEFAULT_CACHE_CONFIG);
	}

	@Override
	public void removeAllCache(String config) {
		if(config == null)return;
		CacheManager manager = getCacheManager();
		if(manager!=null && manager.getCache(config)!=null){
			try {
				manager.getCache(config).removeAll();
				qdevelopCacheIndex.removeIndex(config);
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
		synchronized(lock){
			if(key==null || config == null)return;
			CacheManager manager = getCacheManager();
			if(manager!=null && manager.cacheExists(config)){
				try {
					if(key.indexOf("@")>-1){
						key = key.replaceAll("@.+?\\|", "|").replaceAll("@.+?$|\\^.+\\^", "");
					}
					List<String> indexKeys = qdevelopCacheIndex.findKeys(config, key.split(";|\\|"));
					Cache cache = manager.getCache(config);
					for(String k:indexKeys){
						if(QProperties.isDebug)System.out.println(QString.append("===>Remove Index Cached ",k," (",config,")"));
						cache.remove(k);
					}
					indexKeys.clear();
				} catch (IllegalStateException e) {
					QLog.getInstance().systemError(e.getMessage(), e);
					e.printStackTrace();
				} 
			}
		}
	}

	@Override
	public void removeCache(String param) {
		synchronized(lock){
			CacheManager manager = getCacheManager();
			if(manager==null || param==null)return;
			try {
				Map<String,List<String>> cacheIndex = qdevelopCacheIndex.findKeys(param);
				Iterator<String> itor = cacheIndex.keySet().iterator();
				String config;
				while(itor.hasNext()){
					config = itor.next();
					if(manager.cacheExists(config)){
						List<String> indexKeys = cacheIndex.get(config);
						Cache cache = manager.getCache(config);
						for(String k:indexKeys){
							if(QProperties.isDebug)System.out.println(QString.append("===>Remove Index Cached ",k," (",config,")"));
							cache.remove(k);
						}
						indexKeys.clear();
					}
				}
				cacheIndex.clear();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setCache(String key, Object value, String config) {
		synchronized(lock){
			CacheManager manager = getCacheManager();
			if(manager==null)return;
			try {
				Cache cache = manager.getCache(config);
				if(cache == null){
					return;
				}
				if(value instanceof Serializable)
					cache.put(new Element(key, (Serializable)value)); 
				else 
					cache.put(new Element(key, new SerializCacheBean(value)));
				qdevelopCacheIndex.addCacheIndex(key, config);
			} catch (Exception e) {
				QLog.getInstance().systemError(e.getMessage(), e);
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void setCache(String key, Object value) {
		setCache(key,value,DEFAULT_CACHE_CONFIG);
	}
	@Override
	public boolean hasCacheByConfig(String config) {
		if(getCacheManager().getCache(config)!=null)return true;
		return false;
	}

	/**
	 * 用来自销毁索引对象的 
	 */
	public void selfDestroy(){
		List<String> tmp;
		String config;
		Cache cache;
		CacheManager manager = getCacheManager();
		if(manager==null)return;
		Iterator<String> itor = qdevelopCacheIndex.keySet().iterator();
		List<String> vvv = new ArrayList();
		while(itor.hasNext()){
			vvv.add(itor.next());
		}
		try {
			for(String key:vvv){
				config = key.substring(key.indexOf("@")+1);
				cache = manager.getCache(config);	
				tmp = qdevelopCacheIndex.get(key);
				if(cache == null || tmp == null || tmp.size() == 0){
					qdevelopCacheIndex.remove(key);
				}else{
					String[] tmpList = tmp.toArray(new String[]{});
					for(String tmpKey : tmpList){
						if(cache.get(tmpKey)==null)tmp.remove(tmpKey);
					}
					if(tmp.size()==0)
						qdevelopCacheIndex.remove(key);
					else
						qdevelopCacheIndex.put(key, tmp);
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (CacheException e) {
			e.printStackTrace();
		}
		vvv.clear();
	}

	public void printCacheIndex(){
		System.out.print(qdevelopCacheIndex.toString());
	} 
}
