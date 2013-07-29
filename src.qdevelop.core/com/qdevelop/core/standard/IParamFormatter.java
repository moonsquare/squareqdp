package com.qdevelop.core.standard;


public interface IParamFormatter extends IBeforeRun{
	/**
	 * 在请求之前对参数的格式化工作
	 * @param parameter
	 */
	public void init(String[] params);
}
