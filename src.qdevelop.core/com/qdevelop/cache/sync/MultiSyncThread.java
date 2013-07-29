package com.qdevelop.cache.sync;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.bean.QueueItem;
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
import com.qdevelop.utils.QProperties;

public class MultiSyncThread implements Runnable{
	CasIndexArray indexCasBean;
	ITacties tatics;
	QueueItem item;
	
	public MultiSyncThread(QueueItem item,CasIndexArray indexCasBean,ITacties tatics){
		this.tatics = tatics;
		this.indexCasBean = indexCasBean;
		this.item = item;
	}
	
	public void run(){
		try {
			
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
				if(ki.getQuery()!=null){
					Connection conn=null;
					try {
						if(QProperties.isDebug)System.out.println(new StringBuffer().append("asyn update:\t").append(ki.toString()));
						DBResultBean rb = new DBResultBean();
						Map<String,String> query = new ParserQuery(ki.getQuery());
						DBQueryBean _queryBean = CoreFactory.getInstance().getQueryBean(query);
						conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();
						IResultFormatter[] _formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(_queryBean.getSqlIndex());
						if(_queryBean.isPagination()){
							if(_queryBean.allCount < 1){
								Integer allCount = null;
								if(_queryBean.isCacheAble()){
									allCount = (Integer)MemCachedImpl.getInstance().get(_queryBean.getCountCacheKey(), _queryBean.getCacheConfig(), tatics);
								}
								if(allCount == null){
									allCount = Integer.parseInt( QueryFromDataBaseImp.getInstance().selectSingle(_queryBean.getSQLByCount(), conn, null).get("CN").toString());
									MemCachedImpl.getInstance().set(_queryBean.getCountCacheKey(), allCount, _queryBean.getCacheConfig(), tatics);
								}
								_queryBean.allCount = allCount;
							}
							if(rb instanceof IPagination)
								((IPagination)rb).initPagination(_queryBean.getNowPage(),_queryBean.getMaxNum(),_queryBean.allCount);
							 QueryFromDataBaseImp.getInstance().select(_queryBean.getSQLByOrder(), conn,rb,_queryBean.getNowPage(),_queryBean.getMaxNum(), _formatter,ConnectFactory.getDatabase(_queryBean.getConnect()).equals("MYSQL"));
						}else{
							 QueryFromDataBaseImp.getInstance().select(_queryBean.getSQLByOrder(), conn,rb, _formatter);
						}
						MemCachedImpl.getInstance().set(_queryBean.getCacheKey(), rb, _queryBean.getCacheConfig(), tatics);
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						try {
							if(conn!=null){
								conn.close();
							}
						} catch (Exception e) {
						}
					}
				}else{//query 为null时 是异常情况的cache 需要清理
					MemCachedImpl.getInstance().remove(item.getKey(), item.getConfig(), tatics);
					indexCasBean.removeItem(item.getKey(), item.getConfig());
				}
				break;
			case 3://DELETE
				MemCachedImpl.getInstance().remove(item.getKey(), item.getConfig(), tatics);
				indexCasBean.addItemRate(item,-1);
//				indexCasBean.removeItem(item.getKey(), item.getConfig());
				break;
			case 4://ADDRATE
				indexCasBean.addItemRate(item,1);
				break;
			case 5://DELRATE
				indexCasBean.addItemRate(item,-1);
				break;
			}
			long currentTime = System.currentTimeMillis();
			
			/**清理部分不常用的索引**/
			ConcurrentHashMap<String,IndexItem> temp = new ConcurrentHashMap<String,IndexItem>(indexCasBean);
			Iterator<IndexItem> itor = temp.values().iterator();
			while(itor.hasNext()){
				IndexItem ii = itor.next();
				if(!ii.isCached() && (ii.getQuery() == null || ii.getQuery().indexOf("{")==-1 || (currentTime - ii.getLastTime() > 600000))){
					indexCasBean.removeItem(ii.getKey(), ii.getConfig());
				}
			}
			temp.clear();
		
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
