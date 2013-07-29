package com.qdevelop.cache.clear;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.qdevelop.utils.QLog;

public class SQLClearBean {
	public static Pattern replaceBlank = Pattern.compile(" +");
	public static Pattern replaceEquals = Pattern.compile(" ?= ?");
	public static Pattern replaceInsert = Pattern.compile(" ?, ?");
	public static Pattern replaceNothingStr = Pattern.compile("^ +?| +?$|`");
//	public static Pattern specilStr = Pattern.compile("''| |\\{|\\$|\\{|\\)|\\*|\\+|（|\\|");
	public static Pattern regValues = Pattern.compile("^[a-z|A-Z|0-9|\\_|-|@|\\.|:|,|']+?$");
	public static Pattern updateClear = Pattern.compile("^update | where |\\<(.+?)\\>| set | and | or |[a-z|A-Z|0-9|\\_]+( +?)?=( +?)?now\\(\\)",Pattern.CASE_INSENSITIVE);
	public static Pattern insertClear = Pattern.compile("insert|into| |\r|\n|\\<(.+?)\\>",Pattern.CASE_INSENSITIVE);
	public static Pattern insertSplit = Pattern.compile("\\) ?(value|values) ?\\(",Pattern.CASE_INSENSITIVE);


	private String tableName;
	private int operater;
	private String sql;
	private String[] whereKey;
	private String[] whereVal;

	public final static int INSERT=1;
	public final static int UPDATE=2;
	public final static int DELETE=3;

	public SQLClearBean(String sql){
		this.sql = sql;
		sql=replaceNothingStr.matcher(sql).replaceAll("");
		String tmp = sql.substring(0,6);
		if(tmp.equalsIgnoreCase("insert")){
			parserFromInsert(this.sql);
			operater=INSERT;
		}else if(tmp.equalsIgnoreCase("update")){
			parserFromUpdate(sql);
			operater=UPDATE;
		}else if(tmp.equalsIgnoreCase("delete")){
			parserFromDelete(sql);
			operater=DELETE;
		}
	}

