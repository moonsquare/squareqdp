package com.qdevelop.web.utils;

import java.util.Map;
import java.util.Map.Entry;

import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.AbstractFormatter;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;

public class HtmlFormatter extends AbstractFormatter{

	@Override
	public void init(InitFormatBean param) {
		
	}

	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts)
			throws QDevelopException {
		java.util.Iterator<Entry<String, Object>> itor = data.entrySet().iterator();
		while(itor.hasNext()){
			Entry<String,Object> elem = itor.next();
			String v = String.valueOf(elem.getValue());
			if(v.indexOf(">")>-1)v = v.replaceAll("<","&lt;");
			if(v.indexOf("<")>-1)v = v.replaceAll(">","&gt;");
			if(v.indexOf("\"")>-1)v = v.replaceAll("\"","&quot;");
			data.put(elem.getKey(), v);
		}
	}

}
