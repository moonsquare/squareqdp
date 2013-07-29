package com.qdevelop.core.utils;

import java.util.HashMap;
import java.util.Map;

import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;

public class RepeatSubmit {
	private static RepeatSubmit _RepeatSubmit = new RepeatSubmit();
	public static RepeatSubmit getInstance(){return _RepeatSubmit;}
	public RepeatSubmit(){init();}
	
	private static int repeatSleepTimer;
	private static HashMap<String,Object> indexsMonitor;
	private Boolean o= true;
	
	public boolean isRepeat(Map<String,Object> parameters){
		String index = (String)parameters.get("index");
		if(index==null || indexsMonitor == null || indexsMonitor.get(index)==null)return false;
		parameters.remove("$r");
		String key = QString.get32MD5(parameters.toString());
		if(MemCachedImpl.getInstance().get(key,null,null)!=null)return true;
		MemCachedImpl.getInstance().add(key, o, repeatSleepTimer);	
		return false;
	}
	
	public void addIndexMonitor(String index){
		if(indexsMonitor == null)indexsMonitor = new HashMap<String,Object>();
		indexsMonitor.put(index, o);
	}
	
	public void init(){
		repeatSleepTimer = QProperties.getInstance().getProperty("repeat_sleep_timer")== null
				?	300	:	QProperties.getInstance().getInt("repeat_sleep_timer");
		if(QProperties.getInstance().getProperty("repeat_index_monitor")!=null){
			indexsMonitor = new HashMap<String,Object>();
			String[] indexs = QProperties.getInstance().getProperty("repeat_index_monitor").replaceAll(" ", "").split(",|\\|");
			
			for(String idx:indexs){
				indexsMonitor.put(idx, o);
			}
		}
	}
	
//	public static void main(String[] args) {
//		Map<String,Object> query = new HashMap<String,Object>();
//		query.put("index", "queryTest");
//		query.put("s", "3");
//		System.out.println(RepeatSubmit.getInstance().isRepeat(query));
//		CacheFactory.shutdown();
//	}
}
