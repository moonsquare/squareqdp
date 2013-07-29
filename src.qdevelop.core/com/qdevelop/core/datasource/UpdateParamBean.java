package com.qdevelop.core.datasource;

public class UpdateParamBean{
	
	public int size;private int[] type;private Object[] values ;
	public UpdateParamBean(int size){
		this.size = size;
		type = new int[size];
		values = new Object[size];
	}
	/**整数类型**/
	public static int INT = 0;
	/**双精度类型**/
	public static int DOUBLE = 1;
	/**字符串类型**/
	public static int STRING = 2;
	/**日期类型**/
	public static int DATE = 3;
	
	
	public void addParam(int index,int type,Object value){
		this.type[index] = type;
		this.values[index] = value;
	}
	
	public int getType(int index){
		return this.type[index];
	}
	
	public int getIntValue(int index){
		return Integer.parseInt(String.valueOf(this.values[index]));
	}
	
	public String getStringValue(int index){
		return String.valueOf(this.values[index]);
	}
}
