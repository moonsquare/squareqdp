package com.qdevelop.cache;

import java.util.HashMap;
import java.util.regex.Pattern;

import com.qdevelop.cache.bean.CacheStatusBean;

public class CacheStatus extends HashMap<String,CacheStatusBean>{
	private static final long serialVersionUID = -7960529961966757461L;
	public static Pattern clear = Pattern.compile("\\(.+\\)|@.+$");
	private static CacheStatus _CacheStatus = new CacheStatus();
	public static CacheStatus getInstance(){
		return _CacheStatus;
	}

	public void cached(String index,String config){
		if(index == null)return;
		index = clear.matcher(index).replaceAll("");
		String key = new StringBuffer().append(index).append(config).toString();
		CacheStatusBean csb = this.get(key);
		if(csb == null){
			csb = new CacheStatusBean();
			csb.index = index;
			csb.config = config;
			this.put(key, csb);
		}
		csb.cacheTimer++;
	}

	public void add(String index,String config){
		if(index == null)return;
		index = clear.matcher(index).replaceAll("");
		String key = new StringBuffer().append(index).append(config).toString();
		CacheStatusBean csb = this.get(key);
		if(csb == null){
			csb = new CacheStatusBean();
			csb.index = index;
			csb.config = config;
			this.put(key, csb);
		}
		csb.addTimer++;
	}

	public void delete(String index,String config){
		if(index == null)return;
		index = clear.matcher(index).replaceAll("");
		String key = new StringBuffer().append(index).append(config).toString();
		CacheStatusBean csb = this.get(key);
		if(csb == null){
			csb = new CacheStatusBean();
			csb.index = index;
			csb.config = config;
			this.put(key, csb);
		}
		csb.deleteTimer++;
	}

	@SuppressWarnings("unused")
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(CacheStatusBean csb : this.values()){

		}
		return sb.toString();
	}
}
