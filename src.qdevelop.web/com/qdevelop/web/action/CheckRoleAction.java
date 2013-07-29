package com.qdevelop.web.action;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.opensymphony.xwork2.ActionContext;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.web.utils.WebUtils;

public class CheckRoleAction extends QDevelopAction{
	private static final long serialVersionUID = -3985489309518680543L;
	private	static ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	
	private String roleName;
	boolean hasRole;
	public String execute() throws Exception{
		Map<String,Object> session = ActionContext.getContext().getSession();
		SecurityUserBean sub = WebUtils.getSecurityUserBean(session);
		String sessionkey = new StringBuffer().append(sub.loginName).append("_").append(roleName).toString();
		
//		if(JSCacheUtils.isCached(sessionkey))return SUCCESS;
		
		if(session.get(sessionkey)!=null){
			hasRole = (Boolean)session.get(sessionkey);
			return SUCCESS;
		}
		try {
			if(roleName!=null){
				hasRole = checkRole(sub,roleName);
				session.put(sessionkey, hasRole);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}
	
	public boolean checkRole(SecurityUserBean sub,String _roleName) throws ScriptException{
		_roleName = _roleName.replaceAll("\\&+", "&").replaceAll("\\|+", "|");
		Map<String,String> query = new HashMap<String,String>();
		Pattern _check = Pattern.compile(sub.getRoles());
		query.put("index", "quaryRoles");
		TreeBean roleTree = CoreFactory.getInstance().getQueryTree(query);
		String[] roleNames = _roleName.replaceAll("\\(|\\)", "").split("\\&|\\|");
		String[] findRoles = roleTree.getIdByText(roleNames);
		String tmp = new String(_roleName);
		for(int i=0;i<roleNames.length;i++){
			tmp = tmp.replace(roleNames[i], isInThisRole(findRoles[i],_check));
		}
		tmp = tmp.replaceAll("\\&", "&&").replaceAll("\\|", "||");
		return (Boolean)scriptEngine.eval(tmp);
	} 
	
	private String isInThisRole(String rid,Pattern _check){
		return String.valueOf((rid!=null && _check.matcher(rid).find()));
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public boolean isHasRole() {
		return hasRole;
	}
	public void setHasRole(boolean hasRole) {
		this.hasRole = hasRole;
	}

}
