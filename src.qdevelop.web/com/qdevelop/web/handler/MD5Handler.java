package com.qdevelop.web.handler;

import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.standard.IBeforeRun;
import com.qdevelop.utils.QString;

@SuppressWarnings({"unchecked","rawtypes"})
public class MD5Handler implements IBeforeRun{

	@Override
	public QueryBean disposeQueryBean(QueryBean qb, Map param) {
		return null;
	}

	
	@Override
	public Map disposeQueryMap(Map param,SecurityUserBean sub,Map<String,Object> session) {
		if(param.get("password")!=null && param.get("password").toString().length() != 32){
			param.put("password",QString.get32MD5(param.get("password").toString()));
		}
		return param;
	}

}
