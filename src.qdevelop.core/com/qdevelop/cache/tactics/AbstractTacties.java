package com.qdevelop.cache.tactics;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.qdevelop.cache.bean.AsynCacheQueue;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.CasIndexSort;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.bean.QueueItem;
import com.qdevelop.cache.clear.CacheIndexsUtils;
import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.cache.interfaces.ITacties;
import com.qdevelop.cache.utils.ParserQuery;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.standard.IPagination;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QProperties;

public abstract class AbstractTacties implements ITacties{
	public static long ONEDAY = 86400000;
	private static int limitSize = 1000;
	private static byte[] _lock = new byte[0];
	private static final AsynCacheQueue indexBeanQueue = new AsynCacheQueue();

	@Override
	public void addKey(String key, String config, String query)  {
		indexBeanQueue.offer(key,config,query,AsynCacheQueue.ADD);
		if(indexBeanQueue.size() > limitSize)this.asynRun();
	}
	@Override
	public void updateKey(String key, String config,String query) {
		indexBeanQueue.offer(key,config,query,AsynCacheQueue.UPDATE);
		if(indexBeanQueue.size() > limitSize)this.asynRun();
	}

	@Override
	public void addCacheRate(String key, String config,String query) {
		indexBeanQueue.offer(key,config,query,AsynCacheQueue.ADDRATE);
		if(indexBeanQueue.size() > limitSize)this.asynRun();
	}
	@Override
	public void delCacheRate(String key, String config, String query) {
		indexBeanQueue.offer(key,config,query,AsynCacheQueue.DELRATE);
		if(indexBeanQueue.size() > limitSize)this.asynRun();
	}

	@Override
	public void removeKey(String key, String config) {
		indexBeanQueue.offer(key,config,null,AsynCacheQueue.DELETE);
		if(indexBeanQueue.size() > limitSize)this.asynRun();
	}


	@Override
	public String toKey(String key, String config) {
		if(config==null) return key;
		return new StringBuffer().append(config).append(key).toString();
	}

