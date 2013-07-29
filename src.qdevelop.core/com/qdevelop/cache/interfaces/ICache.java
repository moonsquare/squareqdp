package com.qdevelop.cache.interfaces;

import java.io.Serializable;

public interface ICache {
	/**
	 * 
	 * TODO 增加缓存 
	 * 
	 * @param key
	 * @param value
	 * @param config
	 * @throws Exception
	 */
	public void add(String key,String query,Serializable value,String config) ;
	
	/**
	 * 
	 * TODO 获取缓存
	 * 
	 * @param key
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public Serializable get(String key,String config);
	
	/**
	 * 
	 * TODO 更新缓存
	 * 
	 * @param key
	 * @param value
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public boolean update(String key,Serializable value,String config) ;
	
	/**
	 * 
	 * TODO 删除缓存 
	 * 
	 * @param key
	 * @param config
	 * @throws Exception
	 */
	public void remove(String key,String config) ;
	
	/**
	 * 
	 * TODO （描述方法的作用） 
	 * 
	 * @param complexArgs
	 * @param remoteTable
	 */
//	public void casRemove(String[] complexArgs,String[] remoteTable);
	
	public void shutdown();
}
