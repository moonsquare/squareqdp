package com.qdevelop.core.formatter;

import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;

public class DBResultOnglFormatter  extends AbstractFormatter{
	@Override
	public void init(InitFormatBean param) {
		try {
			expressionsFormatter = Ognl.parseExpression(param.getConfig("expression").trim());
		} catch (OgnlException e) {
			throw new QDevelopException(e);
		}
	}
	
	private Object expressionsFormatter;

	@Override
	public void formatter(Map<String,Object> data,DBStrutsBean struts) throws QDevelopException {
		try {
			Ognl.getValue(expressionsFormatter,data);
		} catch (OgnlException e) {
			e.printStackTrace();
		}
	}

	public boolean isQBQuery(){
		return false;
	}



}
