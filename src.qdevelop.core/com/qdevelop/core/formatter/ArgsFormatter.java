package com.qdevelop.core.formatter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.standard.IParamFormatter;

@SuppressWarnings("rawtypes")
public class ArgsFormatter implements IParamFormatter{
	/**
	 * type in ["INCLUDE","EXCLUDE"]
	 */
	private String type;
	private String[] args;
	@SuppressWarnings("unchecked")
	@Override
	public Map disposeQueryMap(Map param, SecurityUserBean sub,	Map<String, Object> session) {
		if(args!=null){
			java.util.Iterator<String> itor = ((HashMap)((HashMap)param).clone()).keySet().iterator();
			while(itor.hasNext()){
				String key = itor.next();
				if("INCLUDE".equals(type)&& !ArrayUtils.contains(DBQueryBean.clearParam,key) && !ArrayUtils.contains(args, key)){
					param.remove(key);
				}else if("EXCLUDE".equals(type) && ArrayUtils.contains(args, key)){
					param.remove(key);
				}
				
			}
		}
		return param;
	}

	@Override
	public QueryBean disposeQueryBean(QueryBean qb, Map param) {
		return qb;
	}

	@Override
	public void init(String[] params) {
		if(params!=null && params.length>0){
			args=params[0].split(",|\\|");
			if(params.length>1)type=params[1].toUpperCase();
			else type="INCLUDE";
		}
	}
}
