package com.qdevelop.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.cache.interfaces.ICache;
import com.qdevelop.cache.tactics.FirstTactiesImpl;
import com.qdevelop.lang.QDevelopException;

public class FirstCache implements ICache{
	private static FirstCache _FirstCache = new FirstCache();
	public static FirstCache getInstance(){return _FirstCache;}
//	public FirstCache(){
//		QScheduleFactory.getInstance().addSchedule(new ISchedule(){
//			public void run() {
//				FirstCache.getInstance().asynRun();
//			}
//		}, 60, 60);
//	}

	private static FirstTactiesImpl tatics = new FirstTactiesImpl();
	
	public String toKey(String key,String config){
		return tatics.toKey(key, config);
	}
	
	

	@Override
	public void add(String key, String query, Serializable value, String config) throws QDevelopException {
		if(value==null)return;
		try {
			MemCachedImpl.getInstance().add(key, value, config, tatics);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String,Object> get(String key, String config) throws QDevelopException {
		if(key==null || config == null)return null;
		try {
			HashMap<String,Object> val = (HashMap<String,Object>)MemCachedImpl.getInstance().get(tatics.toKey(key, config),1000);
			if(val == null){
				val = tatics.getVal(key, config);
				if(val==null){
					val = new HashMap<String,Object>(0);
					MemCachedImpl.getInstance().add(tatics.toKey(key, config),val,MemCacheConfig.tmpCacheTimer);//非正常数据缓存一小时
				}else{
					MemCachedImpl.getInstance().add(tatics.toKey(key, config),val,MemCacheConfig.ONE_DAY);
				}
			}
			return (HashMap<String,Object>)val;
		} catch (Exception e) {
			throw new QDevelopException(e);
		}
	}
	
	
	/**
	 * 判断一级缓存是否存在 
	 * @param key
	 * @param config
	 * @return
	 */
	public boolean isExist(String key, String config){
		try {
			Map<String,Object> t = this.get(key, config);
			if(t==null || t.size() == 0)return false;
			return true;
		} catch (QDevelopException e) {
			return false;
		}
	}

	/**
	 * 
	 * TODO （描述方法的作用） 
	 * 
	 * @param keys
	 * @param config
	 * @return
	 * @throws QDevelopException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> gets(String[] keys,String config) throws QDevelopException {
		if(keys==null || config == null || keys.length == 0)return null;
		ArrayList<Map<String,Object>> values = new ArrayList<Map<String,Object>>(keys.length);
		ArrayList<String> kkk = null;
		HashMap<String,Object> data;
		try {
			for(int i=0;i<keys.length;i++){
				data = (HashMap<String,Object>)MemCachedImpl.getInstance().get(keys[i], config, tatics);
				if(data == null){
					if(kkk == null)kkk = new ArrayList<String>();
					kkk.add(keys[i]);
					values.add(null);
				}else{
					values.add(i, data);
				}
			}
			if(kkk!=null && kkk.size()>0){
				HashMap<String,HashMap<String,Object>> result = tatics.getVals(config, kkk);
				for(int i=0;i<keys.length;i++){
					if(values.get(i) == null){
						data = result.get(keys[i]);
						if(data==null){
							data =  new HashMap<String,Object>(0);
							MemCachedImpl.getInstance().add(tatics.toKey(keys[i], config),data,MemCacheConfig.tmpCacheTimer);
						}else{
							MemCachedImpl.getInstance().add(tatics.toKey(keys[i], config),data,MemCacheConfig.ONE_DAY);
						}
						values.set(i, data);
					}
				}
				kkk.clear();
			}
			return values;
		} catch (Exception e) {
			throw new QDevelopException(e);
		}
	}
	@Override
	public boolean update(String key, Serializable value, String config) throws QDevelopException {
		if(value==null)return false;
		try {
			return MemCachedImpl.getInstance().update(key, value, config,tatics);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void remove(String key, String config) throws QDevelopException {
		try {
			MemCachedImpl.getInstance().remove(key, config, tatics);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public void casRemove(String[] updateSqls,String[] remoteTable) {
//		if(updateSqls!=null){
//			for(String _t : updateSqls){
//				if(_t==null)continue;
//				TableCacheItem tci = tatics.getTableCacheItem(_t);
//				if(tci.isTableCache()){
//					if(QProperties.isDebug)System.out.println(new StringBuffer().append("Clear Cache ").append(tci.getTableName()).append("[").append(tci.getUniKey()).append(" = ").append(tci.getUniValue()).append("]"));
//					this.remove(tci.getUniValue(), tci.getTableName());
//				}
//			}
//		}
//		if(remoteTable!=null){
//			for(String _t : remoteTable){
//				if(_t==null)continue;
//				TableCacheItem tci = tatics.getTableCacheItem(_t);
//				if(tci.isTableCache()){
//					if(QProperties.isDebug)System.out.println(new StringBuffer().append("Clear Cache ").append(tci.getTableName()).append("[").append(tci.getUniKey()).append(" = ").append(tci.getUniValue()).append("]"));
//					this.remove(tci.getUniValue(), tci.getTableName());
//				}
//			}
//		}
//	}

//	public void asynRun(){
//		tatics.asynRun();
//	}

	@Override
	public void shutdown() {
		tatics.asynRun();
	}

}