	private void parserFromInsert(String insertSql){
		try {
			String sql = insertClear.matcher(insertSql).replaceAll("");
			tableName = sql.substring(0,sql.indexOf("("));
			String[] ts =insertSplit.split(sql);
//					sql.split("\\) ?(value|values|VALUE|VALUES) ?\\(");
			String[] tempkeys = ts[0].replaceAll("^.+\\(|\\)|`", "").toUpperCase().split(",");
			String[] values = new String[tempkeys.length];
			String[] vals = ts[1].replaceAll("^\\(|\\)$", "").replaceAll("( | +)?,( | +)?", ",").split(",");
			int idx = 0;
			int len = 0;
			for(String v:vals){
				if(v.length() == 0)continue;
				if((v.startsWith("'")&&v.endsWith("'"))||(v.indexOf("'")==-1&&v.indexOf(")")==-1&&v.indexOf("(")==-1)){
					if(regValues.matcher(v).find() && !v.equals("'")){
						if(idx<tempkeys.length && v.length() > 0){
							values[idx] = v.replaceAll("'", "");
							len++;
						}
					}
					idx++;
				}else if(v.endsWith(")")||v.endsWith("'")){
					if(idx<tempkeys.length)values[idx++]=null;
				}
			}
			whereKey = new String[len];
			whereVal = new String[len];
			idx = 0;
			for(int i=0;i<tempkeys.length;i++){
				if(values[i]!=null ){
					whereKey[idx] = tempkeys[i];
					whereVal[idx] = values[i];
					idx++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			QLog.getInstance().getLog("system").fatal("parseError:\t"+sql);
		}
	}

	private void parserFromUpdate(String updateSql){
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> vals = new ArrayList<String>();
		String[] ts = updateClear.matcher(updateSql).replaceAll(",").replaceAll("`", "").split(",");
		for(String _t : ts){
			if(_t.length()>0){
				if(tableName==null){
					tableName = _t;
				}
				if(_t.indexOf("=")>-1&&_t.indexOf("(")==-1){
					String key = _t.substring(0,_t.indexOf("=")).trim().toUpperCase();
					String val = _t.substring(_t.indexOf("=")+1).trim();
					if((val.startsWith("'")&&val.endsWith("'")) || val.indexOf("'")==-1){
						val=val.replaceAll("'", "");
						if(val.length()>0&&regValues.matcher(val).find()){
							keys.add(key);
							vals.add(val);
						}
					}
				}
			}
		}
		if(keys.size()>0){
			whereKey = keys.toArray(new String[]{});
			whereVal = vals.toArray(new String[]{});
		}
	}

	private void parserFromDelete(String sql){
		String tmpKey="";
		String tmpVal="";
		String[] sqlArgs =  replaceEquals.matcher(replaceBlank.matcher(sql).replaceAll(" ")).replaceAll("=").split(" ");
		boolean isBegin = false;
		int idx;
		for(int i=0;i<sqlArgs.length;i++){
			if(isBegin && sqlArgs[i].indexOf("=")>-1){
				idx=sqlArgs[i].indexOf("=");
				String t = sqlArgs[i].substring(idx+1);
				if(regValues.matcher(t).find()){
					tmpKey += "|"+sqlArgs[i].substring(0,idx).toUpperCase();
					tmpVal += "|"+ t;
				}
			}
			if(sqlArgs[i].equalsIgnoreCase("from")){
				tableName = sqlArgs[i+1];
			}
			if(sqlArgs[i].equalsIgnoreCase("where")){
				isBegin = true;
			}
		}
		if(tmpKey.length()>0){
			whereKey = tmpKey.substring(1).split("\\|");
			whereVal = tmpVal.substring(1).replaceAll("'", "").split("\\|");
		}
		if(tableName==null || tableName.length()==0)
			tableName=sqlArgs[1];
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("{table:'").append(this.tableName)
		.append("',operate:").append(this.operater).append(",where:{");
		if(whereKey!=null){
			boolean isNext = false;
			for(int i=0;i<whereKey.length;i++){
				sb.append(isNext?",":"").append(whereKey[i]).append(":'").append(whereVal[i]).append("'");
				isNext=true;
			}
		}
		return sb.append("}}").toString();
	}

	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public int getOperater() {
		return operater;
	}
	public void setOperater(int operater) {
		this.operater = operater;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}

	public ArrayList<String> getPrimitValue(String primitKey){
		if(whereKey==null)return null;
		ArrayList<String> tmp = new ArrayList<String>();
		int idx = 0;
		for(String key : whereKey){
			if(primitKey.equalsIgnoreCase(key)){
				tmp.add(whereVal[idx]);
			}
			idx++;
		}
		return tmp;
	}

	public String toRegValueString(){
		StringBuffer sb = new StringBuffer();
		if(whereKey!=null){
			for(int i=0;i<whereKey.length;i++){
				sb.append("|").append(whereKey[i]).append("=").append(whereVal[i]);
			}
		}
		return sb.length()>1?sb.substring(1).toString():"";
	}
	public String toRegColumnString(){
		StringBuffer sb = new StringBuffer();
		if(whereKey!=null){
			for(int i=0;i<whereKey.length;i++){
				sb.append("|").append(whereKey[i]).append("=");
			}

		}
		return sb.length()>1?sb.substring(1).toString():"";
	}

	public String[] getWhereKey() {
		return whereKey;
	}

	public void setWhereKey(String[] whereKey) {
		this.whereKey = whereKey;
	}

	public String getWhereVal(int i) {
		if(whereVal==null || i >= whereVal.length)return "";
		String val = whereVal[i];
		if(val==null)return ".+";
		if(val.indexOf("|")>-1)
			val = val.replaceAll("\\|", "\\|");
		if(val.indexOf(".")>-1)
			val = val.replaceAll("\\.", "\\.");
		if(val.indexOf("+")>-1)
			val = val.replaceAll("\\+", "\\+");
		if(val.indexOf("?")>-1)
			val = val.replaceAll("\\?", "\\?");
		return val;
	}

	public void setWhereVal(String[] whereVal) {
		this.whereVal = whereVal;
	}

	public boolean isRightAnalysis(){
		return whereKey!=null && whereVal!=null;
	}


}
