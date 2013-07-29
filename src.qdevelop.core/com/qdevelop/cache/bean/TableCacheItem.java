package com.qdevelop.cache.bean;


public class TableCacheItem {
	public TableCacheItem(String key,boolean isTableCache){
		this.key = key;
		this.isTableCache = isTableCache;
	}
//	private Pattern valuePattern;
	boolean isTableCache;
	int rate=1;
	String key;
	String tableName,uniKey,uniValue;
	public boolean isTableCache() {
		return isTableCache;
	}
	public void setTableCache(boolean isTableCache) {
		this.isTableCache = isTableCache;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
//		if(valuePattern == null){
//			valuePattern = Pattern.compile("^.+"+key+"|in|=|\\(|\\)|'| ");
//		}
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getUniKey() {
		return uniKey;
	}
	public void setUniKey(String uniKey) {
		this.uniKey = uniKey;
	}
	public String getUniValue() {
		return uniValue;
	}
	public void setUniValue(String uniValue) {
		this.uniValue = uniValue;
	}
	
	public void addRate(){
		++rate;
	}
	
	
	public String toString(){
		return new StringBuffer().append("[").append(this.isTableCache()).append("]\t").append(this.tableName).append(" - ").append(this.uniKey).append(" - ").append(this.uniValue).toString();
	}
	
}
