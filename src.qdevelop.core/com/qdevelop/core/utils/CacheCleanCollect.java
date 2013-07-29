package com.qdevelop.core.utils;

import java.util.HashSet;

public class CacheCleanCollect extends HashSet<String>{
	private static CacheCleanCollect ccc = new CacheCleanCollect();
	public static CacheCleanCollect getInstance(){return ccc;}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1794408775821131168L;

	public void collect(String clearCacheList){
		String[] tmp = clearCacheList.split("\\|");
		for(String t:tmp){
			this.add(t);
		}
	}
}
