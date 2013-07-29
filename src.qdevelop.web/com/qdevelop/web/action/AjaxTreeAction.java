package com.qdevelop.web.action;

import java.util.List;
import java.util.Map;

import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.core.utils.QueryBeanFormatter;
import com.qdevelop.lang.QDevelopException;

@SuppressWarnings("rawtypes")
public class AjaxTreeAction extends QDevelopAction{
	private static final long serialVersionUID = 9220532030327171903L;
	
	List tree;
	public String execute() throws QDevelopException{
		Map query = this.getParamMap();
		String checkValues =  query.get("checked")==null?null:String.valueOf(query.get("checked"));
		query.remove("checked");
		String filterParentKey =  query.get("filterParentKey")==null?null:String.valueOf(query.get("filterParentKey"));
		query.remove("filterParentKey");
		boolean filterLeaf = Boolean.parseBoolean(String.valueOf(query.get("filterLeaf")));
		query.remove("filterLeaf");
		SecurityUserBean sub = this.getUserInfo();
		new QueryBeanFormatter().executeFormatQueryParam(query, sub,this.getSession());
		TreeBean tb = CoreFactory.getInstance().getQueryTree(query,this.getLoginUserInfo(sub));
		tree = tb.toEasyTree(checkValues,filterParentKey,filterLeaf);
		return SUCCESS;
	}
	
	public String treeGrid() throws QDevelopException{
		Map query = this.getParamMap();
		SecurityUserBean sub = this.getUserInfo();
		new QueryBeanFormatter().executeFormatQueryParam(query, sub,this.getSession());
		TreeBean tb = CoreFactory.getInstance().getQueryTree(query,this.getLoginUserInfo(sub));
		tree = tb.toEasyTreeGrid();
		return SUCCESS;
	}
	
	public List getTree() {
		return tree;
	}
	public void setTree(List tree) {
		this.tree = tree;
	}
	
	
}
