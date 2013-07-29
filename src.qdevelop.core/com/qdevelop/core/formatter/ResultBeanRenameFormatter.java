/**
 * ResultBeanRenameFormatter.java
 * 
 * Version 1.0
 * 
 * 2012-9-26
 * 
 * Copyright www.wangjiu.com
 */
package com.qdevelop.core.formatter;

import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.AbstractFormatter;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;

/**
 * TODO (描述类的功能)
 * 
 * @author d
 * 2012-9-26
 *
 */
public class ResultBeanRenameFormatter extends AbstractFormatter{
	/**[targetkey]-[renamekey]**/
	String[][] renameFilters;
	private static String SPLIT = "-";
	/**
	 * TODO(描述功能)
	 * 
	 * @see com.qdevelop.core.standard.IResultFormatter#init(com.qdevelop.core.formatter.bean.InitFormatBean)
	 */
	@Override
	public void init(InitFormatBean param) {
		if(param.getConfig("rename-filter")==null)return;
		String[] tmp = param.getConfig("rename-filter").replaceAll(" ", "").split(",|\\|");
		renameFilters = new String[tmp.length][2];
		for(int i=0;i<tmp.length;i++){
			if(tmp[i].indexOf(SPLIT)==-1)continue;
			renameFilters[i][0] = tmp[i].substring(0,tmp[i].indexOf(SPLIT)).toUpperCase();
			renameFilters[i][1] = tmp[i].substring(tmp[i].indexOf(SPLIT)+1);
		}
	}

	/**
	 * TODO(描述功能)
	 * 
	 * @see com.qdevelop.core.standard.IResultFormatter#formatter(java.util.Map, com.qdevelop.core.bean.DBStrutsBean)
	 */
	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts)
			throws QDevelopException {
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void flush(ResultBean rb) {
		if(renameFilters==null)return;
		int size = rb.size();
		for(int i=0;i<size;i++){
			Map data = rb.getResultMap(i);
			for(String[] filter:renameFilters){
				if(filter==null)continue;
				data.put(filter[1], data.get(filter[0]));
				data.remove(filter[0]);
			}
		}
	}
	
	public boolean isQBQuery(){
		return false;
	}

}
