package com.qdevelop.core.datasource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.bean.DBStrutsLeaf;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.core.standard.IUpdateHook;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QString;

@SuppressWarnings({"unchecked","rawtypes"})
public class QueryFromDataBaseImp {
	//	private static 

	public static QueryFromDataBaseImp getInstance(){
		return new QueryFromDataBaseImp();
	}
	private HashMap<String,Object> record;
	private boolean isNeedFormatter;
	private DBStrutsBean struts = null;
	private static byte[] dblock = new byte[0];

	private void checkNeedFormatter(ResultSetMetaData rsmd ,IResultFormatter[] resultFormatter) throws SQLException{
		if(resultFormatter == null || resultFormatter.length == 0) return;
		isNeedFormatter = true;
		for(IResultFormatter rf : resultFormatter){
			if(rf == null)continue;
			if(rf.isNeedStruts()){
				rf.initFormatter(getDBStrutsBean(rsmd));
			}else
				rf.initFormatter(null);
		}
	}

	private DBStrutsBean getDBStrutsBean(ResultSetMetaData rsmd) throws SQLException{
		if(struts !=null)return struts;
		struts = new DBStrutsBean();
		for(int i=1;i<=rsmd.getColumnCount();i++){
			DBStrutsLeaf sb = new DBStrutsLeaf();
			sb.setColumnName(rsmd.getColumnName(i).toUpperCase());
			sb.setColumnTypeName(rsmd.getColumnTypeName(i));
			sb.setNullAble(rsmd.isNullable(i)==1);
			sb.setAutoIncrement(rsmd.isAutoIncrement(i));
			struts.addStruts(sb);
		}
		return struts;
	}

	private void checkFlushResult(ResultBean rb,IResultFormatter[] resultFormatter){
		if(!isNeedFormatter || resultFormatter == null)return;
		for(IResultFormatter rf : resultFormatter){
			if(rf ==null)continue;
			rf.flush(rb);
		}
	}

	private Map<String,Object> parseRecord(ResultSetMetaData rsmd,ResultSet rs,int recordSize,IResultFormatter[] resultFormatter) throws QDevelopException, SQLException{
		if(record == null)record = new HashMap<String,Object>(recordSize);
		Map data = (HashMap)record.clone();
		for(int i=1;i<=recordSize;i++){
			data.put(rsmd.getColumnName(i).toUpperCase(), rs.getObject(i));
		}
		if(isNeedFormatter && resultFormatter !=null){
			for(IResultFormatter rf : resultFormatter){
				if(rf ==null)continue;
				rf.formatter(data,rf.isNeedStruts()?getDBStrutsBean(rsmd):null);
			}
		}
		return data;
	}

