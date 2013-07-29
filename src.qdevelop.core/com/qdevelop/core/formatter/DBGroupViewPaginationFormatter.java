package com.qdevelop.core.formatter;

import java.util.HashMap;
import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.sqlmodel.SQLModelParser;
import com.qdevelop.core.standard.IPaginationFormatter;

public class DBGroupViewPaginationFormatter implements IPaginationFormatter {
	String sql,orderParam,groupField,cacheConfig,orderStr,connect;
	String[] param;
	
	public DBGroupViewPaginationFormatter(String sql,String orderParam,String groupField,String cacheConfig,String connect,String[] param){
		this.sql = sql;
		this.orderParam = orderParam;
		this.groupField = groupField;
		this.cacheConfig = cacheConfig;
		this.param = param;
		this.connect = connect;
		orderStr = sql.substring(sql.replace("ORDER", "order").indexOf("order"));
		orderStr = orderStr.substring(0,orderStr.indexOf("]")+1);
	}
	
	private static String[] removeParam = new String[]{"index","page","maxNum","order"};
	@SuppressWarnings({"rawtypes","unchecked"})
	@Override
	public Map formatterPagination(Map query) {
		Map data = new HashMap(query);
		for(String k : removeParam){
			data.remove(k);
		}
		boolean isOrder = query.get("order")==null?false:true;
		if(isOrder)
			data.put(orderParam, query.get("order"));
		String sql = SQLModelParser.getInstance().parserQuerySQL(isOrder?this.sql:this.sql.replace(orderStr, ""), param, data,true,null);
		DBQueryBean qb = new DBQueryBean();
		qb.setConnect(connect);
		qb.setSql(sql);
		qb.setNowPage(Integer.parseInt(String.valueOf(query.get("page"))));
		qb.setMaxNum(Integer.parseInt(String.valueOf(query.get("maxNum"))));
		ResultBean rb  = CoreFactory.getInstance().getQueryResult(qb);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<rb.size();i++){
			if(i>0)sb.append("|");
			sb.append(rb.getResultMap(i).get(groupField.toUpperCase()));
		}
		
		query.put("page", "0");
		query.put("maxNum", "-1");
		query.put(groupField, sb.toString());
		return query;
	}
	
}
