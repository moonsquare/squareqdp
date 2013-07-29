package com.qdevelop.cache.utils;

import java.util.HashMap;

public class ParserQuery extends HashMap<String,String>{
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 5676995237097494246L;

	public ParserQuery(String query){
		String[] tmp = query.replaceAll("\\{|\\}|'", "").split(",");
		for(String t:tmp){
			int i = t.indexOf(":");
			if(i<0)i = t.indexOf("=");
			if(i>0)
				this.put(t.substring(0,i), t.substring(i+1));
		}
	}
}
