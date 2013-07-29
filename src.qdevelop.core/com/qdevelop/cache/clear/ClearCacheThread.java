package com.qdevelop.cache.clear;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.qdevelop.cache.FirstCache;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.sync.RemoteSecondCacheSyncIndexs;
import com.qdevelop.cache.utils.CacheUtils;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

/**
 * 清理本系统相关缓存
 * @author Janson
 *
 */
public class ClearCacheThread implements Runnable{
	public static Logger log = QLog.getInstance().getLog("asynClean");
	String tableName;
	ArrayList<SQLClearBean> sqlBeans;
	Pattern regValue;
	Pattern regColumn;
	Pattern caseColumnDelete;
	public static int MULTI_THREAD_NUM = QProperties.getInstance().getProperty("ClearCacheThread.maxThread")==null ? 3 : QProperties.getInstance().getInt("ClearCacheThread.maxThread");

	public ClearCacheThread(String tableName,ArrayList<SQLClearBean> sqlBeans){
		this.tableName = tableName;
		this.sqlBeans = sqlBeans;
	}

	public void run(){
		long s = System.currentTimeMillis();
		StringBuffer sb = new StringBuffer();
		sb.append("clear - [").append(tableName).append("] ");

		HashSet<String> temp = new HashSet<String>();
		HashSet<String> tempColumn = new HashSet<String>();

		ExecutorService exec=null;
		exec = Executors.newFixedThreadPool(MULTI_THREAD_NUM);

		/**一级缓存清理**/
		int idx=0;
		String primitKey = TableIndexs.getInstance().getPrimaryKey(tableName);
		for(SQLClearBean scb : sqlBeans){
			if(primitKey!=null){/**一级缓存清理**/
				ArrayList<String> vals = scb.getPrimitValue(primitKey);
				if(vals!=null){/**值中含有主键值**/
					for(String val:vals){
						idx++;
						FirstCache.getInstance().remove(val, tableName);
						debug("[first cache]",tableName,":",val);
					}
					vals.clear();
				}else{
					if(scb.getOperater()!=SQLClearBean.INSERT)
						exec.execute(new FirstCacheClearQueue(scb.getSql(),primitKey,scb.getTableName(),TableIndexs.getInstance().getConnect(tableName)));
				}
			}
			if(scb.isRightAnalysis()){/**二级缓存清理准备**/
				for(int i=0;i<scb.getWhereKey().length;i++){
					temp.add(new StringBuffer().append(scb.getWhereKey()[i]).append("=").append(scb.getWhereVal(i)).toString());
					tempColumn.add(scb.getWhereKey()[i]);
				}
			}else{
				log.fatal(scb.getSql());
			}
		}

		StringBuffer regStr = new StringBuffer();
		if(temp.size()>0){
			for(String r:temp){
				regStr.append("|").append(r);
			}
			regValue = Pattern.compile(regStr.substring(1).toString());
			debug("[table]",tableName,"[args] :",regStr.substring(1).toString());
		}
		if(tempColumn.size()>0){
			StringBuffer reg = new StringBuffer();
			for(String r:tempColumn){
				reg.append("|").append(r);
			}
			regColumn = Pattern.compile(reg.substring(1).toString());
			caseColumnDelete = Pattern.compile("("+reg.substring(1).toString()+")=(!|>|<|%|.+\\|).+(,|\\})");
		}


		if(idx>0)sb.append(" firstCache:").append(idx);

		if(regColumn == null || regValue == null){
			for(SQLClearBean scb : sqlBeans){
				log.warn("[clear-cache] sqlBean:"+scb.toString());
			}
			return;
		}

		try{
			idx=0;
			/**二级缓存清理**/
			HashSet<String> indexs =  SQLModelLoader.getInstance().getClearIndexByTableName(tableName);
			if(indexs != null){
				debug("[local indexs]",indexs.toString());
				for(String clearIndex : indexs){
					int at = clearIndex.indexOf("@");
					String key = clearIndex.substring(0,at);
					String config = clearIndex.substring(at+1);

					CasIndexArray indexCasBean = CacheIndexsUtils.getInstance().getTempCasIndexArray(key, config);
					if(indexCasBean!=null){
						idx += indexCasBean.size();
						exec.execute(new ClearCacheItemThread(regStr.toString(),"local_temp",indexCasBean,regValue,regColumn,caseColumnDelete,null,false));
					}
					String _cacheKey =  CacheUtils.stackKey(key, config);
					if(!NoCacheIndexs.getInstance().hasCacheIndexs(_cacheKey))continue;
					CasIndexArray cacheIndexCasBean = CacheIndexsUtils.getInstance().getCasIndexArrayFromMemcache(_cacheKey);
					if(cacheIndexCasBean!=null){
						idx += cacheIndexCasBean.size();
						exec.execute(new ClearCacheItemThread(regStr.toString(),"local_cache",cacheIndexCasBean,regValue,regColumn,caseColumnDelete,indexCasBean,true));
					}else{
						NoCacheIndexs.getInstance().addNoCacheIndexs(_cacheKey);
					}

				}
			}

			String[] remoteIndexs  = RemoteSecondCacheSyncIndexs.getAllIndexByTable(tableName);
			if(remoteIndexs!=null && remoteIndexs.length>0){
				debug("[remote indexs]",remoteIndexs);
				for(String clearIndex : remoteIndexs){
					if(indexs!=null && indexs.contains(clearIndex))continue;
					int at = clearIndex.indexOf("@");
					String key = clearIndex.substring(0,at);
					String config = clearIndex.substring(at+1);
					String _cacheKey =  CacheUtils.stackKey(key, config);
					if(!NoCacheIndexs.getInstance().hasCacheIndexs(_cacheKey))continue;
					CasIndexArray cacheIndexCasBean = CacheIndexsUtils.getInstance().getCasIndexArrayFromMemcache(_cacheKey);
					if(cacheIndexCasBean!=null){
						idx += cacheIndexCasBean.size();
						exec.execute(new ClearCacheItemThread(regStr.toString(),"remote_cache",cacheIndexCasBean,regValue,regColumn,caseColumnDelete,null,true));
					}else{
						NoCacheIndexs.getInstance().addNoCacheIndexs(_cacheKey);
					}
				}
			}
			exec.shutdown();
			exec.awaitTermination(60, TimeUnit.SECONDS);

			if(idx>0)sb.append(" secondCache:").append(idx);
			sb.append(" from:").append(temp.toString()).append(" use:").append(System.currentTimeMillis()-s).append("ms");
			log.info(sb.toString());
			tempColumn.clear();
			temp.clear();
			sqlBeans.clear();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void debug(String s,String ... args){
		if(QProperties.isDebug){
			StringBuffer sb = new StringBuffer();
			sb.append(QDate.getNow("yyyy-MM-dd HH:mm:ss")).append(" [cache-clear] (").append(QDevelopConstant.SYSTEM_NAME).append(") ").append(s).append(":");
			if(args != null){
				for(String a:args){
					sb.append(" ").append(a);
				}
			}
			System.out.println(sb.toString());
		}
	}
}
