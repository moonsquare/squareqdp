package com.qdevelop.core.datasource;

import com.qdevelop.cache.clear.ClearCacheQueue;

public class CaseClearCache extends Thread{
	String[] relationIndexs, remoteTable, sqls,firstRemoteTable;
	public CaseClearCache(String[] relationIndexs, String[] remoteTable,String[] sqls,String[] firstRemoteTable){
		this.relationIndexs = relationIndexs;
		this.remoteTable = remoteTable;
		this.sqls = sqls;
		this.firstRemoteTable = firstRemoteTable;
	}
	
	public void run(){
		ClearCacheQueue.getInstance().addCasSqls(sqls);
//		CacheFactory.secondCache().casRemove(relationIndexs,remoteTable);
//		CacheFactory.firstCache().casRemove(sqls,firstRemoteTable);
	}
}
