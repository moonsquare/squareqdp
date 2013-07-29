package com.qdevelop.utils.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.ObjectExistsException;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QProperties;

public class TableDataCache {
	byte[] lock = new byte[0];
	private static TableDataCache _DirectCache = new TableDataCache();
	public static TableDataCache getInstance(){return _DirectCache;}
	private HashMap<String,String> table2Key = new HashMap<String,String>();
	private static Pattern tablePattern = Pattern.compile("^.+?INTO|^UPDATE|LOW_PRIORITY| WHERE.+?$| SET.+?$| VALUE.+?$|\\(.+?\\)|^.+?FROM|`");
	/**
	 * Table Name All Be toUpperCase
	 * @param table
	 * @param key
	 */
	public void setTableKey(String table,String key){
		table2Key.put(table, key.toUpperCase());
	}

	public void clearCacheDataByTable(String table,String id){
		if(table2Key.get(table)==null)return;
		CacheManager manager = EHCache.getInstance().getCacheManager();
		if(manager.getCache(table)==null)return;
		manager.getCache(table).remove(id);
		if(QProperties.isDebug)System.out.println(append("clear DirectCache table:",table,"Data:",id));
	}

	public void clearCacheDataBySQL(String sql){
		String tmp  = sql.toUpperCase();
		if(!tmp.startsWith("UPDATE")&&!tmp.startsWith("DELETE"))return; 
		String table =tablePattern.matcher(tmp).replaceAll("").trim(); 
		if(table2Key.get(table)==null)return;
		String key = table2Key.get(table);
		if(tmp.indexOf(append(" ",key," "))==-1&&tmp.indexOf(append(" ",key,"="))==-1)return;
		String id = tmp.replaceAll(append("^.+ ",key," ?="), "").trim();
		if(id.indexOf(" ")>-1)id = id.substring(0,id.indexOf(" "));
		clearCacheDataByTable(table,id);
	}

	private String append(String ... ss ){
		StringBuffer sb =  new StringBuffer();
		for(String s:ss){
			sb.append(s);
		}
		return sb.toString();
	}

	public boolean checkCacheAndCreate(String config){
		try {
			CacheManager manager = EHCache.getInstance().getCacheManager();
			if(manager.getCache(config)!=null)return false;
			synchronized(lock){
				Cache cache = new Cache(config, 1000000, false, true, 21600, 21600); 
				manager.addCache(cache);
				return true;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ObjectExistsException e) {
			e.printStackTrace();
		} catch (CacheException e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public Map<String,Object> getCache(String key,String config) throws QDevelopException{
		try {
			CacheManager manager = EHCache.getInstance().getCacheManager();
			Element element = manager.getCache(config).get(key);
			return element ==null?null:(Map<String,Object>)element.getValue();
		} catch (IllegalStateException e) {
			throw new QDevelopException(e);
		} catch (CacheException e) {
			throw new QDevelopException(e);
		}
	}

	public void setCache(String key,HashMap<String,Object> data,String config){
		CacheManager manager = EHCache.getInstance().getCacheManager();
		manager.getCache(config).put(new Element(key,data));
	}

	//	public static void main(String[] args) {
	//		long time = System.currentTimeMillis();
	//		for(int i=0;i<100000;i++)
	//		TableDataCache.getInstance().clearCacheDataBySQL("update table set aaa = fasd where id=123123");
	//		System.out.print((System.currentTimeMillis()-time));
	//	}
}
