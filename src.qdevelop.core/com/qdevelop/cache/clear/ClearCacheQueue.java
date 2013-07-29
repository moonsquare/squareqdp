package com.qdevelop.cache.clear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.qdevelop.cache.SecondCache;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.sync.RemoteSecondCacheSyncIndexs;
import com.qdevelop.cache.utils.CacheUtils;
import com.qdevelop.core.schedule.QScheduleFactory;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

public class ClearCacheQueue  extends ConcurrentLinkedQueue<SQLClearBean>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static boolean isRunning = false;
	public static Logger log = QLog.getInstance().getLog("asynClean");
	private static ClearCacheQueue _ClearCacheQueue = new ClearCacheQueue();
	public ClearCacheQueue(){
		QScheduleFactory.getInstance().addSchedule(new Runnable(){
			public void run() {
				if(ClearCacheQueue.getInstance().aync()){
					System.out.println("[ClearCacheQueue] back up running...");
				}
			}
		}, 30, 10);

		QScheduleFactory.getInstance().addSchedule(new Runnable(){
			public void run() {
				RemoteSecondCacheSyncIndexs.reloadRemoteCasIndex();
			}
		}, 0, 60);
	}

	public static ClearCacheQueue getInstance(){
		return _ClearCacheQueue;
	}

	/**
	 * 指定查询索引清理cache
	 * @param indexs
	 */
	public void addClearIndexs(String ... indexs){
		ExecutorService exec = Executors.newFixedThreadPool(ClearCacheThread.MULTI_THREAD_NUM);
		for(String index : indexs){
			String config = SQLModelLoader.getInstance().getConfigByIndex(index);
			CasIndexArray indexCasBean = CacheIndexsUtils.getInstance().getCasIndexArray(index, config);
			for(IndexItem ki : indexCasBean.values()){
				if(ki.getQuery().indexOf("xxxxx")>-1){
					SecondCache.getInstance().remove(ki.getKey(), ki.getConfig());
				}
			}
		}
	}

	public void addCasSqls(String ... sqls){
		for(String sql : sqls){
			SQLClearBean sqlBean = new SQLClearBean(sql);
			if(!sqlBean.isRightAnalysis()){
				log.error(new StringBuffer().append("[sql-analysis]").append(sqlBean.toString()).append(" -- ").append(sql).toString());
			}else{
				this.offer(sqlBean);
			}
		}
		if(!isRunning){
			new Thread(){
				public void run(){
					ClearCacheQueue.getInstance().aync();
				}
			}.start();
		}
	}
	private int maxThread = QProperties.getInstance().getProperty("ClearCacheQueue.maxThread") == null ? 2 : Integer.parseInt(QProperties.getInstance().getProperty("ClearCacheQueue.maxThread"));

	public boolean aync(){
		if(isRunning || this.isEmpty())return false;
		isRunning = true;
		ExecutorService exec=null;
		StringBuffer sb = new StringBuffer();
		sb.append("[ClearCacheQueue] - ");
		try {
			long s = System.currentTimeMillis();
			exec = Executors.newFixedThreadPool(maxThread);
			HashMap<String,ArrayList<SQLClearBean>> collect = new HashMap<String,ArrayList<SQLClearBean>>();
			int idx=0;
			while(!this.isEmpty() && idx < 500){
				SQLClearBean scBean = this.poll();
				if(scBean!=null){
					ArrayList<SQLClearBean> tmp = collect.get(scBean.getTableName());
					if(tmp == null){
						tmp = new ArrayList<SQLClearBean>();
						collect.put(scBean.getTableName(), tmp);
					}
					tmp.add(scBean);
					idx++;
				}
			}

			Iterator<Entry<String, ArrayList<SQLClearBean>>> itor = collect.entrySet().iterator();
			while(itor.hasNext()){
				Entry<String, ArrayList<SQLClearBean>> item = itor.next();
				exec.execute(new ClearCacheThread(item.getKey(),item.getValue()));
				sb.append("|").append(item.getKey());
			}

			exec.shutdown();
			// 等待子线程结束，再继续执行下面的代码
			exec.awaitTermination(120, TimeUnit.SECONDS);
			isRunning = false;
			log.info(sb.append(" use:").append(System.currentTimeMillis()-s).append("ms").toString());
			collect.clear();
			//			ayscRunning.info();
		} catch (InterruptedException e) {
			isRunning = false;
			if(exec!=null)exec.shutdown();
			e.printStackTrace();
		}
		return true;
	}

}
