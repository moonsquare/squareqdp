package com.qdevelop.web.action;

import com.qdevelop.lang.QDevelopException;

public class SystemRemoteClearCacheAction extends QDevelopAction{
	private static final long serialVersionUID = 8777948931682972224L;
	private String target;
	
	public String execute() throws QDevelopException {
		
		return SUCCESS;
	}
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
}
