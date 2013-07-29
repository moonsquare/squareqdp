package com.qdevelop.cache.sync;

import com.qdevelop.cache.bean.IndexItem;

public class MemCachedSyncImpl extends Thread{
	IndexItem item;int threadNum;
	public MemCachedSyncImpl(IndexItem keyItem,int threadNum){
		this.item = keyItem;
		this.threadNum = threadNum;
	}

	public void run(){
		if(item!=null)
			System.out.println(threadNum+" run:\t"+item.getQuery());
	}
}
