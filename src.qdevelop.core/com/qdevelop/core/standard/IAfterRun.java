package com.qdevelop.core.standard;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.lang.QDevelopException;

/**
 * 在数据库执行操作完之后执行的接口
 * @author user
 *
 */
public interface IAfterRun {
	
	/**执行数据查询后，将要调用的方法名**/
	public static final String METHOD_NAME = "disposeResultBean";
	/**
	 * 执行方法
	 * @param qb 	请求参数
	 * @param rb	产生数据结果
	 */
	@SuppressWarnings("rawtypes")
	public ResultBean disposeResultBean(ResultBean rb,QueryBean queryBean) throws QDevelopException;
}
