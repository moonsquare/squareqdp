package com.qdevelop.cache.sync;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.qdevelop.cache.bean.RemoteCasIndex;
import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.utils.QLog;

public class RemoteSecondCacheSyncIndexs {
	public static RemoteCasIndex _REMOTECASINDEX;
//	public static String _remoteSecondCacheSyncIndexsKey = "REMOTESECONDCACHESYNCINDEXSKEY";
	public static String _remoteSerialVersionUID = "RemoteSecondCacheSyncIndexsSerial";
	public static boolean isNeedSync = true;
	public static long version = 0;

	public static RemoteCasIndex getRemoteIndexs(){
		_REMOTECASINDEX = (RemoteCasIndex)MemCachedImpl.getInstance().get(_remoteSerialVersionUID,1000);
		if(_REMOTECASINDEX == null){
			_REMOTECASINDEX =  new RemoteCasIndex();
		}
		return _REMOTECASINDEX;
	}

	public static boolean isChanged(){
		long currentVersion = MemCachedImpl.getInstance().getCasVersion(_remoteSerialVersionUID);
		if(version == 0 || version != currentVersion){
			version = currentVersion;
			return true;
		}
		return false;
	}

	public static boolean saveRemoteIndexs(RemoteCasIndex _remoteCasIndex){
		return MemCachedImpl.getInstance().casUpdate(_remoteSerialVersionUID, _remoteCasIndex, MemCacheConfig.ONE_MONTH);
	}

	public static void reloadRemoteCasIndex(){
		_REMOTECASINDEX = (RemoteCasIndex)MemCachedImpl.getInstance().get(_remoteSerialVersionUID, null, null);
	}

	private static void init(){
		if(_REMOTECASINDEX==null){
			getRemoteIndexs();
		}
	}

	public static HashSet<String> getRomoteClearIndexs(String[] tables,HashSet<String> localIndexs){
		init();
		HashSet<String> remoteIndexs = new HashSet<String>();
		for(String table : tables){
			String[] indexs = _REMOTECASINDEX.get(table);
			if(indexs!=null){
				for(String idx:indexs){
					if(idx!=null && (localIndexs==null || !localIndexs.contains(idx))){
						remoteIndexs.add(idx);
					}
				}
			}
		}
		return remoteIndexs;
	}

	public static HashSet<String> getClearIndexsCollection(String[] tables,String[] localIndexs){
		init();
		HashSet<String> remoteIndexs = new HashSet<String>();
		if(tables!=null){
			for(String table : tables){
				String[] indexs = _REMOTECASINDEX.get(table);
				if(indexs!=null){
					for(String idx:indexs){
						remoteIndexs.add(idx);
					}
				}
			}
		}
		if(localIndexs!=null){
			for(String localIndex : localIndexs){
				remoteIndexs.add(localIndex);
			}
		}

		return remoteIndexs;
	}

	public static String[] getAllIndexByTable(String tableName){
		if(_REMOTECASINDEX != null){
			return _REMOTECASINDEX.get(tableName.toUpperCase());
		}
		return getRemoteIndexs().get(tableName.toUpperCase());
	}


	public static void syncRemoteIndexs(ConcurrentHashMap<String,HashSet<String>> localIndexs){
		_REMOTECASINDEX = getRemoteIndexs();
		if(localIndexs == null || localIndexs.size() == 0)return;
		Iterator<Entry<String, HashSet<String>>> itor = localIndexs.entrySet().iterator();
		int i=0;
		for(;i<10;i++){
			RemoteCasIndex rci =  getRemoteIndexs();
			while(itor.hasNext()){
				Entry<String, HashSet<String>> entry = itor.next();
				HashSet<String> tmp = new HashSet<String>();
				String[] remoteIndexs = rci.get(entry.getKey());
				if(remoteIndexs!=null){
					for(String ri : remoteIndexs){
						tmp.add(ri);
					}
				}
				HashSet<String> val = entry.getValue();
				for(String ri : val){
					tmp.add(ri);
				}
				rci.put(entry.getKey(), tmp.toArray(new String[]{}));
			}
			if(MemCachedImpl.getInstance().casUpdate(_remoteSerialVersionUID, rci, MemCacheConfig.ONE_MONTH)){
				_REMOTECASINDEX = rci;
//				if(QProperties.isDebug)System.out.println("[cache-clear] sync remote indexs");
				break;
			}
		}
		if(i==10)QLog.getInstance().systemError("remote sync error!");
	}

	//	@SuppressWarnings("rawtypes")
	//	public static void executeSchedule(ConcurrentHashMap<String, Set> table2Index){
	//		/**
	//		 * 10分钟执行一次
	//		 */
	//		QScheduleFactory.getInstance().addSchedule(new RemoteSecondCacheThread((version == 0 && table2Index !=null) ?table2Index:SQLModelLoader.getInstance().getTable2Indexs()), 60, 600);
	//	}

}
