package com.qdevelop.cache.tactics;

import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.utils.CLang;


public class SecondTactiesImpl extends AbstractTacties{

	@Override
	public boolean isDeleteImmediately(IndexItem ki) {
		if(isLongCache(ki.getConfig())){
			return false ;
		}else if(isShortCache(ki.getConfig())){
			//最近一个小时点击的内容都需要清理
			return System.currentTimeMillis() - ki.getLastTime() < 86400000;
		}else {
			//平均点击时间小于10分钟的
			return ki.getAvgTime() < 60000  ;
		}
	}

	@Override
	public boolean isUpdateAsynchronous(IndexItem ki) {
		if(isLongCache(ki.getConfig())){
			return ki.getCacheTimes() > 2 || System.currentTimeMillis() - ki.getLastTime() < ONEDAY*10;
		}else if(isShortCache(ki.getConfig())){
			return ki.getCacheTimes() > 10 || ki.getAvgTime() < ONEDAY/2;
		}else {
			return ki.getCacheTimes() > 10 || System.currentTimeMillis() - ki.getLastTime() > ONEDAY/2;
		}
	}

	@Override
	public boolean isRemoveAsynchronous(IndexItem ki) {
		if(isLongCache(ki.getConfig())){
			return false;
		} else if(isShortCache(ki.getConfig()))
			return true;
		else
			return true ;
	}
	
	private boolean isLongCache(String config){
		return config.length()>2 && config.substring(0,2).equals(CLang.LONG_CACHE_PREFIX);
	}
	
	private boolean isShortCache(String config){
		return config.length()>2 && config.substring(0,2).equals(CLang.SHORT_CACHE_PREFIX);
	}

	
}
