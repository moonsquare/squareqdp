package com.qdevelop.web.action;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.qdevelop.cache.SecondCache;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.lang.QDevelopException;

public class QCacheManageAction extends QDevelopAction{

	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 3897678766847603461L;
	String key,config;
	int oper;
	HashMap<String,Object>[] result;
	
	@SuppressWarnings("unchecked")
	public void list() throws QDevelopException{
		CasIndexArray casIndexArray = SecondCache.getInstance().getCacheIndexs(key, config);
		if(casIndexArray !=null){
			result = new HashMap[casIndexArray.size()];
			int idx=0;
			for(IndexItem ii : casIndexArray.values()){
				result[idx++] = new HashMap<String,Object>();
				result[idx++].put("key", ii.getKey());
				result[idx++].put("config", ii.getConfig());
				result[idx++].put("cacheTimes", ii.getCacheTimes());
				result[idx++].put("avgTime", ii.getAvgTime());
				result[idx++].put("createTime", toDate(ii.getCreateTime()));
				result[idx++].put("lastTime", toDate(ii.getLastTime()));
				result[idx++].put("query", ii.getQuery());
			}
		}
	}
	
	public void operate() throws QDevelopException{
		
	}
	
	
	private static SimpleDateFormat _defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String toDate(long timer){
		GregorianCalendar c1 = new GregorianCalendar();
		c1.setTimeInMillis(timer);
		return _defaultFormat.format(c1.getTime());
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public int getOper() {
		return oper;
	}

	public void setOper(int oper) {
		this.oper = oper;
	}

	public HashMap<String, Object>[] getResult() {
		return result;
	}

	public void setResult(HashMap<String, Object>[] result) {
		this.result = result;
	}
}
