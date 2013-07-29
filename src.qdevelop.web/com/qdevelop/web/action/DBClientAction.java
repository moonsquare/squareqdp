package com.qdevelop.web.action;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.cache.FirstCache;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.DataBaseFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.web.utils.HtmlFormatter;
import com.qdevelop.web.utils.WebUtils;

@SuppressWarnings({ "rawtypes" })
public class DBClientAction extends QDevelopAction{

	private static final long serialVersionUID = 279876800346227984L;

	private int total;
	private List rows;
	private String msg;
	private final static IResultFormatter[] queryHtmlFormatter = new IResultFormatter[]{new HtmlFormatter()};

	public String queryDB() throws QDevelopException{
		Map query = this.getParamMap();
		this.isLazyPagination = true;
		this.initPagination(query);
		String sql;
		if(query.get("sql")==null)throw new QDevelopException("SQL 未设定！");
		sql = query.get("sql").toString();
		/**兼容EasyUI Datagrid数据请求格式**/
		String connect = getConnect(query);
		Connection conn = null;
		ResultBean rb = new DBResultBean();
		try {
			conn = ConnectFactory.getInstance(connect).getConnection();
			QueryFromDataBaseImp.getInstance().select(sql, conn,rb,page,maxNum, queryHtmlFormatter ,ConnectFactory.getDatabase(connect).equals("MYSQL"));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(conn!=null )
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//CoreFactory.getInstance().getQueryResult(qb);
		total = getLazyPageSize(rb);
		rows = rb.getResultList();
		return SUCCESS;
	}


	public String clearCache() throws QDevelopException{
		Map query = this.getParamMap();
		if(query.get("tableName")==null)throw new QDevelopException("tableName 未设定！");
		/**兼容EasyUI Datagrid数据请求格式**/
		String connect = getConnect(query);
		String tableName = (String)query.get("tableName");
		Connection _conn=null;
		ResultSet rs = null ;
		PreparedStatement ps = null;
		int size=0;
		long timer = System.currentTimeMillis();
		try {
			_conn = ConnectFactory.getInstance(connect).getConnection();
			DatabaseMetaData dbMeta = _conn.getMetaData(); 
			rs = dbMeta.getPrimaryKeys(null,null,tableName); 
			rs.next();
			String uniKey = rs.getString(4); 
			String where = query.get("where") == null? "1=1" : (String)query.get("where");
			ps = _conn.prepareStatement(new StringBuffer().append("select ").append(uniKey).append(" from ").append(tableName)
					.append(" where ").append(where).toString()
					,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			while(rs.next()){
				++size;
				String key = String.valueOf(rs.getObject(1));
				System.out.print(key+"\t");
				FirstCache.getInstance().remove(key, tableName);
			}
			msg = "Table:["+tableName+"] Where:["+where+"] clear:["+size+"] use_time:["+(System.currentTimeMillis()-timer)+"]";
		} catch (QDevelopException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
				if(_conn!=null)_conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return SUCCESS;
	}

	public String updateDB() throws QDevelopException{
		/**兼容多index格式**/
		/**isMultiQuery=true 兼容split param值生成多SQL**/
		Map query = this.getParamMap();
		String sql;
		if(query.get("sql")==null)throw new QDevelopException("SQL 未设定！");
		sql = query.get("sql").toString();
		String connect = getConnect(query);
		if(sql.indexOf(";")>-1)
			total = DataBaseFactory.getInstance().update(connect,sql.split(";"),null);
		else 
			total = DataBaseFactory.getInstance().update(connect,sql,null);
		return SUCCESS;
	}

	public String tableNames() throws QDevelopException{
		String connect = getConnect(this.getParamMap());
		String database = ConnectFactory.getDatabase(connect);
		if(database.equals("ORACLE")){
			rows = CoreFactory.getInstance().getQueryResult("SELECT TABLE_NAME FROM TABS ORDER BY TABLE_NAME",connect).getResultList();

		}else if(database.equals("MYSQL")){
			rows = CoreFactory.getInstance().getQueryResult("SHOW TABLES",connect).getResultList();
		}
		return SUCCESS;
	}

	public String struts() throws QDevelopException{
		/**兼容多index格式**/
		Map query = this.getParamMap();
		String sql;
		if(query.get("sql")==null)throw new QDevelopException("SQL 未设定！");
		sql = query.get("sql").toString();
		rows = DataBaseFactory.getInstance().getQueryStruts(getConnect(query),sql,!WebUtils.isNeedCacheStruts.matcher(sql.toUpperCase()).find());
		return SUCCESS;
	}
	private String getConnect(Map param){
		String connect = String.valueOf(param.get("connect"));
		if(!connect.equals("null")){
			param.remove("connect");
		}else{
			connect = QueryBean.CONNECT_DEFAULT;
		}
		return connect;
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


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}

}
