package com.qdevelop.web.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QString;

public class Log2DBThread extends Thread{
	HttpServletRequest request;SecurityUserBean sub;String uri,referer;
	public Log2DBThread(HttpServletRequest request,SecurityUserBean sub,String uri,String referer){
		this.request = request;
		this.sub = sub;
		this.uri = uri;
		this.referer = referer;
	}
	
	public void run(){
		if(uri.length() == 0)return;
		String fun = referer==null ? referer : SecurityFactory.getInstance().getUrlResultBean().getNameByUrl(WebUtils.getRomveHeaderURI(request, referer));
		if(fun == null)return;
		String logSQL = QString.append("insert into qd_operater_log(user_id,user_name,fun_name,uri,content,oper_time,ip) value('",sub.id,"','",sub.name,"','",fun,"','",uri.replace("publicJson/", ""),"','",WebUtils.args(request),"','",QDate.getNow("yyyy-MM-dd HH:mm:ss"),"','",sub.userIP,"')");

		Connection conn=null;
		Statement stmt=null;
		try {
			conn = ConnectFactory.getInstance(QueryBean.CONNECT_DEFAULT).getConnection();
			stmt = conn.createStatement();
			stmt.execute(logSQL);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				if(stmt!=null)
					stmt.close();
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
