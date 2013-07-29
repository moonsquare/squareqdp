package com.qdevelop.core.datasource.rollback;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.qdevelop.cache.clear.SQLClearBean;
import com.qdevelop.cache.clear.TableIndexs;
import com.qdevelop.core.datasource.UpdateSQL;

public class RollBackSQLBean extends ArrayList<String>{

	private static final long serialVersionUID = 8918328969072903558L;

	public RollBackSQLBean(Connection conn,UpdateSQL ... sqls){
		for(UpdateSQL sql:sqls){
			addRollSql(conn,sql);
		}
	}

	public RollBackSQLBean(){
	}

	public void addRollSql(Connection conn,UpdateSQL usql) {
		try {
			SQLClearBean cb = new SQLClearBean(usql.getSql());
			String primitKey = TableIndexs.getInstance().getTablePrimaryKey(cb.getTableName(), conn, null);
			switch(cb.getOperater()){
			case SQLClearBean.INSERT : 
				if(usql.getInsertedId()>1){
					this.add(new StringBuffer().append("delete from ").append(cb.getTableName()).append(" where ").append(primitKey).append("='").append(usql.getInsertedId()).append("'").toString());
				}else{
					ArrayList<String> ids = cb.getPrimitValue(primitKey);
					for(String idVal:ids){
						this.add(new StringBuffer().append("delete from ").append(cb.getTableName()).append(" where ").append(primitKey).append("='").append(idVal).append("'").toString());
					}
				}
				break;
			case SQLClearBean.UPDATE : 

				break;
			case SQLClearBean.DELETE : 

				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void rollback(Connection conn){
//		for(String sql:this){
//
//		}
	}
	
}
