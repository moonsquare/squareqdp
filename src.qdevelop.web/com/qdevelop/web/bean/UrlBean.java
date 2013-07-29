package com.qdevelop.web.bean;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings({"rawtypes"})
public class UrlBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5421119507048623419L;
	@SuppressWarnings("unused")
	private String url,name,id,pid,urlRole;
	private Pattern roles; 
	
	public UrlBean(Map data){
		url = String.valueOf(data.get("LINKURL"));
		name = String.valueOf(data.get("MENUNAME"));
		urlRole = (String)data.get("SID");
		roles = data.get("SID")==null?null:Pattern.compile(String.valueOf(data.get("SID")).replaceAll(";", "|"));
		id = String.valueOf(data.get("ID"));
		pid = String.valueOf(data.get("PID"));
	}
	public String getName(){return name;}
	public String getPid(){return pid;}
	public String getId(){return id;}
	
	public boolean hasRole(String role){
		if(role==null)return false;
		if(roles==null || "0".equals(role)|| "ALL".equals(urlRole) || role.replaceAll("^0\\||\\|0\\||\\|0$", "").length() != role.length())return true;
		return roles.matcher(role).find();
	} 
	
	public String getUrlRole(){
		return urlRole;
	}
}
