package com.qdevelop.web.action;

public class SecurityHelpAction extends QDevelopAction{
	private static final long serialVersionUID = -8278769237339859945L;
	Object result;String param;
	
	public String checkUserRole() throws Exception{
		
		return SUCCESS;
	}

	public String checkSingleLogin() throws Exception{
		
		return SUCCESS;
	}
	
	
	
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
	
}
