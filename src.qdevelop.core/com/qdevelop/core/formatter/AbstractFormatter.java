package com.qdevelop.core.formatter;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.standard.IResultFormatter;

/**
 * formatter抽象类，有些公共的方法在集成写的时候不需要定义
 * @author Janson.Gu
 *
 */
public abstract class AbstractFormatter  implements IResultFormatter{
	public IResultFormatter clone(){
		try {
			return (IResultFormatter)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public void flush(ResultBean rb) {
		
	}
	@Override
	public boolean isNeedStruts() {
		return false;
	}
	
	public void initFormatter(DBStrutsBean struts){
		
	}
	
	public boolean isQBQuery(){
		return true;
	}
	
}
