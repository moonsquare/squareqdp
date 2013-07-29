package com.qdevelop.cache.interfaces;

import com.qdevelop.cache.bean.IndexItem;

public interface ITacties {
	/**
	 * 
	 * TODO 增加键值对应的请求处理方式 
	 * 
	 * @param key
	 * @param config
	 * @param query
	 */
	public void addKey(String key,String config,String query);
	
	/**
	 * 
	 * TODO 是否是需要立即删除的缓存 
	 * 
	 * @param clickTimes
	 * @param updateTime
	 * @param config
	 * @return
	 */
	public boolean isDeleteImmediately(IndexItem ki);
	
	
	/**
	 * 
	 * TODO 是否是需要异步更新的缓存 
	 * 
	 * @param clickTimes
	 * @param updateTime
	 * @param config
	 * @return
	 */
	public boolean isUpdateAsynchronous(IndexItem ki);
	
	
	/**
	 * 
	 * TODO 是否需要异步删除 
	 * 
	 * @param clickTimes
	 * @param updateTime
	 * @param config
	 * @return
	 */
	public boolean isRemoveAsynchronous(IndexItem ki);
	
	/**
	 * 
	 * TODO 更新索引值 
	 * 
	 * @param key
	 * @param config
	 */
	public void updateKey(String key,String config,String query);
	
	
	public void removeKey(String key,String config);
	
	/**
	 * 增加缓存获取成功的次数
	 * TODO （描述方法的作用） 
	 * 
	 * @param key
	 * @param config
	 */
	public void addCacheRate(String key,String config,String query);
	
	public void delCacheRate(String key,String config,String query);
	
	public String toKey(String key,String config);
	
	/**
	 * 
	 * TODO 调试排队等待执行的信息 
	 *
	 */
	public void printQueue();
	
	/**
	 * 
	 * TODO 在缓存没有及时执行更新之前，新增的缓存操作是不被知晓的，因此在执行级联清理之前需要知晓一下那些已经被进入缓存中了 
	 * 
	 * @param casIndexArray
	 */
//	public void mergeAsynAddCache(CasIndexArray casIndexArray);
	/**
	 * 
	 * TODO 策略 异步执行方法
	 *
	 */
	public void asynRun();
	
}
