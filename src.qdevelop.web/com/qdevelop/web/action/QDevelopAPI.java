package com.qdevelop.web.action;

import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.lang.QDevelopException;

public class QDevelopAPI extends QDevelopAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -71441177525740133L;

	public String selectWithJson() throws QDevelopException{
//		DBResultBean rb = selectResult();
//		StringBuffer sb = new StringBuffer();
//		sb.append("[");
//		java.util.Iterator<Map.Entry> itor;
//		Map.Entry d;
//		for(Map data:rb){
//			sb.append("{");
//			itor = data.entrySet().iterator();
//			while(itor.hasNext()){
//				d = itor.next();
//				sb.append("'").append(d.getKey()).append("':'").append(d.getValue()).append("'");
//			}
//			sb.append("}");
//		}
//		sb.append("]");
		result = selectResult().getResultList();
		return SUCCESS;		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String selectWithXML() throws QDevelopException{
		ResultBean rb = selectResult();
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ROOT>");
		java.util.Iterator<Map.Entry> itor;
		Map.Entry d;
		for(int i=0;i<rb.size();i++){
			Map data = rb.getResultMap(i);
			sb.append("<DATA>");
			itor = data.entrySet().iterator();
			while(itor.hasNext()){
				d = itor.next();
				sb.append("<").append(d.getKey()).append(">").append(d.getValue()).append("<").append(d.getKey()).append(">");
			}
			sb.append("</DATA>");
		}
		sb.append("</ROOT>");
		return SUCCESS;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ResultBean selectResult() throws QDevelopException{
		Map query = this.getParamMap();
		initPagination(query);
		if(query.get("index")==null)query.put("index", index);
		CoreFactory cf  =CoreFactory.getInstance();
		QueryBean qb = cf.getQueryBean(query, this.getUserInfo(),this.getSession());
		return cf.getQueryResult(qb);
	}

	String index;
	Object result;
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}


}
