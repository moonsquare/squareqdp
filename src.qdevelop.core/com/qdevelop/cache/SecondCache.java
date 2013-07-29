package com.qdevelop.cache;

import java.io.Serializable;

import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.clear.CacheIndexsUtils;
import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.cache.interfaces.ICache;
import com.qdevelop.cache.interfaces.ITacties;
import com.qdevelop.cache.sync.RemoteSecondCacheSyncIndexs;
import com.qdevelop.core.schedule.QScheduleFactory;
import com.qdevelop.core.sqlmodel.SQLModelLoader;

public class SecondCache implements ICache{
//	private static byte[] _lock = new byte[0];
	private static final SecondCache sc = new SecondCache();
	public static final ITacties tatics = MemCacheConfig.getInstance().getSendCondCacheTactiesImpl();
	public static long autoSyncSeconds = 60;
	
	public SecondCache(){
		QScheduleFactory.getInstance().addSchedule(new Runnable(){
			public void run() {
				RemoteSecondCacheSyncIndexs.syncRemoteIndexs(SQLModelLoader.getInstance().getTable2Indexs());
			}
		}, 0, 3600);
	}

	public static SecondCache getInstance(){
		return sc;
	}

	/**
	 * 直接增加缓存，指定时长
	 * @param key
	 * @param query
	 * @param value
	 * @param config
	 * @param timer
	 */
	public void add(final String key, final String query, final Serializable value,final String config, int timer)  {
		if(value==null)return;
		try {
			if(MemCachedImpl.getInstance().add(tatics.toKey(key, config), value, timer)){
//				tatics.addKey(key, config, query);
				CacheIndexsUtils.getInstance().addChangeIndexItem(key, config, query, true);
				CacheStatus.getInstance().add(key, config);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add(final String key,final String query, final Serializable value, final String config)  {
		if(value==null)return;
		try {
			if(MemCachedImpl.getInstance().add(key, value, config, tatics)){
				//tatics.addKey(key, config, query);
//				QueueItem item = new QueueItem(key,config,query,AsynCacheQueue.ADD);
//				CasIndexArray indexCasBean = (CasIndexArray) MemCachedImpl.getInstance().get(item.stackKey(), null, null);
//				if(indexCasBean==null)indexCasBean = new CasIndexArray(item.stackKey());
//				indexCasBean.addTacties(item);
//				MemCachedImpl.getInstance().add(item.stackKey(),indexCasBean ,MemCacheConfig.ONE_DAY);
				CacheIndexsUtils.getInstance().addChangeIndexItem(key, config, query, true);
				CacheStatus.getInstance().add(key, config);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Serializable get(final String key, final String config)  {
		try {
			Serializable val = MemCachedImpl.getInstance().get(key, config, tatics);
			if(val!=null){
//				if(QProperties.isDebug)System.out.println(new StringBuffer().append("Result From Cache ===> ").append(tatics.toKey(key, config)).toString());
				//tatics.addCacheRate(key, config,null);
				CacheIndexsUtils.getInstance().addChangeIndexItem(key, config, null, true);
				CacheStatus.getInstance().cached(key, config);
			}
			return val;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean update(String key, Serializable value, String config)  {
		if(value==null)return false;
		try {
			return MemCachedImpl.getInstance().update(key, value, config,tatics);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void remove(String key, String config)  {
		try {
			MemCachedImpl.getInstance().remove(key, config, tatics);
//			tatics.delCacheRate(key, config,null);
			CacheIndexsUtils.getInstance().addChangeIndexItem(key, config, null, false);
			CacheStatus.getInstance().delete(key, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void asynUpdate(String key,String config,String query){
		tatics.updateKey(key,config,query);
	}

	public void asynRemove(String key,String config){
		tatics.removeKey(key,config);
	}

	public CasIndexArray getCacheIndexs(String key,String config) {
		try {
			return  CacheIndexsUtils.getInstance().getCasIndexArray(key, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**最大执行并发线程数**/
//	private static int MULTI_THREAD_NUM = QProperties.getInstance().getProperty("QDevelop_second_cache_thread_num")==null ? 10 : QProperties.getInstance().getInt("QDevelop_second_cache_thread_num");

	/**
	 * 
	 * TODO 级联缓存清理策略,根据SQL Config 定制 
	 * 
	 * @param relationIndexs
	 */
//	public void casRemove(String[] relationIndexs,String[] remoteTable){
//		//		synchronized(_lock){
//		HashSet<String> remoteIndexs = RemoteSecondCacheSyncIndexs.getClearIndexsCollection(remoteTable, relationIndexs);
//		if(remoteIndexs==null || remoteIndexs.size()==0)return;
//		ExecutorService exec=null;
//		try {	
//			java.util.Iterator<String> itor = remoteIndexs.iterator();
//			exec = Executors.newFixedThreadPool(MULTI_THREAD_NUM);
//			while(itor.hasNext()){
//				String index = itor.next();
//				if(index.indexOf("@")>-1){
//					exec.execute(new MultiClearCacheThread(tatics,index));
//				}
//			}
//			exec.shutdown();
//			// 等待子线程结束，再继续执行下面的代码
//			exec.awaitTermination(10, TimeUnit.SECONDS);
//		} catch (Exception e) {
//			if(exec!=null)exec.shutdown();
//			e.printStackTrace();
//		}
//		remoteIndexs.clear();
//		//		}
//	}

//	public void casRemoveOld(String[] relationIndexs,String[] remoteTable){
//		synchronized(_lock){
//			try {
//				if(relationIndexs!=null && relationIndexs.length > 0){
//					for(String t : relationIndexs){
//						if(t == null)continue;
//						int at = t.indexOf("@");
//						if(at==-1)continue;
//						String key = t.substring(0,at);
//						String config = t.substring(at+1);
//						try {
//							CasIndexArray indexCasBean = getCacheIndexs(key,config);
//							if(indexCasBean!=null){
//								for(IndexItem ki : indexCasBean.values()){
//									if(ki==null)continue;
//									if(tatics.isDeleteImmediately(ki)){
//										tatics.delCacheRate(ki.getKey(), ki.getConfig() ,null);
//										MemCachedImpl.getInstance().remove(ki.toKey(), null, null);
//										if(QProperties.isDebug)System.out.println(new StringBuffer().append("deleteImmediately:\t").append(ki.getKey()));
//									}else if(tatics.isRemoveAsynchronous(ki)){
//										tatics.removeKey(ki.getKey(), ki.getConfig());
//										if(QProperties.isDebug)System.out.println(new StringBuffer().append("removeAsynchronous:\t").append(ki.getKey()));
//									}else if(tatics.isUpdateAsynchronous(ki)){
//										tatics.updateKey(ki.getKey(), ki.getConfig(),ki.getQuery());
//										if(QProperties.isDebug)System.out.println(new StringBuffer().append("updateAsynchronous:\t").append(ki.getKey()));
//									}
//								}
//								indexCasBean.clear();
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//				if(remoteTable!=null){
//					remoteClearCache(remoteTable,relationIndexs);
//				}
//			} catch (Exception e) {
//				QLog.getInstance().systemError("系统自动缓存清理异常",e);
//				e.printStackTrace();
//			}
//		}
//	}
//
//	/**
//	 * 同步清理远程缓存 
//	 * @param remoteTable
//	 * @param relationIndexs
//	 */
//	private void remoteClearCache(String[] remoteTable,String[] relationIndexs){
//		HashSet<String> remoteIndexs = RemoteSecondCacheSyncIndexs.getRomoteClearIndexs(remoteTable, relationIndexs);
//		if(remoteIndexs==null || remoteIndexs.size()==0)return;
//		java.util.Iterator<String> itor = remoteIndexs.iterator();
//		while(itor.hasNext()){
//			String index = itor.next();
//			int at = index.indexOf("@");
//			if(at==-1)continue;
//			String key = index.substring(0,at);
//			String config = index.substring(at+1);
//			CasIndexArray indexCasBean = (CasIndexArray) MemCachedImpl.getInstance().get(CacheUtils.stackKey(key,config), null, null);
//			if(indexCasBean!=null){
//				Collection<IndexItem> coll = indexCasBean.values();
//				for(IndexItem ki : coll){
//					if(ki==null)continue;
//					/**后期尽量修改成主动更新方式**/
//					MemCachedImpl.getInstance().remove(ki.getKey(), ki.getConfig(), SecondCache.tatics);
//					if(QProperties.isDebug)System.out.println(new StringBuffer().append("remote Update:\t").append(ki.getKey()));
//				}
//				indexCasBean.clear();
//			}
//		}
//	}

	public ITacties getTacties(){
		return tatics;
	}

//	public void asynRun(){
//		tatics.asynRun();
//	}

	@Override
	public void shutdown() {
		tatics.asynRun();
	}


}