	public void printQueue(){
		System.out.println("==================printQueue start===============");
		for(Iterator<QueueItem> i = indexBeanQueue.iterator(); i.hasNext();)    { 
			QueueItem item = i.next(); 
			System.out.println(item.getOper()+"\t"+item.getKey());
		} 
		System.out.println("================== printQueue end ===============");

	}
	@Override
	public void asynRun(){
		if(indexBeanQueue.size() == 0)return;
		synchronized(_lock){
			boolean isPrint = false;
			int getTimes = 0;
			StringBuffer info = new StringBuffer();
			long start = System.currentTimeMillis();
			info.append("[").append(QDate.getNow("yyyy-MM-dd HH:mm:ss")).append("] ==> synchronization \tqueue size:").append(indexBeanQueue.size());
			HashMap<String,CasIndexArray> tmp = new HashMap<String,CasIndexArray>();
			QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
			try {

				while(!indexBeanQueue.isEmpty()){
					QueueItem item = indexBeanQueue.poll();
					try {
						CasIndexArray indexCasBean = tmp.get(item.stackKey());
						if(indexCasBean==null) {
							++getTimes;
							indexCasBean =  CacheIndexsUtils.getInstance().getCasIndexArrayFromMemcache(item.stackKey());
									//(CasIndexArray) MemCachedImpl.getInstance().get(item.stackKey(), null, null);
						}
						if( item.getOper() != 1 && indexCasBean == null)continue;

						switch( item.getOper()){
						case 1://ADD
							if(indexCasBean == null) indexCasBean = new  CasIndexArray(item.stackKey());
							indexCasBean.addTacties(item);
							break;
						case 2://UPDATE
							IndexItem ki  = indexCasBean.findKeyItem(item);
							if(ki==null){
								indexCasBean.addTacties(item);
								break;
							}
							if(ki.getQuery()!=null && ki.getQuery().indexOf("{")>-1){
								if(QProperties.isDebug)System.out.println(new StringBuffer().append("asyn update:\t").append(ki.toString()));
								DBResultBean rb = new DBResultBean();
								Map<String,String> query = new ParserQuery(ki.getQuery());
								DBQueryBean _queryBean = CoreFactory.getInstance().getQueryBean(query);
								
								Connection conn=null;
								try {
									conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();
									IResultFormatter[] _formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(_queryBean.getSqlIndex());
									if(_queryBean.isPagination()){
										if(_queryBean.allCount < 1){
											Integer allCount = null;
											if(_queryBean.isCacheAble()){
												allCount = (Integer)MemCachedImpl.getInstance().get(_queryBean.getCountCacheKey(), _queryBean.getCacheConfig(), this);
											}
											if(allCount == null){
												allCount = Integer.parseInt(queryFromDataBaseImp.selectSingle(_queryBean.getSQLByCount(), conn, null).get("CN").toString());
												MemCachedImpl.getInstance().set(_queryBean.getCountCacheKey(), allCount, _queryBean.getCacheConfig(), this);
											}
											_queryBean.allCount = allCount;
										}
										if(rb instanceof IPagination)
											((IPagination)rb).initPagination(_queryBean.getNowPage(),_queryBean.getMaxNum(),_queryBean.allCount);
										queryFromDataBaseImp.select(_queryBean.getSQLByOrder(), conn,rb,_queryBean.getNowPage(),_queryBean.getMaxNum(), _formatter,ConnectFactory.getDatabase(_queryBean.getConnect()).equals("MYSQL"));
									}else{
										queryFromDataBaseImp.select(_queryBean.getSQLByOrder(), conn,rb, _formatter);
									}
									MemCachedImpl.getInstance().set(_queryBean.getCacheKey(), rb, _queryBean.getCacheConfig(), this);
									
								} catch (Exception e) {
									MemCachedImpl.getInstance().remove(item.getKey(), item.getConfig(), this);
									indexCasBean.removeItem(item.getKey(), item.getConfig());
									e.printStackTrace();
								}finally{
									if(conn!=null){
										try {
											conn.close();
										} catch (Exception e) {
										}
									}
								}
							}else{//query 为null时 是异常情况的cache 需要清理
								MemCachedImpl.getInstance().remove(item.getKey(), item.getConfig(), this);
								indexCasBean.removeItem(item.getKey(), item.getConfig());
							}
							break;
						case 3://DELETE
							MemCachedImpl.getInstance().remove(item.getKey(), item.getConfig(), this);
							indexCasBean.addItemRate(item,-1);
							//							indexCasBean.removeItem(item.getKey(), item.getConfig());
							break;
						case 4://ADDRATE
							indexCasBean.addItemRate(item,1);
							break;
						case 5://DELRATE
							indexCasBean.addItemRate(item,-1);
							break;
						}
						/**清理部分不常用的索引**/

						if(indexCasBean.size() > 800){
							IndexItem[] arrayOfObject = indexCasBean.values().toArray(new IndexItem[]{});
							Arrays.sort(arrayOfObject, new CasIndexSort());
							int i = arrayOfObject.length-1;
							int l = i - 400;
							long currentTime = System.currentTimeMillis();
							long one_day = MemCacheConfig.ONE_DAY*1000;
							for(;i>l;i--){
								IndexItem ii = arrayOfObject[i];
								if(!ii.isCached()){
									indexCasBean.removeItem(ii.getKey(), ii.getConfig());
								}else if(currentTime - ii.getLastTime() > one_day ){
									MemCachedImpl.getInstance().remove(this.toKey(ii.getKey(), ii.getConfig()));
									indexCasBean.removeItem(ii.getKey(), ii.getConfig());
									if(indexCasBean.size() < 200)break;
								}
							}
						}else{
							long currentTime = System.currentTimeMillis();
							long one_day = MemCacheConfig.ONE_DAY*1000;
							ConcurrentHashMap<String,IndexItem> temp = new ConcurrentHashMap<String,IndexItem>(indexCasBean);
							Iterator<IndexItem> itor = temp.values().iterator();
							while(itor.hasNext()){
								IndexItem ii = itor.next();
								if(!ii.isCached() && (ii.getCacheTimes() < -5 || (currentTime - ii.getLastTime() > one_day))){
									indexCasBean.removeItem(ii.getKey(), ii.getConfig());
								}
							}
							temp.clear();

						}
						tmp.put(item.stackKey(), indexCasBean);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				info.append("\tRemote get:").append(getTimes).append("\tRemote set:").append(tmp.size());
				
				isPrint = tmp.size() > 0;
				Iterator<Entry<String,CasIndexArray>> itor = tmp.entrySet().iterator();
				while(itor.hasNext()){ 
					Entry<String,CasIndexArray> bean = itor.next();
					if(bean.getValue()!=null){
						CacheIndexsUtils.getInstance().setCasIndexArrayToCache(bean.getValue());
//						int i=1;
//						for(;i<=10;i++){
//							if(MemCachedImpl.getInstance().casUpdate(bean.getKey(), bean.getValue(), MemCacheConfig.ONE_DAY)){
//								if(i>1)System.out.println("try sync indexs ["+bean.getKey()+"] "+(i-1));
//								break;
//							}
//							try {
//								Thread.sleep(10*i);
//							} catch (Exception e) {
//							}
//						}
//						if(i>10)QLog.getInstance().systemWarn("try sync indexs ["+bean.getKey()+"]");
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				tmp.clear();
			}
			info.append("\t Used time:").append(System.currentTimeMillis()-start);
			if(isPrint)System.out.println(info);
		}
	}
}
