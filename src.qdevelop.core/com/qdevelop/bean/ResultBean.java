package com.qdevelop.bean;

import java.util.List;
import java.util.Map;

public interface ResultBean{
	public ResultBean clone();
	/**
	 * 
	 * TODO 获取查询结果列表 
	 * 
	 * @return
	 */
	public List<Map<String,Object>> getResultList();

	/**
	 * 
	 * TODO 数据查询过程中，执行数据添加工作 
	 * 
	 * @param i
	 * @return
	 */
	public Map<String,Object> getResultMap(Object i);
	
	public int size();
	
	public void setResultList(List<Map<String,Object>> result);
	
	/**
	 *	后台数据添加专用 
	 * @param data
	 */
	public void addResult(Map<String,Object> data) throws Exception;
	
	public String toString();
	
	
	/**
	 * 数据从数据库加载完成后执行
	 */
	public void flush();
	
	public void clear();
}
