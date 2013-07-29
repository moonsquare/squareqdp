package com.qdevelop.core.formatter.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;

public class TableResultBean extends HashMap<String,Map<String,Object>> implements Serializable,ResultBean,Cloneable{
	public TableResultBean clone(){
		return (TableResultBean) super.clone();
	}
	
	private static final long serialVersionUID = -7100153666212802873L;
	private String priKey;//,cacheConfig;
	
	boolean isCache,hasData;
	public TableResultBean(String priKey,String cacheConfig,boolean isCache){
		this.priKey = priKey;
//		this.cacheConfig = cacheConfig;
		this.isCache = isCache;
	}
	@Override
	public List<Map<String,Object>> getResultList() {
		return null;
	}

	@Override
	public Map<String, Object> getResultMap(Object i) {
		return this.get((Integer)i);
	}

	@Override
	public int size() {
		return 0;
	}
	
	public boolean hasData(){
		return hasData;
	}

	@Override
	public void addResult(Map<String, Object> data) throws Exception {
		String key = String.valueOf(data.get(priKey));
		this.put(key, data);
		hasData = true;
//		if(isCache)
//			TableDataCache.getInstance().setCache(key, (HashMap<String,Object>)data, cacheConfig);

	}

	@Override
	public void flush() {

	}

	@Override
	public void clear() {
		super.clear();
	}
	@Override
	public void setResultList(List<Map<String, Object>> result) {
		
	}

}
