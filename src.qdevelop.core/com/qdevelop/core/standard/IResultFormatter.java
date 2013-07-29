package com.qdevelop.core.standard;

import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;

/**
 * 请求结果初始化接口
 * @author Janson.Gu
 *
 */
public interface IResultFormatter extends Cloneable{
	
	/**
	 * 第一次加载formatter类时，从配置中将参数传入类使用，只运行一次
	 * @param param
	 */
	public void init(InitFormatBean param);
	
	public IResultFormatter clone();
	
	/**
	 * 初始化时，将请求结果结构带入 ； isNeedStruts()==false 时 struts为null<br>
	 * 每次调用该执行类时均调用该方法
	 * @param sbs
	 */
	public void initFormatter(DBStrutsBean struts);
	
	/**
	 * 请求数据时，对数据结构进行格式化,isNeedStruts()==false时，struts=null；
	 * @param data
	 * @throws QDevelopException
	 */
	public void formatter(Map<String,Object> data,DBStrutsBean struts) throws QDevelopException;
	
	/**
	 * 请求结束后回调请求结果
	 * @param rb
	 */
	public void flush(ResultBean rb);
	
	/**
	 * 控制是否需要请求的表结构
	 * @return
	 */
	public boolean isNeedStruts();
	
}
