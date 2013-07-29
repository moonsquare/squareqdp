package com.qdevelop.core.sqlmodel;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;

import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.datasource.DataBaseFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;

public class SQLCondition {
	int result=0;
	DBQueryBean qb;
	Connection conn;
	public SQLCondition(DBQueryBean _queryBean,Connection conn){
		qb = _queryBean;
		this.conn = conn;
	}
	
	@SuppressWarnings("unchecked")
	public boolean isExec(){
		Element condition = SQLModelLoader.getInstance().getElementByIndex(qb.sqlIndex).element("condition");
		Iterator<Element> iter = condition.elementIterator();
		while(iter.hasNext()){
			Element con = iter.next();
			if(con.getName().equals("if")){
				String[] args = con.attributeValue("param") == null ? new String[]{} : con.attributeValue("param").split("\\|");
				String sql = con.getText();
				String index = con.attributeValue("exec");
				if(hasData(sql,con.attributeValue("connect"),qb.getQueryData(),args)){
					if(index.equals(qb.sqlIndex)){
						return true;
					}else{
						Map<String,String> query = qb.getQueryData();
						query.put("index", index);
						DBQueryBean  qb = CoreFactory.getInstance().getQueryBean(query);
						result = DataBaseFactory.getInstance().update(qb, conn, null);
						break;
					}
				}
			}else{
				Map<String,String> query = qb.getQueryData();
				query.put("index", con.attributeValue("exec"));
				DBQueryBean  qb = CoreFactory.getInstance().getQueryBean(query);
				result = DataBaseFactory.getInstance().update(qb, conn, null);
				break;
			}
		}
		return false;
	}
	
	private boolean hasData(String sql,String connect,Map<String,String> query,String[] args){
		String _sql = SQLModelParser.getInstance().parserQuerySQL(sql, args, query, true, null);
		return QueryFromDataBaseImp.getInstance().selectSingle(_sql, conn, null) != null;
	}
	
	public int getResult(){
		return result;
	}
}
