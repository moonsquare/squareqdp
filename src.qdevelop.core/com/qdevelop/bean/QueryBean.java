package com.qdevelop.bean;


/**
 * 简单数据请求处理<br>
 * 只实现请求内容，请求分页、排序和内容查找功能
 * @author Janson.Gu
 *
 */
public interface QueryBean<V> {
	
	public static final String CONNECT_DEFAULT = "default";
	
	/**
	 * 设置请求类型
	 * @param type
	 */
	public void setQueryType(String type);
	
	/**
	 * 获取请求类型 
	 * @return
	 */
	public String getQueryType();
	/**
	 * 根据不用应用，设置具体请求内容
	 * @param query
	 */
	public void setQuery(Object query);
	/**
	 * 根据不用应用，获取具体请求内容
	 * @return
	 */
	public Object getQuery();
	
	/**
	 * 设置排序请求
	 * @param order
	 */
	public void setOrder(String order);
	
	/**
	 * 获取排序请求
	 * @return
	 */
	public String getOrder();
	
	/**
	 * 设置当前页码
	 * @param nowPage
	 */
	public void setNowPage(int nowPage);
	
	/**
	 * 获取当前页码
	 * @return
	 */
	public int getNowPage();
	
	/**
	 * 设置每页最大请求数
	 * @param maxNum
	 */
	public void setMaxNum(int maxNum);
	
	/**
	 * 获取每页最大请求数
	 * @return
	 */
	public int getMaxNum();
	
	/**
	 * 设置运行后结果操作的方法类名
	 * @param afterRun
	 * @return
	 */
	public void setAfterRun(String afterRun);
	
	/**
	 * 获取运行后结果操作的方法类名
	 * @return
	 */
	public String getAfterRun();
	
	
	/**
	 * 设置运行结果前的方法调用
	 * @param beforeRun
	 */
	public void setBeforeRun(String beforeRun);
	
	/**
	 * 获取运行结果前的方法调用
	 * @return
	 */
	public String getBeforeRun();
	
	
	/**
	 * 是否将结果缓存
	 * @return
	 */
	public boolean isCacheAble();
	
	/**
	 * 是否具有分页请求
	 * @return
	 */
	public boolean isPagination();
	
	/**
	 * 生成Cache所需要的KEY
	 * @return
	 */
	public String getCacheKey();
	
	/**
	 * 设置缓存配置名称
	 * @param cacheConfig
	 */
	public void setCacheConfig(String cacheConfig);
	
	/**
	 * 获取缓存配置名称
	 * @return
	 */
	public String getCacheConfig();
	
	/**
	 * 是否具有数据权限控制
	 * @return
	 */
	public boolean isDataSecurity();
	
	public String getSql();
	
	public void setSql(String sql);
	
	public String toKey();
}
