package com.qdevelop.cache.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qdevelop.cache.FirstCache;
import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.cache.tactics.FirstTactiesImpl;
import com.qdevelop.core.connect.ConnectFactory;

public class LoadDataThread extends ArrayList<Map<String,Object>> implements Runnable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -776867437750585232L;
	String config, sql, uniKey,tableName;boolean isLoad;
	public LoadDataThread(String config,String sql,String uniKey,String tableName,boolean isLoad){
		this.config = config;
		this.sql = sql;
		this.uniKey = uniKey;
		this.isLoad = isLoad;
		this.tableName = tableName;
		if(!this.config.endsWith("_R")){
			this.config = this.config+"_R";
		}
	}

	@Override
	public void run() {
		long ss = System.currentTimeMillis();
		FirstTactiesImpl fti = null;
		if(isLoad){
			fti = new FirstTactiesImpl();
		}
		Connection _conn=null;
		ResultSet rs = null ;
		PreparedStatement ps = null;
		try{
			_conn = ConnectFactory.getInstance(config).getConnection();
			ps = _conn.prepareStatement(sql.toString(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int recordSize = rsmd.getColumnCount(); 
			String keyVal;
			while(rs.next()){
				Content.size++;
				keyVal = String.valueOf(rs.getObject(uniKey));
				if(!isLoad){
					FirstCache.getInstance().remove(keyVal, tableName);
				}else{
					HashMap<String,Object> data = new HashMap<String,Object>();
					for(int i=1;i<=recordSize;i++){
						data.put(rsmd.getColumnName(i).toUpperCase(), rs.getObject(i));
					}
					if(MemCachedImpl.getInstance().add(fti.toKey(keyVal, tableName),data,MemCacheConfig.ONE_DAY))
						this.add(data);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(rs!=null)
					rs.close();
				if(_conn!=null)
					_conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		for(Map<String,Object> data:this){
			data.clear();
		}
		this.clear();
		System.out.println("firstCache "+(isLoad?"load":"remove")+"======> "+sql.toString()+" use:"+(System.currentTimeMillis()-ss));
	}

}
