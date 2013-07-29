package com.qdevelop.web.action;

import java.util.HashMap;
import java.util.Map;

import com.qdevelop.core.utils.QueryBeanFormatter;

public class AjaxSystemInfoAction extends QDevelopAction{
	private static final long serialVersionUID = 1374866452913778233L;
	Object value;String key;
	
	public String execute() throws Exception{
		Map<String,Object> r = new HashMap<String,Object>();
		new QueryBeanFormatter().executeFormatterParam(key, "value", r, this.getUserInfo(),this.getSession());
		value = r.get("value");
		return SUCCESS;
	}
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}
