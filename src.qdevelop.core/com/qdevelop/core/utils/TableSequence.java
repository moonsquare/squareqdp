package com.qdevelop.core.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.cache.MapCache;

public class TableSequence {
	private static String TABLE_SEQ_CACHE_CONFIG = "com.qdevelop.core.utils.TableSequence";
	
	@SuppressWarnings("unchecked")
	public static int getSeqIdByTable(String key,String tableName,String connect){
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(connect).getConnection();
			Map<String,Object> data = QueryFromDataBaseImp.getInstance().selectSingle(QString.append("SELECT Auto_increment as SEQID FROM information_schema.tables  WHERE table_name='",tableName,"'"), conn,null);
			if(data!=null && data.size()>0){
				return Integer.parseInt(String.valueOf(data.get("SEQID")));
			}
		} catch (QDevelopException e) {
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
	
	public static int getNextIdByTable(String key,String tableName,String connect){
		return (getSeqIdByTable(key,tableName,connect));
	}
	public static void clear(){
		MapCache.getInstance().removeAllCache(TABLE_SEQ_CACHE_CONFIG);
	}
	
	public static void main(String[] args) {
		System.out.println(TableSequence.getNextIdByTable("id","compact","biz"));
	}
}
