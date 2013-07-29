package com.qdevelop.cache.clear;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.lang.QDevelopException;

/**
 * 一级缓存索引
 * @author Janson
 *
 */
public class TableIndexs extends ConcurrentHashMap<String,String[]>{
	private static TableIndexs _LoadTableIndexs = new TableIndexs();
	public static TableIndexs getInstance(){
		return _LoadTableIndexs;
	}
	transient static ConcurrentHashMap<String,String> tableKeyCollect = new ConcurrentHashMap<String,String>();

	/**
	 * 
	 */
	private static final long serialVersionUID = -1162970164292463260L;

	private String toKey(String connect,String tableName){
		return new StringBuffer().append(connect).append(tableName).toString();
	}
	public void addTableIndex(String connect,String tableName,String primitKey){
		String key = toKey(connect,tableName);
		String[] item = new String[3];
//		if(item == null){
//			/**
//			 * arg[0] table
//			 * arg[1] connect
//			 * arg[2] primitKey
//			 */
//			item = new String[3];
//			this.put(key, item);
//		}
		item[0] = tableName;
		item[1] = connect;
		item[2] = primitKey;
		tableKeyCollect.put(tableName, key);
		this.put(key, item);
	}

	public  String getTablePrimaryKey(String tableName,String connect){
		String key = toKey(connect,tableName);
		String[] item = this.get(key);
		if(item!=null && item.length==3 && item[2]!=null){
			return item[2];
		}
		Connection conn = null;
		try {
			conn = ConnectFactory.getInstance(connect).getConnection();
			return getTablePrimaryKey(tableName,conn,connect);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void initByConnect(String connectName,String database,Connection conn) {
		String sql=null;
		if(database.equals("MYSQL")){
			sql = "SHOW TABLES";
		}else if(database.equals("ORACLE")){
			sql = "SELECT TABLE_NAME FROM TABS ORDER BY TABLE_NAME";
		}
		connectName = connectName.replaceAll("\\_R$", "");
		if(sql==null)return;
		ResultSet rs = null ;
		ResultSet prs = null ;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			DatabaseMetaData dbMeta = conn.getMetaData(); 
			rs = ps.executeQuery();
			while(rs.next()){
				String table = String.valueOf(rs.getObject(1));
				if(tableKeyCollect.get(table)==null){
					prs = dbMeta.getPrimaryKeys(null,null,table);
					if(prs!=null){
						if(prs.next()) {
							String[] item = new String[3];
							item[0] = table;
							item[1] = connectName;
							item[2] = prs.getString(4);
							String key = toKey(connectName,table);
							if(this.get(key)==null){
								tableKeyCollect.put(table, key);
								this.put(key, item);
							}
						} 
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
				if(conn!=null)conn.close();
			} catch (Exception e) {
			}
		}
	}

	public String getTablePrimaryKey(String tableName,Connection conn,String connectName) throws SQLException{
		if(connectName==null)connectName = conn.getCatalog();
		String key = toKey(connectName,tableName);
		String[] item = this.get(key);
		ResultSet rs=null;
		if(item!=null && item.length==3 && item[2]!=null){
			return item[2];
		}else{
			tableKeyCollect.put(tableName, key);
			item = new String[3];
			this.put(key, item);
		}
		try {
			DatabaseMetaData dbMeta = conn.getMetaData(); 
			rs = dbMeta.getPrimaryKeys(null,null,tableName); 
			while(rs.next()) {
				item[0] = tableName;
				item[1] = connectName;
				item[2] = rs.getString(4);
			} 

		} catch (QDevelopException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				if(rs!=null)rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return item[2];
	}

	/**
	 * 根据表名获取一级缓存key配置，有key即表示有一级缓存配置存在
	 * @param tableName
	 * @return
	 */
	public String getPrimaryKey(String tableName){
		String key =  tableKeyCollect.get(tableName);
		if(key==null)return null;
		String[] item = this.get(key);
		return (item==null||item.length<3)?null:item[2];
	}

	public String[] getPrimaryItem(String tableName){
		String key =  tableKeyCollect.get(tableName);
		if(key==null)return null;
		String[] item = this.get(key);
		return (item==null||item.length<3)?null:item;
	}

	public String getConnect(String tableName){
		String key =  tableKeyCollect.get(tableName);
		if(key==null)return null;
		String[] item = this.get(key);
		return (item==null||item.length<3)?null:item[1];
	}

	public String toString(){
		Iterator<String[]> itor = this.values().iterator();
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean isNext = false;
		while(itor.hasNext()){
			String[] item = itor.next();
			System.out.println(item[1]);
			sb.append(isNext?",":"").append(item[0]).append(":'").append(item[2]).append("'");
			isNext=true;
		}
		return sb.append("}").toString();
	}

}
