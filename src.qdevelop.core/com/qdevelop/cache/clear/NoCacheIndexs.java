package com.qdevelop.cache.clear;

import java.util.concurrent.ConcurrentHashMap;

public class NoCacheIndexs extends ConcurrentHashMap<String,Long>{
	private static final long serialVersionUID = -6417402445713116846L;
	
	private static NoCacheIndexs _NoCacheIndexs = new NoCacheIndexs();
	public static NoCacheIndexs getInstance(){
		return _NoCacheIndexs;
	}
	
	public boolean hasCacheIndexs(String index){
		if(this.get(index)==null) 
			return true;
		if(System.currentTimeMillis() - this.get(index) > 60000){
			this.remove(index);
			return true;
		}
		return false;
	}
	
	
	public void addNoCacheIndexs(String index){
		this.put(index, System.currentTimeMillis());
	}
	
}
