package com.qdevelop.cache.clear;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.qdevelop.cache.SecondCache;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.utils.CLang;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

public class ClearCacheItemThread implements Runnable{
//	public static final Pattern 
	String regInfo;
	String info;
	CasIndexArray indexCasBean;
	CasIndexArray preViewIndex;
	Pattern regValue;
	Pattern regColumn;
	Pattern caseColumnDelete;
	boolean isClear;

	public static Logger log = QLog.getInstance().getLog("asynClean");

	public ClearCacheItemThread(String regInfo,String info,CasIndexArray indexCasBean,Pattern regValue,Pattern regColumn,Pattern caseColumnDelete,CasIndexArray preViewIndex,boolean isClear){
		this.indexCasBean = indexCasBean;
		this.regValue = regValue;
		this.regColumn = regColumn;
		this.caseColumnDelete = caseColumnDelete;
		this.info = info;
		this.preViewIndex=preViewIndex;
		this.regInfo = regInfo;
		this.isClear = isClear;
	}

	@Override
	public void run() {
		if(indexCasBean==null || indexCasBean.size()==0)return;
		int size=0;
		StringBuffer logInfo = new StringBuffer();
		if(QProperties.isDebug)logInfo.append("\r\n").append("[cache-clear] [run]:=====start=====>").append(info).append("\r\n[cache-clear] [Rule] ").append(regInfo);
		IndexItem[] itors = indexCasBean.values().toArray(new IndexItem[]{});
		for(IndexItem ki : itors){
			if( ki==null || (preViewIndex!=null && preViewIndex.get(ki.toKey())!=null) )continue;
			if(isMiniCache(ki.getConfig())){
				SecondCache.getInstance().remove(ki.getKey(), ki.getConfig());
				continue; 
			}
			if(ki.getQuery() == null || "null".equals(ki.getQuery())){
				SecondCache.getInstance().remove(ki.getKey(), ki.getConfig());
				if(QProperties.isDebug)logInfo.append("\r\n").append("[cache-clear] [run]:").append(info).append(" del null:\t").append(ki.toString());
				continue;
			}
			if(regColumn!=null && regValue!=null){
				String queryString = ki.getQuery().replaceAll("'", "").replaceAll(":", "=");
				/**精准匹配的立即删除缓存**/
				if(regValue.matcher(queryString).find()){
					ki.setCached(false);
					SecondCache.getInstance().remove(ki.getKey(), ki.getConfig());
					if(QProperties.isDebug)logInfo.append("\r\n").append("[cache-clear] [run]:").append(info).append(" del accurate:\t[").append(ki.getConfig()).append("] ").append(ki.getQuery());
				}else if(caseColumnDelete.matcher(queryString).find()){/**精准匹配到列，并且列中的值含有模糊搜索内容的，立即删除**/
					ki.setCached(false);
					SecondCache.getInstance().remove(ki.getKey(), ki.getConfig());
					if(QProperties.isDebug)logInfo.append("\r\n").append("[cache-clear] [run]:").append(info).append(" del vague:\t[").append(ki.getConfig()).append("] ").append(ki.getQuery());
				}else if( !isLongCache(ki.getConfig()) && !regColumn.matcher(queryString).find()){/**非长缓存策略的，并且没有匹配到请求列的，立即删除**/
					ki.setCached(false);
					SecondCache.getInstance().remove(ki.getKey(), ki.getConfig());
					if(QProperties.isDebug)logInfo.append("\r\n").append("[cache-clear] [run]:").append(info).append(" del vague:\t[").append(ki.getConfig()).append("] ").append(ki.getQuery());
				}else{
					++size;
					if(QProperties.isDebug)System.out.println(new StringBuffer().append(QDate.getNow("yyyy-MM-dd HH:mm:ss")).append(" [cache-clear] no delete:").append(ki.toString()));
				}
			}else{
				ki.setCached(false);
				SecondCache.getInstance().remove(ki.getKey(), ki.getConfig());
				if(QProperties.isDebug)logInfo.append("\r\n").append("[cache-clear] [run]:").append(info).append(" del no reg:\t[").append(ki.getConfig()).append("] ").append(ki.getQuery());
			}
		}
		itors=null;
		if(QProperties.isDebug)log.debug(logInfo.append("\r\n").append("[cache-clear] [run]:=====end======>").append(info).append("\t"));
		if(size>0)log.warn(new StringBuffer().append("[cache-clear] [").append(indexCasBean.getStackKey()).append("] no delete:").append(size).append(" with:").append(regInfo.toString()));
		if(isClear)indexCasBean.clear();
	}

	private boolean isLongCache(String config){
		return config.length()>2 && config.substring(0,2).equals(CLang.LONG_CACHE_PREFIX);
	}
	
	/**
	 * OA后台管理类缓存，全部清理索引队列
	 * @param config
	 * @return
	 */
	private boolean isMiniCache(String config){
		return config.length()>2 && config.substring(0,2).equals(CLang.MINI_CACHE_PREFIX);
	}

}
