package com.qdevelop.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class IDGenerator extends ConcurrentLinkedQueue<String>{
	private static IDGenerator _IDGenerator = new IDGenerator();
	public static IDGenerator getInstance(){
		return _IDGenerator;
	}
	private int size = 0;
	public String seqID(){
		if(size == 0)generator();
		return this.poll();
	}
	
	public boolean offer(String id){
		++size;
		return super.offer(id);
	}
	public String poll(){
		--size;
		return super.poll();
	}
	
	private synchronized void generator(){
		
	}
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = -6769746398812302188L;
	
//	private static char[] seeds = "0123456789abcdefghigklmnpqrstuvwxyABCDEFGHIGKLMNOPQRSTUVWXYZ-".toCharArray();
	
	
	/**
	 * TODO （描述方法的作用） 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
