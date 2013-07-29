package com.qdevelop.core.formatter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Element;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.cache.CacheFactory;
import com.qdevelop.cache.implments.MemCacheConfig;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.cache.MapCache;

@SuppressWarnings("rawtypes")
public class DBResultKeyValueFormatter  extends  LoopQueryBaseformatter{
	private String cacheIndex, cacheKey, cacheValue, targetKey, 
	targetName,tableConfigName,reName,whereKey,defaultVal;
	Boolean isCache ;
	private String[] fields;
	@Override
	public void init(InitFormatBean param) {
		this.cacheIndex = param.getConfig("cacheIndex");
		this.setIndex(param.getConfig("cacheIndex"));
		this.cacheKey = param.getConfig("cacheKey",true);
		this.cacheValue = param.getConfig("cacheValue",true);
		this.targetKey = param.getLoopConfig("targetKey","join","uniKey");
		if(this.targetKey!=null)this.targetKey = this.targetKey.toUpperCase();
		this.targetName = param.getConfig("targetName",true);
		this.fields = param.getConfig("fields",",|\\|",true);
		this.reName = param.getConfig("reName");// param[6]==null?"RE_":param[4];
		if(this.reName==null)this.reName = "RE_";
		this.whereKey = param.getConfig("whereKey");// param[7]==null?this.cacheKey:param[7];
		if(this.whereKey==null)this.whereKey = this.cacheKey;
		this.defaultVal = param.getConfig("default");

	}

	public DBResultKeyValueFormatter(){}

	private HashMap<String,List<Integer>> condition;
	private HashMap<String,HashMap> tempCache;
	private int idx=0;
	private String currentConfig;
	private ArrayList<String> keys;

	@Override
	public void formatter(Map<String,Object> data,DBStrutsBean struts) throws QDevelopException {
		if(data.get(targetKey)!=null ){
			String joinValue = String.valueOf(data.get(targetKey));
			@SuppressWarnings("unchecked")
			HashMap<String,String> query = (HashMap<String,String>) this.query.clone();
			this.joinQuery(query);
			query.put("index", cacheIndex);
			query.put(whereKey, joinValue);
			String tokenKey = tmpCacheKey(query);
			HashMap dd = tempCache.get(tokenKey);			
			if(isCache && dd==null)
				dd = (HashMap)CacheFactory.secondCache().get(tokenKey, currentConfig);

			if(isCache && dd!=null ){
				tempCache.put(tokenKey, dd);
				joinData(data,dd);
			}else{
				if(this.targetName!=null&&this.defaultVal!=null){
					data.put(this.targetName, this.defaultVal);
				}
				List<Integer> ids = condition.get(joinValue);
				if(ids==null){
					ids = new ArrayList<Integer>();
				}
				ids.add(idx);
				condition.put(joinValue,ids);
			}
		}
		idx++;
	}

	private String  getCondition(){
		StringBuffer sb = new StringBuffer();
		keys = new ArrayList<String>(condition.size());
		Iterator<Entry<String, List<Integer>>> iter = condition.entrySet().iterator();
		while(iter.hasNext()){
			String key = iter.next().getKey();
			keys.add(key);
			sb.append("|").append(key);
		}
		return sb.toString().substring(1);
	}

	@Override
	public void flush(ResultBean rb) {
		if(condition.size()==0)return;
		if(QProperties.isDebug)System.out.println(new StringBuffer().append("DBResultKeyValueFormatter [").append(cacheIndex).append("] (").append(tableConfigName).append(") - ").append(condition).toString());
		this.joinQuery(query);
		query.put("index", cacheIndex);
		query.put(whereKey, getCondition());
		DBQueryBean _queryBean = CoreFactory.getInstance().getQueryBean(query);
		_queryBean.setCacheAble(false);
		Connection conn = null;
		try {
			conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();
			DBResultBean result = new DBResultBean();
			IResultFormatter[] _formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(_queryBean.getSqlIndex());
			result = (DBResultBean) QueryFromDataBaseImp.getInstance().select(_queryBean.getSQLByOrder(), conn,result, _formatter);
			for(int i=0;i<result.size();i++){
				Map data =result.get(i);
				String valuekey = String.valueOf(data.get(whereKey));
				keys.remove(valuekey);
				List<Integer> indexs = condition.get(valuekey);
				if(indexs!=null){
					for(Integer idx:indexs){
						joinData(rb.getResultMap(idx),data);
					}
				}

				if(isCache){
					query.put("index", cacheIndex);
					query.put(whereKey, valuekey);
					String tokenKey = tmpCacheKey(query);
					CacheFactory.secondCache().add(tokenKey,new StringBuffer().append("{").append(whereKey.toUpperCase()).append(":").append(valuekey).append("}").toString(),(HashMap)data, _queryBean.getCacheConfig());
				}
			}
			if(isCache && keys.size() > 0){
				for(String key:keys){
					query.put("index", cacheIndex);
					query.put(whereKey, key);
					String tokenKey = tmpCacheKey(query);
					CacheFactory.secondCache().add(tokenKey,query.toString(),new HashMap(0), _queryBean.getCacheConfig(),MemCacheConfig.tmpCacheTimer);//增加一个空缓存，用1小时
				}
			}

			QLog.getInstance().sqlLogger(_queryBean);
		} catch (QDevelopException e) {
			QLog.getInstance().sqlError(_queryBean);
			throw e;
		}finally{
			try {
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		keys.clear();
		query.clear();
		_queryBean.clearQueryData();
		condition.clear(); 
		tempCache.clear();
	}

	@SuppressWarnings({ "unchecked" })
	private void joinData(Map<String, Object> data,Map joinTableData){
		if(joinTableData == null){
			
			return;
		}
		if(cacheValue==null){
			if(this.fields==null){
				Iterator iter = joinTableData.entrySet().iterator(); 
				while(iter.hasNext()){
					Map.Entry<String, Object> entry = (Map.Entry)iter.next();
					if(data.get(entry.getKey())==null){
						data.put(entry.getKey(),entry.getValue());
					}else{ 
						data.put(new StringBuffer().append(this.reName).append(entry.getKey()).toString(),entry.getValue());
					}
				}
			}else{
				for(String field:fields){
					if(joinTableData.get(field)!=null)
						data.put(field, data.get(field)==null?joinTableData.get(field):new StringBuffer().append(this.reName).append(joinTableData.get(field)).toString());
				}
			}
		}else{
			Object val = joinTableData.get(cacheValue);
			if(val==null && this.defaultVal!=null){
				val=this.defaultVal;
			}
			data.put(targetName == null?MapCache.getInstance().getFormatterKey(targetKey):targetName, val);
		}
	}

	private String tmpCacheKey(Map<String,String> query){
		return CoreFactory.toQueryKey(query, null).replace(",,", "KV");
	}

	private HashMap<String,String> query;

	@Override
	public void initFormatter(DBStrutsBean struts) {
		condition = new HashMap<String,List<Integer>>(); 
		query = new HashMap<String,String>();
		tempCache = new HashMap<String,HashMap>();
		currentConfig = SQLModelLoader.getInstance().getConfigByIndex(cacheIndex);
		idx=0;
		if(isCache==null){
			Element e = SQLModelLoader.getInstance().getElementByIndex(cacheIndex);
			if(e!=null && "true".equals(e.attributeValue("cacheAble"))){
				isCache = true;
			}else isCache = false;
		}
	}

	@Override
	public boolean isNeedStruts() {
		return false;
	}


}
