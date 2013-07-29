package com.qdevelop.cache.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * <tableName,casIndexs>
 * @author Janson
 *
 */
public class RemoteCasIndex extends HashMap<String,String[]> implements Serializable{

	private static final long serialVersionUID = 5992026105971037132L;
	
	/**
	 * 将本地索引和远程索引进行合并处理
	 * @param table
	 * @param localIndexs
	 */
	public void addCasIndex(String table,String[] localIndexs){
		String[] remote = this.get(table);
		if(remote != null){	
			HashSet<String> tmp = new HashSet<String>();
			for(String r:remote){
				tmp.add(r);
			}
			for(String i:localIndexs){
				tmp.add(i);
			}
			this.put(table, tmp.toArray(new String[]{}));
		}else{
			this.put(table, localIndexs);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addCasIndex(String table,HashSet<String> localIndexs){
		String[] remote = this.get(table);
		if(remote != null){	
			HashSet<String> tmp = (HashSet<String>)localIndexs.clone();
			for(String r:remote){
				tmp.add(r);
			}
			this.put(table, tmp.toArray(new String[]{}));
		}else{
			this.put(table, localIndexs.toArray(new String[]{}));
		}
	}
	
	/**
	 * 获取远程所有的请求的index的值，进行级联更新清理
	 * @param table
	 * @param localIndexs
	 * @return
	 */
	public String[] getRemoteIndexs(String table,String[] localIndexs){
		String[] remoteIndexs = this.get(table);
		if(remoteIndexs == null)return null;
		HashSet<String> tmp = new HashSet<String>();
		for(String i:localIndexs){
			tmp.add(i);
		}
		for(int i=0;i<remoteIndexs.length;i++){
			if(tmp.contains(remoteIndexs[i])){
				remoteIndexs[i] = null;
			}
		}
		return remoteIndexs;
	}
}
