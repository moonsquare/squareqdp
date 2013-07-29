package com.qdevelop.core.bean;

import java.util.HashMap;

public class DBStrutsBean extends HashMap<String,DBStrutsLeaf>{
	private static final long serialVersionUID = -7390592305262986054L;
	
	public void addStruts(DBStrutsLeaf sl){
		this.put(sl.getColumnName(), sl);
	}
	
	public DBStrutsLeaf getStruts(String columnName){
		return this.get(columnName);
	}

}