	/**
	 * 
	 * @param sql
	 * @param conn
	 * @param resultFormatter
	 * @return
	 * @throws QDevelopException
	 */
	public Map selectSingle(String sql,Connection conn,IResultFormatter[] resultFormatter) throws QDevelopException{
		ResultSet rs = null ;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			if(rs.next()){
				ResultSetMetaData rsmd = rs.getMetaData();
				checkNeedFormatter(rsmd,resultFormatter);
				int recordSize = rsmd.getColumnCount();
				return parseRecord(rsmd,rs,recordSize,resultFormatter);
			}
			return null;
		} catch (Exception e) {
			System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+sql);
			QLog.getInstance().sqlError(sql);
			throw new QDevelopException(e);
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 * @param sql
	 * @param conn
	 * @param resultFormatter
	 * @param tree
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean selectTree(String sql,Connection conn,ResultBean tree,IResultFormatter[] resultFormatter) throws QDevelopException{
		ResultSet rs = null ;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			checkNeedFormatter(rsmd,resultFormatter);
			int recordSize = rsmd.getColumnCount();
			Map datas;
			while(rs.next()){
				datas = parseRecord(rsmd,rs,recordSize,resultFormatter);
				tree.addResult(datas);
			}
			checkFlushResult(tree,resultFormatter);
			tree.flush();
			return tree;
		} catch (Exception e) {
			System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+sql);
			QLog.getInstance().sqlError(sql);
			throw new QDevelopException(e);
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 * @param sql
	 * @param conn
	 * @param resultFormatter
	 * @param rb
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean select(String sql,Connection conn,ResultBean rb,IResultFormatter[] resultFormatter) throws QDevelopException{
		ResultSet rs = null ;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			ps.setFetchSize(1000);
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			checkNeedFormatter(rsmd,resultFormatter);
			int recordSize = rsmd.getColumnCount();
			Map datas;
			while(rs.next()){
				datas = parseRecord(rsmd,rs,recordSize,resultFormatter);
				rb.addResult(datas);
			}
			checkFlushResult(rb,resultFormatter);
			rb.flush();
			return rb;
		} catch (Exception e) {
			System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+sql);
			QLog.getInstance().sqlError(sql);
			throw new QDevelopException(e);
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
			} catch (Exception e) {
			}
		}
	}


	/**
	 * 
	 * @param sql
	 * @param conn
	 * @param resultFormatter
	 * @param rb
	 * @param page
	 * @param maxNum
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean select(String sql,Connection conn,ResultBean rb,int page,int maxNum,IResultFormatter[] resultFormatter,boolean isMysql) throws QDevelopException{
		ResultSet rs = null ;
		PreparedStatement ps = null;
		int resultType;
		if(page*maxNum>5000)
			resultType = ResultSet.TYPE_SCROLL_SENSITIVE;//当记录量较大时，采用先获取记录rowId，再获取记录实体方式
		else
			resultType = ResultSet.TYPE_SCROLL_INSENSITIVE;//当记录量较小时，采用直接cache记录方式
		try {
			Map datas;
			if(isMysql){
				ps = conn.prepareStatement(
						new StringBuffer().append(sql).append(" limit ").append(maxNum*(page-1))
						.append(",").append(maxNum).toString()
						,resultType,ResultSet.CONCUR_READ_ONLY);
				ps.setFetchSize(maxNum);
				rs = ps.executeQuery();
				ResultSetMetaData rsmd = rs.getMetaData();
				checkNeedFormatter(rsmd,resultFormatter);
				int recordSize = rsmd.getColumnCount();
				while(rs.next()){
					datas = parseRecord(rsmd,rs,recordSize,resultFormatter);
					rb.addResult(datas);
				}
			}else{
				ps = conn.prepareStatement(sql,resultType,ResultSet.CONCUR_READ_ONLY);
				ps.setFetchSize(maxNum);
				rs = ps.executeQuery();
				ResultSetMetaData rsmd = rs.getMetaData();
				checkNeedFormatter(rsmd,resultFormatter);
				int recordSize = rsmd.getColumnCount();

				if(page>1){
					rs.absolute(maxNum*(page-1));
				}
				int idx=0;
				while(rs.next()){
					if(idx++>=maxNum){
						break;
					}
					datas = parseRecord(rsmd,rs,recordSize,resultFormatter);
					rb.addResult(datas);
				}
			}
			checkFlushResult(rb,resultFormatter);
			rb.flush();
			return rb;
		} catch (Exception e) {
			System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+sql);
			QLog.getInstance().sqlError(sql);
			throw new QDevelopException(e);
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 获取Mysql数据库某个表的Sequence ID 值
	 * 考虑到内部常使用String类型且不知道具体位数，因此返回String
	 * @param tableName	表名	
	 * @param conn
	 * @return String
	 * @throws QDevelopException
	 */
	public String getMysqlSeqID(String tableName,Connection conn) throws QDevelopException{
		ResultSet rs = null ;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(QString.append("SELECT Auto_increment as SEQID FROM information_schema.tables  WHERE table_name='",tableName,"'"),
					ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			if(rs.next()){
				return rs.getString(1);
			}
		} catch (Exception e) {
			throw new QDevelopException(e);
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 通过SQL 获取结果集表结构
	 * @param sql
	 * @param conn
	 * @return
	 * @throws QDevelopException
	 */
	public DBResultBean getStrutsBySQL(String sql,Connection conn) throws QDevelopException{
		DBResultBean rb = new DBResultBean();
		ResultSet rs = null ;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			ps.setFetchSize(1);
			ps.setMaxRows(1);
			rs = ps.executeQuery();
			rs.next();
			//			ps.getParameterMetaData().
			//			rs.getMetaData().getPrecision(column)
			rb.setResultList(getTableStruts(rs.getMetaData()));
			rb.flush();
			return rb;
		} catch (Exception e) {
			System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+sql);
			QLog.getInstance().sqlError(sql);
			throw new QDevelopException(e);
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
			} catch (Exception e) {
			}
		}
	}

	private List getTableStruts(ResultSetMetaData rsmd) throws QDevelopException{
		List result = new ArrayList();
		try {
			for (int i = 1 ; i <= rsmd.getColumnCount(); i++) {
				HashMap<String,Object> tmp = new HashMap<String,Object>(5);
				tmp.put("NAME", rsmd.getColumnName(i));
				tmp.put("TYPE", rsmd.getColumnTypeName(i));
				tmp.put("SIZE", rsmd.getColumnDisplaySize(i));
				tmp.put("NullAble", rsmd.isNullable(i));
				tmp.put("isAutoIncrement", rsmd.isAutoIncrement(i));
				//				rsmd.getp
				//				System.out.println(new StringBuffer().append(rsmd.getColumnName(i)).append(" -- isCurrency:").append(rsmd.isCurrency(i)).append("\tisCaseSensitive:").append(rsmd.isCaseSensitive(i)).append(""));
				result.add(tmp);
			}
		} catch (SQLException e) {
			throw new QDevelopException(e);
		}
		return result;
	}


	/**
	 * 
	 * @param procedureName
	 * @param params
	 * @throws QDevelopException
	 */
	public void queryProcedure(String procedureName,Object params) throws QDevelopException{
		Connection conn = getDefaultConnection();
		try {
			queryProcedure(procedureName,params,conn);
		} catch (Exception e) {
			throw new QDevelopException(e);
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 请求执行存储过程
	 * @param procedureName
	 * @param params
	 * @param conn
	 * @throws QDevelopException
	 */
	public void queryProcedure(String procedureName,Object params,Connection conn) throws QDevelopException{
		if(procedureName==null)return;
		CallableStatement stmt = null;
		try{
			stmt =  conn.prepareCall(procedureName);  
			if(params instanceof String){
				stmt.setString(1,(String)params);
			}else if(params instanceof Integer){
				stmt.setInt(1, Integer.parseInt(String.valueOf(params)));
			}else if(params instanceof List){
				List tmp = (List)params;
				for(int i=0;tmp!=null&&i<tmp.size();){
					if(tmp.get(i) instanceof String){
						stmt.setString((i+1),(String)tmp.get(i));
					}else if(tmp.get(i) instanceof Integer){
						stmt.setInt((i+1), Integer.parseInt(String.valueOf(tmp.get(i))));
					}
				}
			}else if(params instanceof String[]){
				String[] tmp = (String[])params;
				for(int i=0;i<tmp.length;i++){
					stmt.setString((i+1),tmp[i]);
				}
			}
			stmt.execute();
		}catch(Exception e){
			throw new QDevelopException(e);
		}finally{
			try {
				if(stmt!=null)
					stmt.close();
			}catch (Exception ex) {
			}
		}
	}


	/**
	 * 
	 * @param sqlObject
	 * @return
	 * @throws QDevelopException
	 */
	public int updateBatch(Object sqlObject,IUpdateHook[] iuh,String user,Integer[] fetchIndex,Integer[] judgeIndex) throws QDevelopException{
		Connection conn=null;
		try {
			conn = getDefaultConnection();
			return updateBatch(sqlObject,conn,iuh,user,fetchIndex,judgeIndex);
		} catch (Exception e) {
			throw new QDevelopException(e);
		}finally{
			try {
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 * @throws QDevelopException
	 */
	public int update(String sql,Connection conn,IUpdateHook[] iuh,String user) throws QDevelopException{
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			UpdateSQL us = new UpdateSQL(sql);
			int num = us.execute(stmt);
			if(iuh!=null){
				for(IUpdateHook iu:iuh){
					iu.collect(sql, num);
				}
			}
			if(us.isInsert()){
				num = us.getInsertedId();
				QLog.getInstance().sqlLogger(new StringBuffer().append(" [").append(user).append("] {").append(us.getCurrentId()).append("} ").append(us.getSql()).toString());
			}else{
				QLog.getInstance().sqlLogger(new StringBuffer().append("[").append(user).append("] ").append(us.getSql()).toString());
			}
			if(num>0){
				flushHook(stmt,iuh);
			}
			return num;
		}catch(Exception ex){
			throw new QDevelopException(ex);
		}finally{
			try {
				if(stmt!=null)stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}
	}

	/**
	 * 批量更新SQL语句
	 * @param qb_OR_sql
	 * @param conn
	 * @param iuh
	 * @param user
	 * @param fetchIndex
	 * @param judgeIndex 判断第几行需要判断是否监控该SQL影响记录数>0? false时 rollback，return -2
	 * @return
	 * @throws QDevelopException
	 */
	public int updateBatch(Object qb_OR_sql,Connection conn,IUpdateHook[] iuh,String user,Integer[] fetchIndex,Integer[] judgeIndex) throws QDevelopException{
		synchronized(dblock){
			if(qb_OR_sql == null){
				throw new QDevelopException("执行SQL语句数据库更新异常！SQL未初始化！");
			}
			int result= 0;

			String[] _sql = null;
			Statement stmt = null;
			Map<String,String> tmpParams = null;
			try{
				conn.setAutoCommit(false); 
				stmt = conn.createStatement();
				if(qb_OR_sql instanceof String){
					_sql = new String[]{qb_OR_sql.toString()};
				}else if(qb_OR_sql instanceof String[]){
					_sql = (String[])qb_OR_sql;
				}else if(qb_OR_sql instanceof List){
					_sql =  (String[]) ((List)qb_OR_sql).toArray(new String[]{});
				}
				UpdateSQL usql;

				for(int i=0;i<_sql.length;i++){
					String sql = _sql[i];
					/**判定是否需要将影响记录数返回**/
					boolean isFetch = (fetchIndex == null||fetchIndex.length==0)?	(i==0)	:	ArrayUtils.contains(fetchIndex, i);
					boolean isJudge = (judgeIndex == null||judgeIndex.length==0)?	false 	:	ArrayUtils.contains(judgeIndex, i);
					usql = new UpdateSQL(sql);
					usql.replaceLastInsertId(tmpParams);
					int fetchnum = usql.execute(stmt);
					if(isJudge && fetchnum < 1){
						QLog.getInstance().sqlError(new StringBuffer().append("[").append(user).append("] (judge fail) ").append(sql).toString());
						throw new QDevelopException(500,"数据库执行["+sql+"]错误，影响记录数为:"+fetchnum);
					}
					if(iuh!=null){
						for(IUpdateHook iu:iuh){
							iu.collect(sql, fetchnum);
						}
					}
					if(isFetch)result += fetchnum;
					if(usql.isInsert()){
						if(isFetch){
							result = usql.getInsertedId();
							fetchIndex = null;
						}
						if(tmpParams==null)tmpParams = new HashMap<String,String>();
						tmpParams.put(usql.getSeqName(), usql.getCurrentId());
						QLog.getInstance().sqlLogger(new StringBuffer().append(" [").append(user).append("] {").append(usql.getCurrentId()).append("} ").append(usql.getSql()).toString());
					}else{
						QLog.getInstance().sqlLogger(new StringBuffer().append("[").append(user).append("] ").append(usql.getSql()).toString());
					}
				}
				flushHook(stmt,iuh);
				conn.commit();
				return result;
			}catch(Exception ex){
				try {
					conn.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				for(String sql : _sql){
					QLog.getInstance().sqlError(new StringBuffer().append("[").append(user).append("] ").append(sql).toString());
				}
				throw new QDevelopException(ex);
			}finally{
				if(tmpParams!=null)tmpParams.clear();
				try {
					conn.setAutoCommit(true);
					if(stmt!=null)stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} 
			}
		}
	}

	/**
	 * 
	 * TODO （描述方法的作用） 
	 * 
	 * @param sqls
	 * @param conns
	 * @param iuh
	 * @param user
	 * @return
	 * @throws QDevelopException
	 */
	public int updateBatch(String[][] sqls,String[] conns,IUpdateHook[] iuh,String user,Integer[][] fetchIndex,Integer[][] judgeIndex) throws QDevelopException{
		synchronized(dblock){
			if(sqls == null){
				throw new QDevelopException("执行SQL语句数据库更新异常！SQL未初始化！");
			}
			int result=0;
			int idx=0;
			HashMap<String,Connection> tempConnect = new HashMap<String,Connection>();
			Statement stmt = null;
			Map<String,String> tmpParams = null;
			try{
				for(String _conn : conns){
					Connection conn = tempConnect.get(_conn);
					if(conn==null){
						conn = ConnectFactory.getInstance(_conn).getConnection();
						if(conn==null)throw new QDevelopException(600,"数据库连接["+_conn+"]获取不到错误！");
						conn.setAutoCommit(false); 
						tempConnect.put(_conn, conn);
					}
				}
				for(String conn : conns){
					String[] _sql = sqls[idx];
					stmt = tempConnect.get(conn).createStatement();
					UpdateSQL usql;
					for(int j=0;j<_sql.length;j++){
						String sql = _sql[j];
						/**判定是否需要将影响记录数返回**/
						boolean isFetch = (fetchIndex == null || fetchIndex[idx] == null||fetchIndex[idx].length==0)? (j==0)  :	ArrayUtils.contains(fetchIndex[idx], j);
						boolean isJudge = (judgeIndex == null || judgeIndex[idx] == null||judgeIndex[idx].length==0)?	false 	:	ArrayUtils.contains(judgeIndex[idx], j);

						usql = new UpdateSQL(sql);
						usql.replaceLastInsertId(tmpParams);
						int fetchnum = usql.execute(stmt);
						if(isJudge && fetchnum < 1){
							QLog.getInstance().sqlError(new StringBuffer().append("[").append(user).append("] (judge fail) ").append(sql).toString());
							throw new QDevelopException(500,"数据库执行["+sql+"]错误，影响记录数为:"+fetchnum);
						}
						if(iuh!=null){
							for(IUpdateHook iu:iuh){
								iu.collect(sql, fetchnum);
							}
						}
						if(isFetch)result += fetchnum;
						if(usql.isInsert()){
							if(isFetch){
								result = usql.getInsertedId();
								fetchIndex[idx] = null;
							}
							if(tmpParams==null)tmpParams = new HashMap<String,String>();
							tmpParams.put(usql.getSeqName(), usql.getCurrentId());
							QLog.getInstance().sqlLogger(new StringBuffer().append(" [").append(user).append("] {").append(usql.getCurrentId()).append("} ").append(usql.getSql()).toString());
						}else{
							if(result==-1)result=fetchnum;
							QLog.getInstance().sqlLogger(new StringBuffer().append("[").append(user).append("] ").append(usql.getSql()).toString());
						}
					}
					flushHook(stmt,iuh);
					idx++;
				}
				for(Connection conn : tempConnect.values()){
					conn.commit();
				}
				return result;
			}catch(Exception ex){

				try {
					for(Connection conn : tempConnect.values()){
						conn.rollback();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				for(String[] _sql:sqls){
					for(String sql : _sql){
						QLog.getInstance().sqlError(new StringBuffer().append("[").append(user).append("] ").append(sql).toString());
					}
				}
				throw new QDevelopException(ex);
			}finally{
				if(tmpParams!=null)tmpParams.clear();
				try {
					for(Connection conn : tempConnect.values()){
						conn.setAutoCommit(true);
						if(conn!=null)conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} 
				tempConnect.clear();
			}
		}
	}

	/**
	 * 批量更新数据，预编译方式，提升更新效率
	 * @param sql
	 * @param params
	 * @param conn
	 * @return
	 * @throws QDevelopException
	 */
	public int updateBatch(String sql,List<Object[]> params,Connection conn) throws QDevelopException{
		PreparedStatement stmt = null;
		try{
			conn.setAutoCommit(false); 
			stmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			for(Object[] param:params){
				for(int i=0;i<param.length;i++){
					Object o = param[i];
					if(o instanceof String){
						stmt.setString(i+1,String.valueOf(o));
					}else if(o instanceof Integer){
						stmt.setInt(i+1,Integer.parseInt(String.valueOf(o)));
					}else if(o instanceof Double){
						stmt.setDouble(i+1,Double.parseDouble(String.valueOf(o)));
					}
				}
				stmt.addBatch();
			}
			stmt.executeBatch();
			conn.commit();
			return stmt.getUpdateCount();
		}catch(Exception ex){
			try {
				conn.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+sql);
			throw new QDevelopException(ex);
		}finally{
			try {
				conn.setAutoCommit(true);
				if(stmt!=null)stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}
	}

	//	private void updateHook(String sql,IUpdateHook[] iuhs){
	//		/**根据SQL清理DirectCacheData**/
	//		TableDataCache.getInstance().clearCacheDataBySQL(sql);
	//
	//		if(iuhs==null)return;
	//		for(IUpdateHook iuh : iuhs){
	//			iuh.collect(sql);
	//		}
	//
	//		//		QLog.getInstance().sqlLogger(sql);
	//	}

	private void flushHook(Statement stmt,IUpdateHook[] iuhs)  throws QDevelopException{
		if(iuhs==null)return;
		for(IUpdateHook iuh : iuhs){
			iuh.flush(stmt);
		}
	}

	private Connection getDefaultConnection(){
		try {
			return ConnectFactory.getInstance().getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param qb
	 * @return
	 */
	public DBStrutsBean getDBStrutsBean(DBQueryBean qb){
		Connection conn = null;
		ResultSet rs = null ;
		PreparedStatement ps = null;
		try {
			conn = ConnectFactory.getInstance(qb.getConnect()).getConnection();
			ps = conn.prepareStatement(new StringBuffer().append(qb.getSql()).append(" limit 1").toString(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			if(rs.next()){
				ResultSetMetaData rsmd = rs.getMetaData();
				return getDBStrutsBean(rsmd);
			}
			return null;
		} catch (Exception e) {
			System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+qb.getSql());
			QLog.getInstance().sqlError(qb);
			throw new QDevelopException(e);
		} finally {
			try {
				if(rs!=null)rs.close();
				if (ps != null)	ps.close();
				if(conn!=null)conn.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
