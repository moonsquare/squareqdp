package com.qdevelop.core.formatter;

import java.util.HashMap;
import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.cache.CacheFactory;
import com.qdevelop.cache.clear.TableIndexs;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;

@SuppressWarnings("rawtypes")
public class TableCacheFormatter extends AbstractFormatter {
	private boolean isTableCached;
	private String cacheTableName,tableCacheConfig,targetKey; String[] targetFields;
	@Override
	public void init(InitFormatBean param) {
		tableCacheConfig = param.getConfig("table");
		if(tableCacheConfig!=null){
			targetKey = param.getConfig("targetKey") == null ? null : param.getConfig("targetKey").toUpperCase();
			targetFields = param.getConfig("fields") == null ? null : param.getConfig("fields").toUpperCase().split(",");
		}
	}

	public void initFormatter(DBStrutsBean struts){
		tempCache = new HashMap<String,HashMap>();
		if(cacheTableName == null && tableCacheConfig!=null){
			String[] item = TableIndexs.getInstance().getPrimaryItem(tableCacheConfig);
			if(item==null){
				QLog.getInstance().securityError("对不起，您配置的一级缓存表名["+tableCacheConfig+"]没有在数据库内存在！");
				return;
			}
			cacheTableName = item[0];
			if(cacheTableName == null) return;
			if(targetKey==null)targetKey=item[2].toUpperCase();
			isTableCached = true;
		}
	}
	
	private HashMap<String,HashMap> tempCache;
	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts)
			throws QDevelopException {
		if(isTableCached && data.get(targetKey) != null){
			String tempkey = new StringBuffer().append(this.cacheTableName).append(data.get(targetKey)).toString();
			@SuppressWarnings("unchecked")
			HashMap<String,Object> cache = tempCache.get(tempkey);
			if(cache==null)
				cache = CacheFactory.firstCache().get(String.valueOf(data.get(targetKey)), cacheTableName);
			if(cache!=null){
				tempCache.put(tempkey, cache);
				if(targetFields==null)
					data.putAll(cache);
				else{
					for(String key:targetFields){
						if(key.indexOf("|")==-1)
							data.put(key, cache.get(key));
						else{
							data.put(key.substring(key.indexOf("|")+1), cache.get(key.substring(0,key.indexOf("|"))));
						}
					}
				}
			}
		}
	}
	
	@Override
	public void flush(ResultBean rb) {
		tempCache.clear();
	}

}
