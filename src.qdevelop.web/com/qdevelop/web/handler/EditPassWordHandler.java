package com.qdevelop.web.handler;

import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.standard.IBeforeRun;
import com.qdevelop.utils.QString;

@SuppressWarnings({"unchecked","rawtypes"})
public class EditPassWordHandler implements IBeforeRun{

	
	@Override
	public QueryBean disposeQueryBean(QueryBean qb, Map param) {
		return null;
	}

	@Override
	public Map disposeQueryMap(Map param,SecurityUserBean sub,Map<String,Object> session) {
		if(param.get("newPass")!=null){
			param.put("newPass",QString.get32MD5(param.get("newPass").toString()));
		}
		if(param.get("oldPass")!=null){
			param.put("oldPass",QString.get32MD5(param.get("oldPass").toString()));
		}
		return param;
	}

}
