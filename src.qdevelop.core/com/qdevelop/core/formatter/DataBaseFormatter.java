package com.qdevelop.core.formatter;

import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.lang.QDevelopException;

public class DataBaseFormatter  implements IResultFormatter{
	String table,connect,targetKey,cacheKey,filds;
	boolean isFormatter;
	
	public DataBaseFormatter clone(){
		try {
			return (DataBaseFormatter)super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void init(InitFormatBean param) {
		
	}

	@Override
	public void initFormatter(DBStrutsBean struts) {
		
	}

	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts)
			throws QDevelopException {
		
	}

	@Override
	public void flush(ResultBean rb) {
		
	}

	@Override
	public boolean isNeedStruts() {
		return false;
	}

}
