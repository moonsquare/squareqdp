package com.qdevelop.web.action;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.web.bean.SQLResultBean;

public class DBParserSQLAction extends DownLoadAction {

	private static final long serialVersionUID = -4509438935778566603L;
	@SuppressWarnings("unchecked")
	public String execute() throws Exception{
		resultStream = parserSQLResultBean(this.getParamMap()).toInputStream();
		return SUCCESS;
	}
	private InputStream resultStream;

	public InputStream getDownloadFile() {
		return resultStream;
	}

	/**
	 * 初始话SQL数据
	 * @param query
	 * @return
	 * @throws QDevelopException
	 */
	public SQLResultBean parserSQLResultBean(Map<String,String> query) throws QDevelopException{
		String formatter = query.get("formatter");
		if(formatter == null){
			return null;
		}
		query.remove("formatter");
		query.remove("fileName");
		DBQueryBean qb ;
		if(query.get("sql")!=null){
			qb = CoreFactory.getInstance().createQueryBean(query.get("sql").toString());
		}else{
			qb = CoreFactory.getInstance().getQueryBean(query);
		}
		SQLResultBean sqlResultBean = new SQLResultBean(formatter,qb.getSql());
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(qb.getConnect()).getConnection();
			QueryFromDataBaseImp.getInstance().select(qb.getSQLByOrder(), conn, sqlResultBean, null);
			return sqlResultBean;
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
			}
		}
	}

//	public static void main(String[] args){
//		Map query = new HashMap();
//		query.put("formatter", "INSERT INTO QD_JAAS_USER(RID,GID,LOGINNAME,NAME,PASSWORD,MARKER,INFO) values('{RID}','{GID}','{LOGINNAME}','{NAME}','{PASSWORD}',{MARKER},{INFO});");
//		query.put("sql", "select * from QD_JAAS_USER");
//		SQLResultBean rb = new DBParserSQLAction().parserSQLResultBean(query);
//		System.out.println(rb);
//	}

}
