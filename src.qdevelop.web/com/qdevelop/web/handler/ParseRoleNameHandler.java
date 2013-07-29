package com.qdevelop.web.handler;

import java.util.HashMap;
import java.util.Map;

import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.lang.QDevelopException;

@SuppressWarnings({ "unchecked","rawtypes" })
public class ParseRoleNameHandler {
	
	public static void addNameByRID(Object rids,Map data){
		if(rids != null){
			try {
				Map tmp = new HashMap();
				tmp.put("index", "quaryRoles");
				TreeBean tb = CoreFactory.getInstance().getQueryTree(tmp);
				if(tb!=null)
					data.put("ROLENAME", tb.getTextById(String.valueOf(rids).split(";|\\|")));
			} catch (QDevelopException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void addNameByRID(Object rid1,Object rid2,Map data){
		if(rid1 != null && rid2!=null){
			try {
				Map tmp = new HashMap();
				tmp.put("index", "quaryRoles");
				TreeBean tb = CoreFactory.getInstance().getQueryTree(tmp);
				if(tb!=null){
					data.put("ROLENAME1", tb.getTextById(String.valueOf(rid1).split(";|\\|")));
					data.put("ROLENAME2", tb.getTextById(String.valueOf(rid2).split(";|\\|")));
				}
			} catch (QDevelopException e) {
				e.printStackTrace();
			}
		}
	}
}
