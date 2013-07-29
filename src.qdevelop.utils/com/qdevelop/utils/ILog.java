package com.qdevelop.utils;

import com.qdevelop.core.bean.DBQueryBean;

public interface ILog {
	public static final int INFO_LEVEL = 0;
	public static final int WARN_LEVEL = 1;
	public static final int DEBUG_LEVEL = 2;
	public static final int ERROR_LEVEL = 3;
	
	/**
	 * 
	 * TODO 系统日志之外可以自定义日志输出 
	 * 
	 * @param type	日志类型 例：sql,security,operater,system和其他自定的log配置名
	 * @param level	日志等级
	 * @param logInfo 日志信息
	 * @param query 请求信息，没有时为null 
	 * @param otherInfo 其他额外信息，没有是为null，在type=system时，会给出exception
	 */
	public void log(String type,int level,Object logInfo,DBQueryBean query,Object otherInfo);
}
