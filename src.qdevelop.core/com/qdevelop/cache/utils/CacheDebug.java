package com.qdevelop.cache.utils;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import com.qdevelop.cache.SecondCache;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;

public class CacheDebug {
	private static SimpleDateFormat _defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String toDate(long timer){
		GregorianCalendar c1 = new GregorianCalendar();
		c1.setTimeInMillis(timer);
		return _defaultFormat.format(c1.getTime());
	}
	
	public void watchCache(String key,String config){
		CasIndexArray casIndexArray = SecondCache.getInstance().getCacheIndexs(key, config);
		if(casIndexArray==null)return;
		for(IndexItem ii : casIndexArray.values()){
			StringBuffer sb = new StringBuffer();
			sb.append("key:").append(ii.getKey()).append(", config:").append(ii.getConfig()).append(", cacheTimes:")
			.append(ii.getCacheTimes()).append(", avgTime:").append(ii.getAvgTime()/1000).append(", createTime:").append(toDate(ii.getCreateTime()))
			.append(", lastTime:").append(toDate(ii.getLastTime())).append(", query:").append(ii.getQuery());
			System.out.println(sb);
		}
	}
	/**
	 * TODO （描述方法的作用） 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new CacheDebug().watchCache("getUserAddress","QD_RESULT");
	}

}
