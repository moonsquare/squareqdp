package com.qdevelop.cache.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.schedule.ISchedule;
import com.qdevelop.core.schedule.QScheduleFactory;
import com.qdevelop.core.sqlmodel.SQLModelParser;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.web.bean.UrlResultBean;
import com.qdevelop.web.utils.SecurityFactory;

public class OperaterLogQueue extends ConcurrentLinkedQueue<Object[]>{
	private static OperaterLogQueue _OperaterLogThread = new OperaterLogQueue();
	private byte[] _lock = new byte[0];
	public OperaterLogQueue(){
		QScheduleFactory.getInstance().addSchedule(new ISchedule(){
			public void run() {
				OperaterLogQueue.getInstance().syncLog();
			}
		}, 60, 30);
	}
	private static final long serialVersionUID = 2888990483385308935L;
	private int size = 0;
	public static OperaterLogQueue getInstance(){
		return _OperaterLogThread;
	} 
	
	/**
	 * 用户的操作日志
	 * //user_id,user_name,fun_name,uri,content,oper_time,ip
	 * @param oper
	 */
	public void addLog(Object[] oper){
		synchronized(_lock){
			this.offer(oper);
			size++;
			if(size>20)syncLog();
		}
	}
	
	public Object[] nextLog(){
		size--;
		return this.poll();
	}
	public boolean hasLog(){
		return size>0;
	}
	
	private DBQueryBean operaterQueryBean;
	
	/**
	 * 把用户操作日志同步到数据库中去
	 */
	public void syncLog() {
		if(!hasLog())return;
		PreparedStatement  prest=null;
		Connection conn=null;
		if(operaterQueryBean==null){
			try {
				Map<String,String> query = new HashMap<String,String>();
				query.put("index", "operaterLogInsert");
				operaterQueryBean = SQLModelParser.getInstance().getQueryBean(query, null, null);
			} catch (QDevelopException e) {
				operaterQueryBean = new DBQueryBean();
				operaterQueryBean.setConnect(QueryBean.CONNECT_DEFAULT);
				operaterQueryBean.setSql("insert into qd_operater_log(user_id,user_name,fun_name,uri,content,oper_time,ip) value(?,?,?,?,?,?,?)");
			}
		}
		try{
			UrlResultBean urlBean = SecurityFactory.getInstance().getUrlResultBean();
			conn = ConnectFactory.getInstance(operaterQueryBean.getConnect()).getConnection();
			prest = conn.prepareStatement(operaterQueryBean.getSql(),ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String funName,uri; 
			while(hasLog()){
				//user_id,user_name,fun_name,uri,content,oper_time,ip
				Object[] log = nextLog();
				if(log==null)continue;
				funName = urlBean.getNameByUrl(String.valueOf(log[2]));
				uri = log[3]==null?"":(String)log[3];
				prest.setString(1,log[0]==null?"":(String)log[0]);
				prest.setString(2,log[1]==null?"":(String)log[1]);
				prest.setString(3,funName == null ?(String)log[2]:funName );
				prest.setString(4,uri.replace("publicJson/", ""));
				prest.setString(5,log[4]==null?"":(String)log[4]);
				prest.setTimestamp(6,log[5]==null?new Timestamp(System.currentTimeMillis()):new Timestamp((Long)log[5]));
				prest.setString(7,log[6]==null?"":(String)log[6]);
				prest.addBatch();
			}
			prest.executeBatch();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			try {
				if(prest!=null)
					prest.close();
				if(conn!=null){
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
