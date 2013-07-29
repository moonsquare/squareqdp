package com.qdevelop.core.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.regex.Pattern;

import com.qdevelop.core.utils.NoAutoInsertTableCollect;


public class UpdateSQL{
	private static final Pattern InsertTableReg = Pattern.compile("^INSERT +INTO +|^insert +into +|\\(.+\\)|value.+?$|VALUE.+?$| +");
	private static final Pattern tablePattern = Pattern.compile("^.+?INSERT|^.+?INTO|^UPDATE| WHERE.+?$| SET.+?$| VALUE.+?$|\\(.+?\\)|^.+?FROM|`");
	private static final Pattern currentTableName = Pattern.compile("\\.CURRENT_ID.+$|\\.CURRENT_ID$");

	public UpdateSQL(String sql){
		this.sql = sql;
		isInsert = this.sql.substring(0,6).toLowerCase().equals("insert");
		if(isInsert){
			seqName = InsertTableReg.matcher(sql.replaceAll("\n|\t", "")).replaceAll("").trim();
		}
	}

	public int execute(Statement stmt) throws SQLException{
		if(isInsert){
			int r = stmt.executeUpdate(this.sql, Statement.RETURN_GENERATED_KEYS);
			String table = tablePattern.matcher(this.sql.toUpperCase().replaceAll("\n|\t", "")).replaceAll("").trim();
			if(NoAutoInsertTableCollect.isAutoInsert(table)){
				try {
					ResultSet resultset=stmt.getGeneratedKeys();
					if(resultset.next()){
						setCurrentId(resultset.getInt(1));
					}else{
						NoAutoInsertTableCollect.addCollect(table);
						this.currentId = 1;
					}
				} catch (Exception e) {
					NoAutoInsertTableCollect.addCollect(table);
					this.currentId = 1;
					System.out.println("No AutoInsert Id!!");
				}
			}else{
				this.currentId = 1;
			}
			return r;
		}else{
			return stmt.executeUpdate(this.sql);
		}
	}

	public void replaceLastInsertId(Map<String,String> lastInsertIds){
		if(lastInsertIds!=null && this.sql.indexOf(".CURRENT_ID")>-1){
			String tableName = currentTableName.matcher(sql.replaceAll("\n|\t", "")).replaceAll("").trim();
			if(tableName.indexOf(" ")>-1)
				tableName = tableName.substring(tableName.lastIndexOf(" ")+1);
			if(tableName.indexOf("=")>-1)
				tableName = tableName.substring(tableName.lastIndexOf("=")+1);
			if(tableName.indexOf(",")>-1)
				tableName = tableName.substring(tableName.lastIndexOf(",")+1);
			if(tableName.indexOf("(")>-1)
				tableName = tableName.substring(tableName.lastIndexOf("(")+1);

			if(lastInsertIds.get(tableName)!=null){
				this.sql = this.sql.replace(new StringBuffer().append(tableName).append(".CURRENT_ID").toString(), lastInsertIds.get(tableName));
			}
		}
	}



	private String sql;
	private boolean isInsert;
	private String seqName;
	private int currentId;

	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public boolean isInsert() {
		return isInsert;
	}
	public void setInsert(boolean isInsert) {
		this.isInsert = isInsert;
	}
	public String getSeqName() {
		return seqName;
	}
	public void setSeqName(String seqName) {
		this.seqName = seqName;
	}

	public String getCurrentId() {
		return String.valueOf(currentId);
	}

	public int getInsertedId(){
		return this.currentId;
	}

	public void setCurrentId(int currentId) {
		this.currentId = currentId;
	}

}
