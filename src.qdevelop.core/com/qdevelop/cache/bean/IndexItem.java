package com.qdevelop.cache.bean;

import java.io.Serializable;

public class IndexItem  implements Serializable{
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 1498098525729914998L;
	String fullKey,
	/*存放的是HashMap query toString()后的值*/query,
	config,pureIndex,stackKey;
	
	public String getPureIndex() {
		return pureIndex;
	}

	Integer rate=1;
	private String itemKey;
//	public boolean isDelete;
	private boolean isCached = true;
	private long lastclicktime,createtime,avgtime = 0;

	public IndexItem(QueueItem queueItem){
		this.fullKey = queueItem.key;
		this.config = queueItem.config;
		this.query = queueItem.query;
		this.pureIndex = queueItem.pureIndex();
		this.stackKey = queueItem.stackKey();
//		isLongCache = this.config.substring(0, 2).equals(CLang.LONG_CACHE_PREFIX);
		createtime = lastclicktime = System.currentTimeMillis();
	}
	
	public IndexItem(IndexItem ii){
		this.fullKey = ii.fullKey;
		this.config = ii.config;
		this.query = ii.query;
		this.pureIndex = ii.pureIndex;
		this.stackKey = ii.stackKey();
		this.createtime = ii.createtime;
		this.lastclicktime = ii.lastclicktime;
		this.isCached = ii.isCached;
		this.itemKey = ii.itemKey;
		this.rate = ii.rate;
		this.avgtime = ii.avgtime;
	}

	public IndexItem(String key,String config,String query,String pureIndex,String stackKey){
		this.fullKey = key;
		this.query = query;
		this.config = config;
		this.pureIndex = pureIndex;
		this.stackKey = stackKey;
//		isLongCache = this.config.substring(0, 2).equals(CLang.LONG_CACHE_PREFIX);
		createtime = lastclicktime = System.currentTimeMillis();
	}

	public void addRate(int diff){
		this.rate = diff+this.rate;
		this.isCached = (diff > 0);
		if(diff > 0){
			long currenttime = System.currentTimeMillis();
			avgtime = (avgtime+currenttime-lastclicktime)/2;
			lastclicktime = currenttime;
		}
	}

	public boolean isCasKey(String key,String config){
		return config==this.config && key.startsWith(this.pureIndex);
	}

//	public boolean isLongCache() {
//		return isLongCache;
//	}
	public int hashCode(){
		return this.toKey().hashCode();
	}

	public boolean equals(Object ki){
		return this.hashCode() == ki.hashCode();
	}

	public boolean equals(String key,String config){
		return key.equals(this.fullKey) && config.equals(this.config);
	}

	public String toString(){
		return new StringBuffer().append(this.fullKey).append("[").append(this.config).append(",").append(this.rate).append(",").append(this.isCached).append("]: ").append(this.query).toString();
	}

	public String toKey(){
		if(itemKey==null)itemKey = new StringBuffer().append(config).append(this.fullKey).toString();
		return itemKey;
	}

	public String stackKey(){
		return stackKey;
	}

	public String getQuery(){
		return this.query;
	}

	public String getKey(){
		return fullKey;
	}

	public String getConfig(){
		return this.config;
	}

	/**
	 * 
	 * TODO 获取缓存被启用的次数 
	 * 
	 * @return
	 */
	public int getCacheTimes(){
		return this.rate;
	}

	/**
	 * 
	 * TODO 获取缓存最后启用时间 
	 * 
	 * @return
	 */
	public long getLastTime(){
		return this.lastclicktime;
	}

	/**
	 * 
	 * TODO 获取缓存平均获取间隔时长 
	 * 
	 * @return
	 */
	public long getAvgTime(){
		return this.avgtime;
	}

	/**
	 * 
	 * TODO 获取缓存创建时间 
	 * 
	 * @return
	 */
	public long getCreateTime(){
		return this.createtime;
	}


	public void setLastclicktime(long lastclicktime) {
		this.lastclicktime = lastclicktime;
	}


	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}

	public void setAvgtime(long avgtime) {
		this.avgtime = avgtime;
	}

	public void setCacheTimes(int cacheTimes){
		this.rate = cacheTimes;
	}

	public boolean isCached() {
		return isCached;
	}

	public void setCached(boolean isCached) {
		this.isCached = isCached;
	}

}
