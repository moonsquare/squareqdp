package com.qdevelop.cache.clear;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.qdevelop.cache.CasSaveCache;
import com.qdevelop.cache.SecondCache;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.cache.interfaces.ITacties;
import com.qdevelop.cache.utils.CacheUtils;
import com.qdevelop.core.schedule.ISchedule;
import com.qdevelop.core.schedule.QScheduleFactory;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QString;

/**
 * 优化缓存索引，将1M以上的索引数据分段存放，
 * 多级索引数据合并读取工具类
 * 主进程记录临时的索引队列
 * @author Janson
 *
 */
public class CacheIndexsUtils extends ConcurrentLinkedQueue<IndexItem>{
	private static final long serialVersionUID = 6100104768434634318L;
	public static Logger log = QLog.getInstance().getLog("system");
	private static CacheIndexsUtils _CasIndexArrayUtils = new CacheIndexsUtils();
	public static CacheIndexsUtils getInstance(){
		return _CasIndexArrayUtils;
	}
	public CacheIndexsUtils(){
		QScheduleFactory.getInstance().addSchedule(new ISchedule(){
			public void run() {
				CacheIndexsUtils.getInstance().syncAddCasIndexArray();
				CacheIndexsUtils.getInstance().sync();
			}
		}, 60, 30);

		/**设置第二天凌晨2点开始清理缓存**/
		GregorianCalendar tomorrow = new GregorianCalendar();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 2);
		tomorrow.set(Calendar.MINUTE, 0);
		QScheduleFactory.getInstance().addSchedule(new ISchedule(){
			public void run() {
				CacheIndexsUtils.getInstance().systemCacheIndexsClean();
			}
		}, tomorrow.getTime(), 86400);
	}
	/**
	 * 所有动态变更的索引存放处
	 */
	private ConcurrentHashMap<String,CasIndexArray> tempCache = new ConcurrentHashMap<String,CasIndexArray>();
	public static int maxSize = 500;//超过1K索引时，自动分多列存储
	public static int maxWaitTimer = MemCacheConfig.getInstance().maxWaitTimer;
	private boolean isRunning;

	public CasIndexArray getCasIndexArray(String key,String config){
		return getCasIndexArrayFromMemcache(CacheUtils.stackKey(key, config));
	}


	public CasIndexArray getTempCasIndexArray(String key,String config){
		return tempCache.get(CacheUtils.stackKey(key, config));
	}
	/**
	 * 
	 * @param key
	 * @param config
	 * @param isAddRate true：增加一次操作 false:删除索引 
	 */
	public void addChangeIndexItem(String key,String config,String query,boolean isAddRate){
		IndexItem _ii = new IndexItem(key,config,query,CacheUtils.stackIndex(key),CacheUtils.stackKey(key, config));
		_ii.setCached(isAddRate);
		this.add(_ii);
		if(!isRunning){
			new Thread(){
				public void run(){
					CacheIndexsUtils.getInstance().syncAddCasIndexArray();
				}
			}.start();
		}

	}



	public boolean syncAddCasIndexArray(){
		if(isRunning || this.isEmpty())return false;
		isRunning = true;
		ConcurrentHashMap<String,HashMap<String,IndexItem>> temp = new ConcurrentHashMap<String,HashMap<String,IndexItem>>();
		ConcurrentHashMap<String,CasIndexArray> tmpCasIndexArray = new ConcurrentHashMap<String,CasIndexArray>();
		try {
			while(!this.isEmpty()){
				IndexItem ii = this.poll();
				if(ii!=null){
					if(!ii.isCached()){
						addChangeIndexItem(ii);
						continue;
					}
					CasIndexArray cia = tmpCasIndexArray.get(ii.stackKey());
					if(cia==null){
						cia = getCasIndexArrayFromMemcache(ii.stackKey());
						if(cia!=null)tmpCasIndexArray.put(ii.stackKey(), cia);
					}
					if(cia==null || cia.findKeyItem(ii.getKey(), ii.getConfig())==null){
						String key = new StringBuffer().append(ii.getPureIndex()).append(ii.getConfig()).toString();
						HashMap<String,IndexItem> iis = temp.get(key);
						if(iis==null){
							iis = new HashMap<String,IndexItem>();
							temp.put(key, iis);
						}
						iis.put(ii.toKey(), ii);
					}else{
						ii = cia.findKeyItem(ii.getKey(), ii.getConfig());
						ii.addRate(1);
						addChangeIndexItem(new IndexItem(ii));
					}
				}
			}
			tmpCasIndexArray.clear();
			for(HashMap<String,IndexItem> iis : temp.values()){
				int i=0;
				CasIndexArray indexCasBean=null;
				for(;i<=10;i++){
					for(IndexItem ii : iis.values()){
						indexCasBean = tmpCasIndexArray.get(ii.stackKey());
						if(indexCasBean==null){
							indexCasBean = getCasIndexArrayFromMemcache(ii.stackKey());
							if(indexCasBean!=null)tmpCasIndexArray.put(ii.stackKey(), indexCasBean);
						}
						if(indexCasBean==null)indexCasBean = new CasIndexArray(ii.stackKey());
						indexCasBean.mergeIndexItem(ii);
					}
					if(indexCasBean!=null && MemCachedImpl.getInstance().casUpdate(indexCasBean.getStackKey(), indexCasBean, MemCacheConfig.ONE_DAY)){
						if(i>1)System.out.println("try sync indexs ["+indexCasBean.getStackKey()+"] "+(i-1));
						break;
					}
					try {
						Thread.sleep(10*i);
					} catch (Exception e) {
					}
				}
				if(indexCasBean!=null && i>10)QLog.getInstance().systemWarn("try sync indexs ["+indexCasBean.getStackKey()+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		temp.clear();
		temp=null;
		tmpCasIndexArray.clear();
		tmpCasIndexArray=null;
		isRunning = false;
		return true;
	} 

	public void addChangeIndexItem(IndexItem ii){
		CasIndexArray cia = tempCache.get(ii.stackKey());
		if(cia==null){
			cia = new CasIndexArray(ii.stackKey());
			tempCache.put(ii.stackKey(), cia);
		}
		cia.put(ii.toKey(), ii);
	}

	private CasIndexArray getCasIndexArray(String key){
		Serializable val = MemCachedImpl.getInstance().get(key,maxWaitTimer);
		if(val==null)return null;
		//		if(val instanceof HashMap){
		//			CasIndexArray cia = new CasIndexArray(key);
		//			for(IndexItem ii : ((HashMap<String,IndexItem>)val).values()){
		//				cia.put(ii.toKey(), ii);
		//			}
		//			return cia;
		//		}
		//		if(val instanceof ConcurrentHashMap){
		//			ConcurrentHashMap<String,IndexItem> chm = (ConcurrentHashMap<String,IndexItem>)val;
		//			return chm.size()>0?(CasIndexArray)val:null;
		//		}
		return (CasIndexArray)val;
	}

	/**
	 * 直接使用服务器的数据
	 * @param stackKey
	 * @return
	 */
	public CasIndexArray getCasIndexArrayFromMemcache(String stackKey){
		if(stackKey == null)return null;
		CasIndexArray indexCasBean = getCasIndexArray(stackKey);
		if(indexCasBean == null )return null;
		//		indexCasBean.modifyTime = System.currentTimeMillis();
		try{
			if(indexCasBean.nextStackKey!=null ){
				loopGetCasIndexArray(indexCasBean,indexCasBean.nextStackKey);
			}
		}catch(Exception e){

		}
		return indexCasBean;
	}

	private void loopGetCasIndexArray(CasIndexArray indexCasBean,String nextStackKey){
		if(nextStackKey == null)return;
		CasIndexArray tmp  = (CasIndexArray) getCasIndexArray(nextStackKey);
		if(tmp!=null){
			for(IndexItem ii : tmp.values()){
				indexCasBean.put(ii.toKey(), ii);
			}
			if(tmp.nextStackKey!=null){
				loopGetCasIndexArray(indexCasBean,tmp.nextStackKey);
			}
		}
	}

	private void loopRemoveCasIndexArray(String nextStackKey){
		if(nextStackKey == null)return;
		CasIndexArray tmp  = (CasIndexArray) getCasIndexArray(nextStackKey);
		if(tmp!=null){
			MemCachedImpl.getInstance().remove(tmp.getStackKey());
			if(tmp.nextStackKey!=null){
				loopRemoveCasIndexArray(tmp.nextStackKey);
			}
			tmp.clear();
		}
	}

	int maxLen = 1024000;
	public void setCasIndexArrayToCache(final CasIndexArray indexCasBean){
		if(indexCasBean==null)return;
		if(indexCasBean.size() < maxSize){
			new CasSaveCache(indexCasBean.getStackKey(),indexCasBean).start();
			return;
		}
		
		
		
		CasIndexArray tmp = null;
		String nextStackKey = indexCasBean.getStackKey();
		int i=0;
		for(IndexItem ii : indexCasBean.values()){
			if(i%maxSize==0){
				if(tmp==null){
					tmp = new CasIndexArray(indexCasBean.getStackKey());
				}else{
					tmp.nextStackKey  = QString.get32MD5(new StringBuffer().append(indexCasBean.getStackKey()).append(System.currentTimeMillis()).toString());
					new CasSaveCache(tmp.getStackKey(),tmp).start();
					nextStackKey = tmp.nextStackKey;
					tmp = new CasIndexArray(indexCasBean.getStackKey());
				}
			}
			tmp.put(ii.toKey(), ii);
			i++;
		}
		if(tmp.size()>0){
			new CasSaveCache(nextStackKey,tmp).start();
		}
	}

	public void remove(String key,String config){
		String stackKey = CacheUtils.stackKey(key, config);
		CasIndexArray indexCasBean = getCasIndexArray(stackKey);
		if(indexCasBean == null )return;
		MemCachedImpl.getInstance().remove(indexCasBean.getStackKey());
		try{
			if(indexCasBean.nextStackKey!=null){
				loopRemoveCasIndexArray(indexCasBean.nextStackKey);
			}
		}catch(Exception e){

		}
		indexCasBean.clear();
	}

	public void sync(){
		if(tempCache.size()==0)return;
		boolean isSuccessful = false;
		java.util.Iterator<CasIndexArray> itor = tempCache.values().iterator();
		while(itor.hasNext()){
			CasIndexArray tmp = itor.next();
			if(tmp!=null){
				int i=0;
				for(;i<=10;i++){
					CasIndexArray indexCasBean = getCasIndexArrayFromMemcache(tmp.getStackKey());
					if(indexCasBean==null)indexCasBean = new CasIndexArray(tmp.getStackKey());
					for(IndexItem _ii : tmp.values()){
						indexCasBean.mergeIndexItem(_ii);
					}

					/**索引超长时,主动清理cache**/
//					if(indexCasBean.size()>maxSize*5){
//						IndexItem[] arrayOfObject = indexCasBean.values().toArray(new IndexItem[] {});
//						Arrays.sort(arrayOfObject, new CasIndexSort());
//						for (int j = maxSize*4; j < arrayOfObject.length; j++) {
//							indexCasBean.removeItem(arrayOfObject[j].getKey(), arrayOfObject[j].getConfig());
//							MemCachedImpl.getInstance().remove(tatics.toKey(arrayOfObject[j].getKey(), arrayOfObject[j].getConfig()));
//						}
//					}

					if(indexCasBean.size() > 0 && MemCachedImpl.getInstance().casUpdate(tmp.getStackKey(), indexCasBean, MemCacheConfig.ONE_MONTH)){
						if(i>1)
							System.out.println("try sync indexs ["+tmp.getStackKey()+"] "+(i-1));
						isSuccessful = true;
						break;
					}
					try {
						Thread.sleep(10*i);
					} catch (Exception e) {
					}
				}
				if(i>10)QLog.getInstance().systemWarn("try sync indexs ["+tmp.getStackKey()+"]");
			}
		}
		if(isSuccessful)
			tempCache.clear();
	}

	public static ITacties tatics = SecondCache.getInstance().getTacties();
	/**
	 * 系统级缓存清理
	 */
	public void systemCacheIndexsClean(){
		ConcurrentHashMap<String,HashSet<String>> all = SQLModelLoader.getInstance().getTable2Indexs();
		if(all==null)return;
		for(HashSet<String> indexs : all.values()){
			if(indexs == null )continue;
			for(String index : indexs){
				CasIndexArray indexCasBean = getCasIndexArrayFromMemcache(CacheUtils.getStackIndexFromKeyConfig(index));
				if(indexCasBean == null || System.currentTimeMillis() - indexCasBean.modifyTime < 43200000 )continue;
				@SuppressWarnings("unchecked")
				Map<String,IndexItem> tmp = (HashMap<String,IndexItem>)indexCasBean.clone();
				for(IndexItem _ii : tmp.values()){
					if(System.currentTimeMillis() - _ii.getLastTime() > 86400000){
						indexCasBean.removeItem(_ii.getKey(), _ii.getConfig());
						MemCachedImpl.getInstance().remove(tatics.toKey(_ii.getKey(), _ii.getConfig()));
						log.info("[CasIndexArray] delete cache "+_ii.toString());
					}
				}
				tmp.clear();
				indexCasBean.modifyTime = System.currentTimeMillis();
				setCasIndexArrayToCache(indexCasBean);
			}
		}
	}

	public void printTemp(){
		for(CasIndexArray cia  : tempCache.values()){
			for(IndexItem ii : cia.values()){
				System.out.println("temp ==> "+ii.toString());
			}
		}
	}
}
