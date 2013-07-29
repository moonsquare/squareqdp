package com.qdevelop.utils.cache;

public interface ICache {
	/**默认缓存配置**/
	public static final String DEFAULT_CACHE_CONFIG = "default"; 
	/**初始化缓存**/
	public void initCache();
	/**缓存是否存在**/
	public boolean isCached(String key);
	/**设置缓存**/
	public void setCache(String key,Object value);
	/**获取缓存**/
	public Object getCache(String key);
	/**移除缓存**/
	public void removeCache(String key);
	/**清除所有缓存**/
	public void removeAllCache();
	
	public boolean isCached(String key,String config);
	public void setCache(String key,Object value,String config);
	public Object getCache(String key,String config);
	public void removeCache(String key,String config);
	public void removeAllCache(String config);
	
	public boolean hasCacheByConfig(String config);

}
