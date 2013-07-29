package com.qdevelop.cache;

import java.io.Serializable;

import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.utils.QLog;

public class CasSaveCache extends Thread{
	String key;
	Serializable value;
	public CasSaveCache(String key,Serializable value){
		this.key = key;
		this.value = value;
	}
	public void run(){
		int i=0;
		for(;i<=10;i++){
			if(MemCachedImpl.getInstance().casUpdate(key, value, MemCacheConfig.ONE_DAY)){
				if(i>1)QLog.getInstance().systemWarn("try sync indexs ["+key+"] "+(i-1));
				value = null;
				break;
			}
			try {
				Thread.sleep(10*i);
			} catch (Exception e) {
			}
		}
		if(i>10)QLog.getInstance().systemError("try sync indexs ["+key+"]");
	}
}
