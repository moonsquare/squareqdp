package com.qdevelop.web.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.core.standard.IFooter;
import com.qdevelop.core.standard.IPagination;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.CMDParser;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QString;
import com.qdevelop.web.utils.ReversalCompare;
import com.qdevelop.web.utils.TransposeData;
import com.qdevelop.web.utils.WebUtils;

@SuppressWarnings("rawtypes")
public class AjaxQueryDBAction extends QDevelopAction{

	private static final long serialVersionUID = 6225991807620637911L;

	int total;
	List rows;
	List footer;

	@SuppressWarnings("unchecked")
	public String queryReversalData() throws QDevelopException{
		Map query = this.getParamMap();
		/**reversalFields=colum1|column2|...**/
		String[] reversalFields = String.valueOf(query.get("reversalFields")).split("\\|");
		/**reversalShows=ID:编号|NAME:名称|...**/
		Map<String,String> reversalShows = new CMDParser(String.valueOf(query.get("reversalShows"))).getParamers();
		initPagination(query);
		String memOrder = null;
		if(query.get("order")!=null){
			memOrder = String.valueOf(query.get("order")).toUpperCase();
			query.remove("order");
		}
		QueryBean qb = CoreFactory.getInstance().getQueryBean(query, this.getUserInfo(),this.getSession());
		ResultBean rb = queryResut(query,qb);
		rows = toReversalData(reversalFields,rb.getResultList(),reversalShows);

		/**order support:	field desc|field asc|field**/
		if(memOrder!=null){
			toReversalSort(rows,memOrder);
		}
		return SUCCESS;
	}



	@SuppressWarnings("unchecked")
	public String queryDB() throws QDevelopException{
		Map query = this.getParamMap();
		try {
			initPagination(query);
			//初始化反转函数
			initTranspose(query);
			if(isLazyLoad){
				total = 0;
				rows=new ArrayList();
				return SUCCESS;
			}
			QueryBean qb = CoreFactory.getInstance().getQueryBean(query, this.getUserInfo(),this.getSession());
			ResultBean rb = queryResut(query,qb);
			rows = rb.getResultList();

			if(rb instanceof IFooter)
				footer = ((IFooter)rb).getFooter();

			if(!isTranspose)return SUCCESS;
			//翻转相关 核心代码
			TransposeData transposeData = new TransposeData();
			rows = transposeData.toTransposeData(transposeShows,rb.getResultList());
			total = rows.size();
		} catch (QDevelopException e) {
			HttpServletRequest request = ServletActionContext.getRequest();
			String uri = WebUtils.getRomveHeaderURI(request,request.getRequestURI());
			SecurityUserBean sub = this.getUserInfo();
			QLog.getInstance().operaterError(QString.append("[",sub.name,"]",uri," param:",this.getParamMap().toString()));
			throw e;
		}
		return SUCCESS;
	}

	/**
	 * 获取结果集
	 * @param query
	 * @return
	 * @throws QDevelopException
	 */
	@SuppressWarnings("unchecked")
	private ResultBean queryResut(Map query,QueryBean qb)throws QDevelopException{
		ResultBean rb = CoreFactory.getInstance().getQueryResult(qb);
		total = this.getLazyPageSize(rb);
		if(isReversal){
			List reuslt = new ArrayList(rb.size());
			for(int i=rb.size()-1;i>-1;i--){
				reuslt.add(rb.getResultMap(i));
			}
			rb.setResultList(reuslt);
		}
		return rb;
	}

	@SuppressWarnings("unchecked")
	public String count() throws QDevelopException{
		Map query = this.getParamMap();
		initPagination(query);
		DBQueryBean _queryBean = CoreFactory.getInstance().getQueryBean(query, this.getUserInfo(),this.getSession());
		Connection conn = null; 
		Map result;
		try {
			conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();
			QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
			result = queryFromDataBaseImp.selectSingle(_queryBean.getSQLByCount(), conn, null);
			if(result!=null)total=Integer.parseInt(String.valueOf(result.get("CN")));
		} catch (QDevelopException e) {
			QLog.getInstance().sqlError(_queryBean);
			throw e;
		}finally{

			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			_queryBean.clearQueryData();//清理数据
			query.clear();
		}

		return SUCCESS;
	}

	public String updateDB() throws QDevelopException{
		/**兼容多index格式**/
		Map query = this.getParamMap();
		try {
			/**isMultiQuery=true 兼容split param值生成多SQL**/
			CoreFactory cf  =CoreFactory.getInstance();
			@SuppressWarnings("unchecked")
			QueryBean qb = cf.getQueryBean(query, this.getUserInfo(),this.getSession());
			total = CoreFactory.getInstance().getQueryUpdate(qb);
		} catch (QDevelopException e) {
			HttpServletRequest request = ServletActionContext.getRequest();
			String uri = WebUtils.getRomveHeaderURI(request,request.getRequestURI());
			SecurityUserBean sub = this.getUserInfo();
			QLog.getInstance().operaterError(QString.append("[",sub.name,"]",uri," param:",this.getParamMap().toString()));
			throw e;
		}
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

	@SuppressWarnings("unchecked")
	public List toReversalData(String[] fields,List data,Map fieldShow){
		List tmpResult = new ArrayList(fields.length);
		int dataSize = data.size()+1;
		for(int i=0;i<fields.length;i++){
			Map dd = new HashMap(dataSize);
			dd.put("COLUMN0", fieldShow.get(fields[i]));
			for(int j=1;j<dataSize;j++){
				dd.put("COLUMN"+j, ((Map)data.get(j-1)).get(fields[i]));
			}
			tmpResult.add(dd);
		}
		return tmpResult;
	}

	@SuppressWarnings("unchecked")
	public List toReversalSort(List data,String sortField){
		ReversalCompare rompare = new ReversalCompare(sortField);
		Collections.sort(data, rompare);
		return data;
	}

	//	public static void main(String[] args) {
	//		List<Map> tt = new ArrayList();
	//		tt.add(new CMDParser("index=janson&int=0&xy=125.63").getParamers());
	//		tt.add(new CMDParser("index=dasd&int=5&xy=225.63").getParamers());
	//		tt.add(new CMDParser("index=afsc&int=2&xy=155.63").getParamers());
	//		tt.add(new CMDParser("index=asdf&int=8&xy=325.63").getParamers());
	//		AjaxQueryDBAction aa = new AjaxQueryDBAction();
	//		aa.toReversalSort(tt,"index desc");
	//		for(Map t:tt){
	//			System.out.println(t);
	//		}
	//		System.out.println("============================");
	//		aa.toReversalSort(tt,"index");
	//		for(Map t:tt){
	//			System.out.println(t);
	//		}System.out.println("============================");
	//		aa.toReversalSort(tt,"int desc");
	//		for(Map t:tt){
	//			System.out.println(t);
	//		}System.out.println("============================");
	//		aa.toReversalSort(tt,"xy");
	//		for(Map t:tt){
	//			System.out.println(t);
	//		}
	//	}
}
