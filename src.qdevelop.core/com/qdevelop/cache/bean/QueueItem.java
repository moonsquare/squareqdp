package com.qdevelop.cache.bean;

import java.io.Serializable;

import com.qdevelop.cache.utils.CacheUtils;

public class QueueItem  implements Serializable{
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 3664335561862929883L;
	public String key,config,query;int oper;
	public QueueItem(String key,String config,String query,int oper){
		this.key = key;
		this.config = config;
		this.query = query;
		this.oper = oper;
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

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getOper() {
		return oper;
	}

	public void setOper(int oper) {
		this.oper = oper;
	}

	public String stackKey(){
		return CacheUtils.stackKey(key,config);// new StringBuffer().append("i_").append(this.config).append(pureIndex()).toString();
	}
	
	public String pureIndex(){
		return CacheUtils.stackIndex(key);
	}
	
	public String toString(){
		return new StringBuffer().append(this.oper).append(":\t").append(this.key).toString();
	}
	
}
