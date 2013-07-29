package com.qdevelop.web.utils;

import java.io.Serializable;
import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class UrlParamSort implements Comparator,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4124473428568881752L;

	public int compare(Object o1,Object o2) {
		String A1=(String)o1;
		String A2=(String)o2;
		if(A1.compareTo(A2) > 0)
			return 1;
		else
			return 0;
	}
	
}
