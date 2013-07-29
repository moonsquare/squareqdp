package com.qdevelop.utils.cache;

import java.util.LinkedList;
import java.util.Queue;

public class SyncQueue {
	private static Queue<String> clearQueue = new LinkedList<String>();
	
	public static boolean add(String key){
		return clearQueue.offer(key);
	}
	
	@SuppressWarnings("unused")
	public static void clearCache(){
		String key;
		while((key=clearQueue.poll())!=null){
			
		}
	}
}
