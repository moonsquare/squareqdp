package com.qdevelop.core.standard;

import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.lang.QDevelopException;

/**
 * 在执行数据库操作之前执行的方法的接口
 * @author user
 *
 */
@SuppressWarnings("rawtypes")
public interface IBeforeRun {
	/**
	 * 在执行数据库操作之前,处理请求参数Map
	 * @param param
	 * @return
	 */
	public Map disposeQueryMap(Map param,SecurityUserBean sub,Map<String,Object> session) throws QDevelopException;
	
	/**
	 * 在执行数据库操作之前，处理QueryBean
	 * @param qb
	 * @return
	 */
	public QueryBean disposeQueryBean(QueryBean qb,Map param) throws QDevelopException;
}
