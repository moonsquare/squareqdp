package com.qdevelop.cache.sync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.qdevelop.cache.bean.RemoteCasIndex;
import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.core.schedule.ISchedule;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

@SuppressWarnings("rawtypes")
public class RemoteSecondCacheThread implements ISchedule{

	private HashMap<String,HashSet> allLocalTableIndexs;
	public RemoteSecondCacheThread(HashMap<String,HashSet> allLocalTableIndexs){
		this.allLocalTableIndexs = allLocalTableIndexs;
	}

	@SuppressWarnings({ "unchecked" })
	public void run(){
		if(allLocalTableIndexs == null || allLocalTableIndexs.size() == 0)return;
		try {
			int idx = 0;
			RemoteCasIndex _RemoteCasIndex;
			while(true && QProperties.globeCache){
				_RemoteCasIndex = (RemoteCasIndex)MemCachedImpl.getInstance().get(RemoteSecondCacheSyncIndexs._remoteSerialVersionUID, null, null);
				if(_RemoteCasIndex == null){
					_RemoteCasIndex =  new RemoteCasIndex();
					MemCachedImpl.getInstance().add(RemoteSecondCacheSyncIndexs._remoteSerialVersionUID, _RemoteCasIndex, MemCacheConfig.ONE_MONTH);
				}
				Iterator<Entry<String, HashSet>> itor = allLocalTableIndexs.entrySet().iterator();
				while(itor.hasNext()){
					Entry<String, HashSet> e = itor.next();
					_RemoteCasIndex.addCasIndex(e.getKey(), (HashSet)e.getValue());
				}
				if(_RemoteCasIndex.size() == 0)break;
				if(MemCachedImpl.getInstance().casUpdate(RemoteSecondCacheSyncIndexs._remoteSerialVersionUID, _RemoteCasIndex, MemCacheConfig.ONE_MONTH)){
					RemoteSecondCacheSyncIndexs._REMOTECASINDEX = _RemoteCasIndex;
					QLog.getInstance().systemLogger("sync RemoteSecondCache once!");
					break;
				}
				if(idx > 20){
					QLog.getInstance().systemWarn("sync RemoteSecondCache failed!!!");
					break;
				}
				try {
					Thread.sleep(10*idx);
				} catch (Exception e) {
				}
			}
			if(idx>1)	QLog.getInstance().systemWarn("try sync RemoteSecondCache "+(idx++));
		} catch (QDevelopException e) {
			e.printStackTrace();
		}
	}

}
