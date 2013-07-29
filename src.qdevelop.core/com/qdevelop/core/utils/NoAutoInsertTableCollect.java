package com.qdevelop.core.utils;

import java.util.HashMap;

public class NoAutoInsertTableCollect {
	private static HashMap<String,Boolean> collect = new HashMap<String,Boolean>();
	public static void addCollect(String table){
		collect.put(table, true);
	}
	public static boolean isAutoInsert(String table){
		return collect.get(table)==null;
	}
}
