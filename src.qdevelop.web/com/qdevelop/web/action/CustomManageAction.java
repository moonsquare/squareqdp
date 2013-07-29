package com.qdevelop.web.action;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.qdevelop.lang.QDevelopException;

public class CustomManageAction extends QDevelopAction{
	
	private static final long serialVersionUID = 1104640372121410108L;
	
	String key;
	int maxAge;
	Object value;
	int i=0;
	
	public String execute() {
		return SUCCESS;
	}

	public String customSetCookie()throws QDevelopException{
		Cookie co = new Cookie(key,getValue()==null?null:String.valueOf(value));
		co.setMaxAge(getMaxAge());
		co.setPath("/");
		ServletActionContext.getResponse().addCookie(co);
		return SUCCESS;
	}
	
	public String customGetCookie()throws QDevelopException{
		Cookie cookies[] = ServletActionContext.getRequest().getCookies();
		for(Cookie co : cookies){
			if(co.getName().equals(key)){
				value = co.getValue();
				break;
			}
		}
		return SUCCESS;
	}
	
	public String customSetSession()throws QDevelopException{
		ActionContext.getContext().getSession().put(key, getValue());
		return SUCCESS;
	}
	
	public String customGetSession()throws QDevelopException{
		value = ActionContext.getContext().getSession().get(key);
		return SUCCESS;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value==null||value.equals("null")?null:value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getMaxAge() {
		return maxAge>0?maxAge:(60*60*24*365);
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
	
}
