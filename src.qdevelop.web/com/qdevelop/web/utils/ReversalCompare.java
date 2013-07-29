package com.qdevelop.web.utils;

import java.util.Comparator;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ReversalCompare implements Comparator{
	private boolean isDesc;
	private String field;
	private Boolean isNumber;
	public ReversalCompare(String order){
		isDesc = order.toUpperCase().endsWith("DESC");
		if(order.indexOf(" ")>-1){
			field = order.substring(0,order.indexOf(" "));
		}else field = order;
	}
	@Override
	public int compare(Object arg0, Object arg1) {
		Map a  = (Map)arg0;
		Map b  = (Map)arg1;
		if(a.get(field)==null)return isDesc?1:0;
		if(b.get(field)==null)return isDesc?0:1;
		if(isNumber==null){
			isNumber = a.get(field) instanceof Integer || a.get(field) instanceof Double || a.get(field) instanceof Float || a.get(field) instanceof Long;
		}
		if(isNumber){
			int r = Float.parseFloat(String.valueOf(a.get(field)))>Float.parseFloat(String.valueOf(b.get(field)))?1:0;
			if(isDesc){
				return r*-1;
			}
			return r;
		}else{
			int r = String.valueOf(a.get(field)).compareTo(String.valueOf(b.get(field)));
			if(isDesc){
				return r*-1;
			}
			return r;
		}
	}

}
