package com.qdevelop.core.formatter;

import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.standard.IParamFormatter;
import com.qdevelop.utils.QProperties;

public class ParamFormatter implements IParamFormatter{
	private String key,value;
	
	public void init(String[] params){
		this.key = params[0];
		this.value = params[1].replaceAll("\\$\\{|\\}", "");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map disposeQueryMap(Map param,SecurityUserBean sub,Map<String,Object> session) {
		Object vv;
		if(sub!=null&&value.startsWith("user.")){
			vv = sub.getUserInfo(value.replace("user.", ""));
		}else{
			if(session!=null && session.get(value)!=null){
				vv = session.get(value);
			}else{
				vv = QProperties.getInstance().getJsonValue(value);
			}
		}
		if(vv!=null)param.put(key,vv);
		return param;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public QueryBean disposeQueryBean(QueryBean qb, Map param) {
		return null;
	}

}
