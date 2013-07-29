package com.qdevelop.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;

import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.datasource.DataBaseFactory;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QJson;

public class AjaxMultiUpdateAction extends QDevelopAction{

	private static final long serialVersionUID = -8487336551224346981L;
	int total;String parameter;boolean lowerCase;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String execute() throws QDevelopException{
		CoreFactory cf  = CoreFactory.getInstance();
		if(parameter==null||parameter.length()<1)return SUCCESS;
		Object[] objects =  QJson.getObjectArrayFromJson(parameter);
		DBQueryBean[] querys = new DBQueryBean[objects.length];
		int idx=0;
		boolean isSameDatabase = true;
		for(Object obj:objects){
			JSONObject jsonObject = JSONObject.fromObject(obj.toString());   
			Map map = new HashMap();   
			for(Iterator iter = jsonObject.keys(); iter.hasNext();){   
				String key = (String)iter.next();   
				map.put(lowerCase?key.toLowerCase():key, String.valueOf(jsonObject.get(key)));   
			} 
			querys[idx++] = cf.getQueryBean(map, this.getUserInfo(),this.getSession());
			/*判定是否是同一库，再同一库时具有事务功能*/
			if(idx>1&&querys[idx-1].connect.equals(querys[idx-2].connect))isSameDatabase = false;
		}
		if(isSameDatabase){
			ArrayList<String> sqls = new ArrayList<String>();
			for(int i=0;i<querys.length;i++){
				String[] sql_s = querys[i].getQuery();
				for(String _s:sql_s){
					sqls.add(_s);
				}
			}
			total = DataBaseFactory.getInstance().update(querys[0].connect,sqls.toArray(new String[]{}),null);
		}else{
			total = DataBaseFactory.getInstance().update(querys);
		}
		return SUCCESS;
	}
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public boolean isLowerCase() {
		return lowerCase;
	}

	public void setLowerCase(boolean lowerCase) {
		this.lowerCase = lowerCase;
	}
	
}
