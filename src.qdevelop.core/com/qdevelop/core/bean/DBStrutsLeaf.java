package com.qdevelop.core.bean;

public class DBStrutsLeaf {
	String columnName,columnTypeName;
	int size;
	boolean isNullAble,isAutoIncrement;
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnTypeName() {
		return columnTypeName;
	}
	public void setColumnTypeName(String columnTypeName) {
		this.columnTypeName = columnTypeName;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public boolean isNullAble() {
		return isNullAble;
	}
	public void setNullAble(boolean isNullAble) {
		this.isNullAble = isNullAble;
	}
	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}
	public void setAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}
	
}
