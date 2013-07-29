package com.qdevelop.core.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QCache;
import com.qdevelop.utils.QLog;

@SuppressWarnings("unused")
public class DatabaseComplex {
	public static DatabaseComplex getInstance(){return new DatabaseComplex();}
	
	public int complexUpdate(DBQueryBean select,String updateIndex) throws QDevelopException{
		Connection conn = null; 
		DBQueryBean update = new DBQueryBean();
		try {
			conn = ConnectFactory.getInstance(select.getConnect()).getConnection();
			IResultFormatter[] _formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(select.getSqlIndex());
			if(select.isPagination()){
				
			}
			Element sqlModel = SQLModelLoader.getInstance().getElementByIndex(updateIndex);
			if(sqlModel==null) {
				QLog.getInstance().systemError(append("SQL Model Config [",updateIndex,"] Not Found!!"));
				throw new QDevelopException(append("SQL Model Config [",updateIndex,"] Not Found!!"));
			}
//			if(select.connect != update.connect)conn = ConnectFactory.getInstance(update.getConnect()).getConnection();
			
		}catch (QDevelopException e) {
			QLog.getInstance().sqlError(select);
			QLog.getInstance().sqlError(update);
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	private String parserSQL(Element sqlModel,Map<String,Object> data,DBQueryBean update){
		
		return null;
	}
	
	/**
	 * 指定表名，连接名和ID值获取一级缓存数据
	 * @param tableName
	 * @param connectConfig
	 * @param ids
	 * @return
	 */
	public List<Map<String,Object>> getResultByKey(String tableName,String connectConfig,String ... ids){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		HashSet<String> set = new HashSet<String>();
		for(String id:ids){
			String key = append(tableName,"_",ids);
//			Map cacheData = (Map)QCache.defaultCache().getCache(id, tableName);
//			if(cacheData==null){
//				set.add(id);
//			}else{
//				
//			}
		}
		return null;
	}
	
	private String append(Object ... s){
		StringBuffer sb = new StringBuffer();
		for(Object _s:s)sb.append(_s);
		return sb.toString();
	}
}
