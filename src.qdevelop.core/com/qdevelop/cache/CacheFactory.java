package com.qdevelop.cache;

import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.core.schedule.QScheduleFactory;


public class CacheFactory {
	
	/**
	 * 二级缓存
	 * @return
	 */
	public static SecondCache secondCache(){
		return SecondCache.getInstance();
	}

	/**
	 * 一级缓存
	 * @return
	 */
	public static FirstCache firstCache(){
		return FirstCache.getInstance();
	}

	public static void shutdown(){
		SecondCache.getInstance().shutdown();
		FirstCache.getInstance().shutdown();
		QScheduleFactory.getInstance().shutdown();
		MemCachedImpl.getInstance().shutdown();
	}

}
