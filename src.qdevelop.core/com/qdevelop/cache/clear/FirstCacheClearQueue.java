package com.qdevelop.cache.clear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.qdevelop.cache.FirstCache;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.utils.QLog;

public class FirstCacheClearQueue implements Runnable{
	public static Pattern replace = Pattern.compile("^.+ where ",Pattern.CASE_INSENSITIVE);
	public static Logger log = QLog.getInstance().getLog("asynClean"); 
	
	String _sql,osql,connect,tableName;

	public FirstCacheClearQueue(String sql,String primitKey,String tableName,String connect){
		this.connect = connect;
		_sql = replace.matcher(sql).replaceAll("select "+primitKey+" from "+tableName+" where ");
		this.tableName=tableName;
		osql=sql;
	}


	@Override
	public void run() {;
		Connection _conn=null;
		ResultSet rs = null ;
		PreparedStatement ps = null;
		int size=0;
		try{
			_conn = ConnectFactory.getInstance(connect).getConnection();
			if(_conn==null)return;
			ps = _conn.prepareStatement(_sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			String keyVal;
			while(rs.next()){
				keyVal = String.valueOf(rs.getObject(1));
				FirstCache.getInstance().remove(keyVal, tableName);
				++size;
			}
			log.info(new StringBuffer().append("backup firstCache remove [").append(size).append("] with:[").append(osql).append("]").toString());
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

	}
}
