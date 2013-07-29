package com.qdevelop.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SecurityUserBean implements Serializable{
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 2010436739451775573L;

	public SecurityUserBean(){

	}

	public SecurityUserBean(String rid){
		this.rid = rid;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SecurityUserBean(Map data) {
		loginName =String.valueOf(data.get("LOGINNAME"));
		name = (String)data.get("NAME");
		password = (String)data.get("PASSWORD");
		rid = String.valueOf(data.get("RID"));
		selfRid = String.valueOf(data.get("RID"));
		gid = String.valueOf(data.get("GID"));
		info = (String)data.get("INFO");
		id = data.get("ID")!=null?String.valueOf(data.get("ID")):String.valueOf(data.get("UID"));
		userType = data.get("USERTYPE")==null?0:Integer.parseInt(String.valueOf(data.get("USERTYPE")));
		marker = data.get("MARKER")==null?0:Integer.parseInt(String.valueOf(data.get("MARKER")));
		all = (HashMap<String, Object>) data;
	}

	public String id;
	public String loginName;
	public String name;
	public String password;
	public String rid;
	public String selfRid;
	public String gid;
	public String info;
	public int marker;
	public int userType;
	public String userIP;
	public String loginTime;
	public HashMap<String,Object> all;

	public Object getUserInfo(String key){
		if(all==null || all.get(key.toUpperCase())==null)return null;
		return all.get(key.toUpperCase());
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRid() {
		return rid;
	}
	public void setRid(String rid) {
		this.rid = rid;
		all.put("RID", rid);
	}
	public String getGid() {
		if(gid==null||gid.trim().length()==0)return null;
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public int getMarker() {
		return marker;
	}
	public void setMarker(int marker) {
		this.marker = marker;
	}
	/**
	 * 用户拥有的全部角色ID
	 * @return
	 */
	public String getRoles(){
		if(this.rid!=null){
			this.rid = this.rid.replaceAll("^;|;$| ", "");
		}
		return this.rid;
	}

	public String getSelfRid() {
		return selfRid;
	}

	public void setSelfRid(String selfRid) {
		this.selfRid = selfRid;
		all.put("SELFRID", selfRid);
	}

	public String getSelfRole(){
		
		return selfRid;
	}
	public void addRoleId(String rids){
		this.rid += ";"+rids;
	}

	public String toCacheKey(){
		return getRoles();
	}

	public boolean isFindRole(String rid){
		return false;
	}

	public String getUserIP() {
		return userIP;
	}

	public void setUserIP(String userIP) {
		this.userIP = userIP;
		all.put("USERIP", userIP);
	}

	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
		all.put("LOGINTIME", loginTime);
	}


}
