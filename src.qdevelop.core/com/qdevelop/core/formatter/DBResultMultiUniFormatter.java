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
import com.qdevelop.core.sqlmodel.SQLModelParser;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.core.utils.QueryBeanFormatter;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DBResultMultiUniFormatter  extends  LoopQueryBaseformatter{

	private String targetKey,uniKey,asColumn,uniIndex;
	String[] fields;
	private HashMap<String,String> queryParam;
	Boolean isCache ;
	//	private String[] fields;
	@Override
	public void init(InitFormatBean param) {
		this.targetKey = param.getConfig("targetKey", true);
		this.uniKey = param.getLoopConfig("uniKey","targetKey");
		if(this.uniKey==null)this.uniKey = this.targetKey;
		queryParam = new HashMap<String,String>();
		queryParam.put("index", param.getConfig("uniIndex"));
		this.setIndex(param.getConfig("uniIndex"));
		uniIndex = param.getConfig("uniIndex");
		this.asColumn = param.getConfig("asColumn")==null?"asColumn":param.getConfig("asColumn");
		fields = param.getConfig("fields", "\\|", true);
		

	}
	public DBResultMultiUniFormatter(){}

	private HashMap<String,List<Integer>> condition;

	private HashMap<String,ArrayList> tempCache;
	private int idx=0;
	private String currentConfig;
	private ArrayList<String> keys;

	@Override
	public void initFormatter(DBStrutsBean struts) {
		condition = new HashMap<String,List<Integer>>(); 
		tempCache = new HashMap<String,ArrayList>();
		currentConfig = SQLModelLoader.getInstance().getConfigByIndex(queryParam.get("index"));
		if(isCache==null){
			Element e = SQLModelLoader.getInstance().getElementByIndex(uniIndex);
			if(e!=null && "true".equals(e.attributeValue("cacheAble"))){
				isCache = true;
			}else isCache = false;
		}
	}

	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts)	throws QDevelopException {
		if(data.get(targetKey)!=null){
			String joinValue = String.valueOf(data.get(targetKey));
			HashMap<String,String> query = (HashMap<String,String>) this.queryParam.clone();
			this.joinQuery(query);
			query.put(uniKey, joinValue);
			String tokenKey = tmpCacheKey(query);

			ArrayList dd = tempCache.get(tokenKey);
			if(isCache&&dd==null)
				dd = (ArrayList)CacheFactory.secondCache().get(tokenKey, currentConfig);
			if(isCache&&dd!=null){
				tempCache.put(tokenKey, dd);
				joinData(data,dd);
			}else{
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
		Iterator<Entry<String, List<Integer>>> iter = condition.entrySet().iterator();
		keys = new ArrayList<String>(condition.size());
		while(iter.hasNext()){
			String key = iter.next().getKey();
			keys.add(key);
			sb.append("|").append(key);
		}
		return sb.toString().substring(1);
	}

	@Override
	public void flush(ResultBean rb) {
		if(condition==null || condition.size()==0)return; 
		Map<String,String> query = (HashMap<String,String>)queryParam.clone();
		this.joinQuery(query);
		query.put(uniKey, getCondition());
		DBQueryBean _queryBean = SQLModelParser.getInstance().getQueryBean(query, null, null);
		if(QProperties.isDebug)System.out.println("DBResultMultiUniFormatter:\t"+condition.toString());
		_queryBean.setCacheConfig(null);//不缓存
		_queryBean.setCacheAble(false);
		Connection conn = null;
		try {
			conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();
			DBResultBean result = new DBResultBean();
			IResultFormatter[] _formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(_queryBean.getSqlIndex());
			result = (DBResultBean) QueryFromDataBaseImp.getInstance().select(_queryBean.getSQLByOrder(), conn,result, _formatter);

			if(_queryBean.getAfterRun()!=null){
				DBResultBean tmp = (DBResultBean)new QueryBeanFormatter().reflectRun(_queryBean.getAfterRun(), "disposeResultBean", new Object[]{result,_queryBean});
				if(tmp!=null){
					result = tmp;
				}
			}

			String _uniKey = uniKey.toUpperCase();
			for(int i=0;i<result.size();i++){
				Map data =result.get(i);
				String key = String.valueOf(data.get(_uniKey));
				ArrayList tmp = tempCache.get(key);
				if(tmp==null){
					tmp = new ArrayList(); 
				}
				tmp.add(data);
				tempCache.put(key, tmp);
			}
			Iterator<Entry<String, List<Integer>>> iter = condition.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, List<Integer>> cond = iter.next();
				String key = cond.getKey();
				ArrayList unionList = tempCache.get(key);
				if(unionList!=null){
					keys.remove(key);
					List<Integer> position = cond.getValue();
					for(Integer idx : position){
						joinData(rb.getResultMap(idx),unionList);
					}
					if(isCache){
						queryParam.put(uniKey, key);
						String tokenKey = tmpCacheKey(queryParam);
						CacheFactory.secondCache().add(tokenKey,new StringBuffer().append("{").append(uniKey.toUpperCase()).append(":").append(key).append("}").toString(),unionList, currentConfig);
					}
				}
			}
			if(isCache && keys.size() > 0){
				for(String key:keys){
					queryParam.put(uniKey, key);
					String tokenKey =tmpCacheKey(queryParam);
					CacheFactory.secondCache().add(tokenKey,queryParam.toString(),new ArrayList(0), currentConfig,MemCacheConfig.tmpCacheTimer);//增加一个空缓存，用1小时
				}
			}
			keys.clear();
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

		condition.clear();
		tempCache.clear();
	}

	private String tmpCacheKey(Map<String,String> query){
		return CoreFactory.toQueryKey(query, null).replace(",,", "MF");
	}

	private void joinData(Map<String,Object> data,ArrayList<Map> result){
		if(fields!=null){
			ArrayList<Map> tmp = new ArrayList(result.size());
			for(int i=0;i<result.size();i++){
				Map dd = result.get(i);
				Map t = new HashMap(fields.length);
				for(String key:fields){
					t.put(key, dd.get(key));
				}
				tmp.add(t);
			}
			data.put(asColumn, tmp);
		}else
			data.put(asColumn, result);
	}

	@Override
	public boolean isNeedStruts() {
		return false;
	}


}
