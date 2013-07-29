package com.qdevelop.utils;

import com.qdevelop.utils.cache.EHCache;
import com.qdevelop.utils.cache.ICache;
import com.qdevelop.utils.cache.MapCache;
import com.qdevelop.utils.cache.MemCache;
import com.qdevelop.utils.cache.QIndexCache;

/**
 * 缓存工具类
 * @author Janson.Gu
 *
 */
public class QCache {
	public static ICache mapCache(){
		return MapCache.getInstance();
	}
	
	public static ICache ehCache(){
		return EHCache.getInstance();
	}
	
	public static QIndexCache indexCache(){
		return QIndexCache.getInstance();
	}
	
	public static MemCache memCache(){
		return MemCache.getInstance();
	}
	
//	public static ICache defaultCache(){
//		return MemCache.getInstance();
//	}
	
	
	
	

}
