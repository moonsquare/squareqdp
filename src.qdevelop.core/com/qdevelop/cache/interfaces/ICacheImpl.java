package com.qdevelop.cache.interfaces;

import java.io.Serializable;

public interface ICacheImpl {
	
	/**
	 * 
	 * TODO 增加缓存 
	 * 
	 * @param key
	 * @param value
	 * @param config
	 * @throws Exception
	 */
	public boolean add(String key,Serializable value,String config,ITacties tatics) throws Exception;
	
	/**
	 * 
	 * TODO 获取缓存
	 * 
	 * @param key
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public Serializable get(String key,String config,ITacties tatics) throws Exception;
	
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
	public boolean update(String key,Serializable value,String config,ITacties tatics) throws Exception;
	
	/**
	 * 
	 * TODO 删除缓存 
	 * 
	 * @param key
	 * @param config
	 * @throws Exception
	 */
	public void remove(String key,String config,ITacties tatics) throws Exception;
	
	
	public void shutdown();
	
}
