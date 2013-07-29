package com.qdevelop.cache.sync;

import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.cache.interfaces.ITacties;
import com.qdevelop.cache.utils.CacheUtils;
import com.qdevelop.utils.QProperties;

public class MultiClearCacheThread implements Runnable{
	ITacties tatics;
	String clearIndex;
	public MultiClearCacheThread(ITacties tatics,String clearIndex){
		this.tatics = tatics;
		this.clearIndex = clearIndex;
	}

	public void run(){
		int at = clearIndex.indexOf("@");
		String key = clearIndex.substring(0,at);
		String config = clearIndex.substring(at+1);
		try {
//			long currentTime = System.currentTimeMillis();
//			long over_date = MemCacheConfig.ONE_DAY * 1000;
			CasIndexArray indexCasBean = (CasIndexArray) MemCachedImpl.getInstance().get(CacheUtils.stackKey(key, config), null, null);
			if(indexCasBean!=null){
				for(IndexItem ki : indexCasBean.values()){
					/**一天以上的缓存，非cache状态，不处理**/
					if(ki==null )continue;
					if(tatics.isDeleteImmediately(ki)){
						tatics.delCacheRate(ki.getKey(), ki.getConfig() ,null);
						MemCachedImpl.getInstance().remove(ki.toKey(), null, null);
						if(QProperties.isDebug)System.out.println(new StringBuffer().append("deleteImmediately:\t").append(ki.getKey()));
					}else if(tatics.isRemoveAsynchronous(ki)){
						tatics.removeKey(ki.getKey(), ki.getConfig());
						if(QProperties.isDebug)System.out.println(new StringBuffer().append("removeAsynchronous:\t").append(ki.getKey()));
					}else if(tatics.isUpdateAsynchronous(ki)){
						tatics.updateKey(ki.getKey(), ki.getConfig(),ki.getQuery());
						if(QProperties.isDebug)System.out.println(new StringBuffer().append("updateAsynchronous:\t").append(ki.getKey()));
					}
				}
				indexCasBean.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
