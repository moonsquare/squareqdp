package com.qdevelop.web.action;

import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.standard.IFooter;
import com.qdevelop.core.standard.IPagination;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QString;

@SuppressWarnings("rawtypes")
public class AjaxDataRoleManageAction extends QDevelopAction{
	private static final long serialVersionUID = 619661891009373828L;

	private int total;
	List rows;List footer;


	@SuppressWarnings("unchecked")
	public String queryDB() throws QDevelopException{
		Map query = this.getParamMap();
		/**兼容EasyUI Datagrid数据请求格式**/
		if(query.get("rows")!=null){
			query.put("maxNum", query.get("rows"));
			query.remove("rows");
		}
		if(query.get("sort")!=null){
			query.put("order", QString.append(query.get("sort")," ",query.get("order")));
			query.remove("sort");
		}
		DBQueryBean queryBean = CoreFactory.getInstance().getQueryBean(query, this.getUserInfo());
		ResultBean rb = CoreFactory.getInstance().getQueryResult(queryBean.getDataManageSql(),queryBean.getConnect());
		if(rb instanceof IPagination)
			total = ((IPagination)rb).getAllCount();
		rows = rb.getResultList();
		if(rb instanceof IFooter)
			footer = ((IFooter)rb).getFooter();
		return SUCCESS;
	}

	public String updateDB() throws QDevelopException{
		/**兼容多index格式**/
		/**isMultiQuery=true 兼容split param值生成多SQL**/
		total = CoreFactory.getInstance().getQueryUpdate(this.getParamMap(), this.getUserInfo());
		return SUCCESS;
	}

	@SuppressWarnings("unchecked")
	public String queryStruts() throws QDevelopException{
		ResultBean rb = CoreFactory.getInstance().getQueryResult(this.getParamMap(), this.getUserInfo());
		if(rb instanceof IPagination)
			total = ((IPagination)rb).getAllCount();
		rows = rb.getResultList();
		return SUCCESS;
	}


	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List getRows() {
		return rows;
	}
	public void setRows(List rows) {
		this.rows = rows;
	}
	public List getFooter() {
		return footer;
	}
	public void setFooter(List footer) {
		this.footer = footer;
	}

}
