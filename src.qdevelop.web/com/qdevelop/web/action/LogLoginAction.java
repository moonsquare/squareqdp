package com.qdevelop.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.web.handler.LogLoader;
import com.qdevelop.web.handler.LogLoginParser;

@SuppressWarnings({"unchecked","rawtypes"})
public class LogLoginAction extends QDevelopAction{
	private static final long serialVersionUID = 2348695460987237330L;

	private int total;
	private List rows;

	public String execute() throws QDevelopException{
		Map<String,String> query = this.getParamMap();
		
		LogLoginParser parser = (LogLoginParser) LogLoader.getInstance().loadLog(new LogLoginParser(query.get("system"),query.get("user"),query.get("date"),query.get("rows")==null?10:Integer.parseInt(query.get("rows"))));
		total = parser.getMax();
		rows = parser;
		return SUCCESS;
	}

	public static void main(String[] args) {
		Map<String,String>  query = new HashMap();
		query.put("system", ".+");
		LogLoginParser parser = (LogLoginParser) LogLoader.getInstance().loadLog(new LogLoginParser(query.get("system"),query.get("user"),query.get("date"),query.get("rows")==null?10:Integer.parseInt(query.get("rows"))));
		for(Map data:parser)
			System.out.println(data);
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

}
