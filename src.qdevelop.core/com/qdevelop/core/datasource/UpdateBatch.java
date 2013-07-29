package com.qdevelop.core.datasource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.utils.QDate;

public class UpdateBatch {
	
	/**整数类型**/
	public static int INT = 0;
	/**双精度类型**/
	public static int DOUBLE = 1;
	/**字符串类型**/
	public static int STRING = 2;
	/**日期类型**/
	public static int DATE = 3;
	
	private String sql;
	public UpdateBatch(){
		
		
	}
	public UpdateBatch(String sqlIndex) throws Exception{
		try {
			DBQueryBean qb = CoreFactory.getInstance().getQueryBean(sqlIndex);
			sql  = clean(qb.getSql());
			con = ConnectFactory.getInstance(qb.connect).getConnection();
			con.setAutoCommit(false);
			prest = con.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			con.setAutoCommit(true);
			this.closeConnect();
			throw e;
		} 			
	}
	
	public void setSql(String sql) throws Exception{
		setSql(sql,QueryBean.CONNECT_DEFAULT);
	}
	
	public void setSql(String sql,String connectConfig) throws Exception{
		try {
			con = ConnectFactory.getInstance(connectConfig).getConnection();
			con.setAutoCommit(false);
			this.sql  = clean(sql);
			prest = con.prepareStatement(this.sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			con.setAutoCommit(true);
			this.closeConnect();
			throw e;
		} 
	}
	
	private String clean(String sql){
		return sql.replaceAll("'\\?'", "?");
	}
	
	private PreparedStatement  prest=null;
	public boolean hasData = false;
	private Connection con=null;
	public void parseParam(UpdateParamBean upb)  throws Exception{
		try {
			for(int i=0;i<upb.size;i++){
				if(upb.getType(i) == UpdateParamBean.INT){
					prest.setInt(i+1, upb.getIntValue(i));
				}else prest.setString(i+1, upb.getStringValue(i));
			}
			prest.addBatch();
			hasData = true;
		} catch (SQLException e) {
			this.closeConnect();
			throw e;
		}
	}
	private boolean isNeedSql;
	private String _sql;
	public String parseParam(int[] type,Object[] value) throws Exception{
		if(isNeedSql)_sql = new String(sql);
		try {
			int valLenth = value.length;
			for(int i=0;i<type.length;i++){
				switch(type[i]){
				case 0:
					int val = 0;
					if(i<valLenth)
						val = Integer.parseInt(String.valueOf(value[i]));
					if(prest!=null)prest.setInt(i+1,val);
					if(isNeedSql)_sql = _sql.replaceFirst("\\?", String.valueOf(val));
					break;
				case 1:
					double valDouble = 0.0;
					if(i<valLenth)
						valDouble = Double.parseDouble(String.valueOf(value[i]));
					if(prest!=null)prest.setDouble(i+1,valDouble);
					if(isNeedSql)_sql = _sql.replaceFirst("\\?", String.valueOf(valDouble));
					break;
					
				case 2:
					String valStr = null;
					if(i<valLenth)
						valStr =  String.valueOf(value[i]);
					if(prest!=null)prest.setString(i+1,valStr);
					if(isNeedSql)_sql = _sql.replaceFirst("\\?", new StringBuffer().append("'").append(valStr).append("'").toString());
					break;
				case 3:
					Date valDate = null;
					if(i<valLenth){
						if(value[i] instanceof java.util.Date){
							valDate = new java.sql.Date(((java.util.Date)value[i]).getTime());
						}else if(value[i] instanceof String){
							try {
								java.util.Date arg = QDate.parseDateAuto((String)value[i]);
								valDate = new java.sql.Date(arg.getTime());
							} catch (Exception e) {
								valDate = new java.sql.Date(System.currentTimeMillis());
								e.printStackTrace();
							}
						}else if(value[i] instanceof Long){
							valDate = new java.sql.Date((Long)value[i]);
						}
					}
					if(prest!=null)prest.setDate(i+1, valDate);
					if(isNeedSql)_sql = _sql.replaceFirst("\\?", new StringBuffer().append("'").append(value[i]).append("'").toString());
					break;
				}
			}
			if(prest!=null)prest.addBatch();
			hasData = true;
			if(isNeedSql)return _sql;
		} catch (Exception e) {
			this.closeConnect();
			throw e;
		}
		return null;
	}

	public void setNeedSql(boolean isNeedSql){
		this.isNeedSql = isNeedSql;
	}

	public int updateBatch() throws Exception{
		try{
			prest.executeBatch();
			con.commit();
			return prest.getUpdateCount();
		}catch(Exception e){
			try {
				con.rollback();
			} catch (SQLException e1){}
			throw e;
		}finally{
			this.closeConnect();
		}
	}

	public void closeConnect(){
		try {
			if(con!=null){
				con.setAutoCommit(true);
				if(!con.isClosed())con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		UpdateBatch ub = new UpdateBatch();
		ub.sql = "insert into sdasd values(?,?)";
		ub.setNeedSql(true);
		int[] type = new int[]{0,2};
		Object[] obj =  new Object[]{1,"aaa"};
		try {
			System.out.println(ub.parseParam(type, obj));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
