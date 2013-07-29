package com.qdevelop.core;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.cache.CacheFactory;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.DataBaseFactory;
import com.qdevelop.core.schedule.QScheduleFactory;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.sqlmodel.SQLModelParser;
import com.qdevelop.core.standard.IUpdateHook;
import com.qdevelop.core.utils.QueryBeanFormatter;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.CMDParser;
import com.qdevelop.utils.QVerifyAbstract;
import com.qdevelop.utils.cache.EHCache;

/**
 * Core请求中心
 * @author Janson.Gu
 *
 */
@SuppressWarnings({ "unchecked","rawtypes" })
public class CoreFactory extends QVerifyAbstract implements Cloneable{
	private static CoreFactory cd = new CoreFactory();
	public static CoreFactory getInstance() throws QDevelopException{
		try {
			return (CoreFactory)cd.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 根据sql 创建QueryBean
	 * @param sql
	 * @return
	 */
	public  DBQueryBean createQueryBean(String sql){
		DBQueryBean qb = new DBQueryBean();
		qb.setSql(sql);
		return qb;
	}
	
	public  DBQueryBean createQueryBean(String sql,String connect){
		DBQueryBean qb = new DBQueryBean();
		qb.setSql(sql);
		qb.setConnect(connect);
		return qb;
	}
	/**
	 * 根据SQL 创建带分页 QueryBean
	 * @param sql
	 * @param page
	 * @param maxNum
	 * @return
	 */
	public  DBQueryBean createQueryBean(String sql,String connect,int page,int maxNum){
		DBQueryBean qb = new DBQueryBean();
		qb.setConnect(connect);
		qb.setSql(sql);
		qb.setNowPage(page);
		qb.setMaxNum(maxNum);
		return qb;
	}

	/**
	 * 由请求String 创建QueryBean
	 * @param queryParam
	 * @return
	 * @throws QDevelopException
	 */
	public  DBQueryBean getQueryBean(String queryParam) throws QDevelopException{
		return getQueryBean(queryParam,null);
	}

	/**
	 * 由请求String 带权限 创建QueryBean
	 * @param queryParam
	 * @param rids
	 * @return
	 * @throws QDevelopException
	 */
	public  DBQueryBean getQueryBean(String queryParam,SecurityUserBean sub) throws QDevelopException{
		if(queryParam.indexOf("=")==-1)queryParam = new StringBuffer().append("index=").append(queryParam).toString();
		Map<String,String> param = new CMDParser(queryParam).getParamers();
		return getQueryBean(param, sub);
	}

	/**
	 * 由请求Map 创建QueryBean
	 * @param queryParam
	 * @return
	 * @throws QDevelopExceptions
	 */
	public  DBQueryBean getQueryBean(Map<String,String> queryParam) throws QDevelopException{
		return getQueryBean(queryParam,null);
	}


	/**
	 * 带权限的创建QueryBean
	 * @param queryParam
	 * @param sub
	 * @return
	 * @throws QDevelopException
	 */
	public  DBQueryBean getQueryBean(Map<String,String> queryParam,SecurityUserBean sub) throws QDevelopException{
		return getQueryBean(queryParam,sub,null);
	}
	
	public  DBQueryBean getQueryBean(Map<String,String> queryParam,SecurityUserBean sub,Map<String,Object> session) throws QDevelopException{
		new QueryBeanFormatter().executeFormatQueryParam(queryParam, sub,session);
		String[] rids = sub==null?null:new String[]{sub.getRoles(),sub.getSelfRole(),sub.getLoginName(),sub.id};
		return getQueryBean(queryParam,toQueryKey(queryParam,rids), rids);
	}

	/**
	 * 
	 * @param queryParam
	 * @param cacheKey
	 * @param rids
	 * @return
	 * @throws QDevelopException
	 */
	public  DBQueryBean getQueryBean(Map<String,String> queryParam,String cacheKey,String[] rids) throws QDevelopException{
		if(queryParam.get("index")==null)throw new QDevelopException("对不起，配置请求数据库，必须设定index值！");
		String modelKey = cacheKey.replaceAll("\\(.+\\)|\\^.+\\^", "");
		DBQueryBean queryBean;
		DBQueryBean queryBeanCache = (DBQueryBean)EHCache.getInstance().getCache(modelKey, QDevelopConstant.CACHE_NAME_SQLMODEL_TATICS);
		if(queryBeanCache!=null){
			queryBean = queryBeanCache.clone();
			queryBean.setCacheKey(cacheKey);
			if(queryParam.get("allCount")!=null)queryBean.allCount =  Integer.parseInt(queryParam.get("allCount"));
			else queryBean.allCount =  0;
//			if(queryParam.get("index")!=null)queryBean.sqlIndex = queryParam.get("index");
			queryBean.setNowPage(queryParam.get("page")==null?0:Integer.parseInt(queryParam.get("page")));
			queryBean.setMaxNum(queryParam.get("maxNum")==null?0:Integer.parseInt(queryParam.get("maxNum")));
			if(queryParam.get("cacheAble")!=null)queryBean.isCacheAble = Boolean.parseBoolean(queryParam.get("cacheAble"));
			queryBean.setOrder(queryParam.get("order"));
			StringBuffer roleInfo = new StringBuffer();
			if(rids!=null){
				roleInfo.append("^").append(rids[0]);
				if(rids.length>2){
					roleInfo.append(rids[2]);
					queryBean.user = rids[2];
					if(rids.length>3)queryBean.userId = rids[3];
				}
				roleInfo.append("^");
				if(queryBean.isAuthorize){
					if(queryBean.isSelect)
						queryBean.userRoleId = rids[0];
					else
						queryBean.userRoleId = rids[1];
					queryBean.setCacheKey(queryBean.getCacheKey().replaceAll("\\^.+\\^", roleInfo.toString()));
				}
			}
			if(!queryBean.isAuthorize)queryBean.setCacheKey(cacheKey.replaceAll("\\^.+\\^", ""));
			queryBean.data = queryBean.clearBaseInfo(queryParam);
		}else{
			queryBean = SQLModelParser.getInstance().getQueryBean(queryParam, cacheKey, rids);
			if(queryBean.isSelect())//只针对查询的SQL初始化可以进行缓存
				EHCache.getInstance().setCache(modelKey, queryBean, QDevelopConstant.CACHE_NAME_SQLMODEL_TATICS);
		}
		new QueryBeanFormatter().executeDisposeQueryBean(queryParam, queryBean);
		
		/**判定是含有指定时间查询的内容，仅支持1分钟缓存时长**/
		if(queryBean.isSelect() && (queryBean.getSql().indexOf("now()") > -1 || queryBean.getSql().indexOf("NOW()") > -1)){
			queryBean.setCacheConfig("s_1min");
		}
		
		return queryBean;
	}

	/**
	 * 由请求Map 带权限 直接获取请求结果
	 * @param queryParam
	 * @param rids
	 * @return
	 * @throws QDevelopException
	 */
	
	public ResultBean getQueryResult(Map<String,String> queryParam,SecurityUserBean sub) throws QDevelopException{
		return getQueryResult(getQueryBean(queryParam, sub));
	}

	/**
	 * 由请求Map 直接获取请求结果
	 * @param queryParam
	 * @return DBResultBean
	 * @throws QDevelopException
	 */
	
	public ResultBean getQueryResult(Map<String,String> queryParam) throws QDevelopException{
		return getQueryResult(getQueryBean(queryParam, null));
	}
	
	/**
	 * 直接由SQL Index获取默认的请求结果
	 * @param index 
	 * @return DBResultBean
	 * @throws QDevelopException
	 */
	public ResultBean getQueryResultByIndex(String index) throws QDevelopException{
		return getQueryResult(getQueryBean(index, null));
	}

	/**
	 * 由请求SQL 直接获取请求结果
	 * @param query
	 * @return
	 * @throws QDevelopException
	 */
	
	public ResultBean getQueryResult(String sql,String connect) throws QDevelopException{
		return getQueryResult(createQueryBean(sql,connect));
	}

	/**
	 * 由QueryBean 直接获取请求结果
	 * @param query
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean getQueryResult(QueryBean<DBQueryBean> query) throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)query;
		ResultBean rb = null;
		if(_queryBean.isCacheAble()){
			rb = (ResultBean)CacheFactory.secondCache().get(_queryBean.getCacheKey(), _queryBean.getCacheConfig());
		}
		if(rb!=null){
			return DataBaseFactory.getInstance().formatterResultBean(_queryBean, rb,null);
		}
		return DataBaseFactory.getInstance().select(query);
	} 

	/**
	 * 获取单个结果
	 * @param query
	 * @return Map
	 * @throws QDevelopException
	 */
	public Map getQueryResultSingle(QueryBean<DBQueryBean>  query) throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)query;
		if(_queryBean.isCacheAble()){
			Serializable object = CacheFactory.secondCache().get(_queryBean.getCacheKey(), _queryBean.getCacheConfig());
			if(object != null && object instanceof DBResultBean){
				return DataBaseFactory.getInstance().formatterResultBean(_queryBean, (DBResultBean)object,null).getResultMap(0);
			}
		}
		return DataBaseFactory.getInstance().selectSingle(query);
	}
	
	/**
	 * 获取单个结果
	 * @param query
	 * @return Map
	 * @throws QDevelopException
	 */
	public Map getQueryResultSingle(Map query) throws QDevelopException{
		return getQueryResultSingle(getQueryBean(query));
	}
	
	/**
	 * 获取结果树
	 * @param query
	 * @return TreeBean
	 * @throws QDevelopException
	 */
	public TreeBean getQueryTree(Map query) throws QDevelopException{
		TreeBean tree = null;
		DBQueryBean qb = getQueryBean(query);
		if(qb.isCacheAble()){
			ResultBean _tree = (ResultBean)CacheFactory.secondCache().get(qb.getCacheKey(), qb.getCacheConfig());
			if(_tree!=null && _tree instanceof TreeBean)return (TreeBean)DataBaseFactory.getInstance().formatterResultBean(qb,_tree,null);
		}
		tree = new TreeBean(query);
		return DataBaseFactory.getInstance().selectTree(qb,tree);
	}
	
	/**
	 * 获取结果树
	 * @param query
	 * @param rids
	 * @return TreeBean
	 * @throws QDevelopException
	 */
	public TreeBean getQueryTree(Map query,String[] rids) throws QDevelopException{
		String key = toQueryKey(query,rids);	
		TreeBean tree = new TreeBean(query);
		
		DBQueryBean qb = getQueryBean(query,key,rids);
		if(qb.isCacheAble()){
			TreeBean _t = (TreeBean)CacheFactory.secondCache().get(qb.getCacheKey(), qb.getCacheConfig());
			if(_t!=null && _t instanceof TreeBean)return  (TreeBean)DataBaseFactory.getInstance().formatterResultBean(qb,_t,null);
		}
		return DataBaseFactory.getInstance().selectTree(qb,tree);
	}


	/**
	 * 由QueryBean 更新数据库
	 * @param queryBean
	 * @return int
	 * @throws QDevelopException
	 */
	public int getQueryUpdate(QueryBean queryBean,IUpdateHook ... iuh) throws QDevelopException{
		return DataBaseFactory.getInstance().update(queryBean,iuh);
	}
	
	/**
	 * 由QueryBean 更新数据库
	 * @param queryBean
	 * @return
	 * @throws QDevelopException
	 */
	public int getQueryUpdate(QueryBean queryBean) throws QDevelopException{
		return DataBaseFactory.getInstance().update(queryBean);
	}
	
	/**
	 * 直接SQL更新数据库
	 * @param sql
	 * @return
	 * @throws QDevelopException
	 */
	public int getQueryUpdate(String ... sql) throws QDevelopException{
		return DataBaseFactory.getInstance().update(sql);
	}
	
	public int getQueryUpdate(String config,boolean isAsyc,String ... sql) throws QDevelopException{
		return DataBaseFactory.getInstance().update(config,sql,null,isAsyc);
	}

	/**
	 * 由请求Map 更新数据库
	 * @param query
	 * @return
	 * @throws QDevelopException
	 */
	
	public int getQueryUpdate(Map query) throws QDevelopException{
		return getQueryUpdate(getQueryBean(query));
	}
	
	public int getQueryUpdate(Map query,IUpdateHook iuh) throws QDevelopException{
		return getQueryUpdate(getQueryBean(query),iuh);
	}

	
	/**
	 * 由sql直接分析出内部关联的SQL索引
	 * @param sqls
	 * @return
	 * @throws QDevelopException
	 */
	public String[] getClearIndexBySQL(String ... sqls)throws QDevelopException{
		return SQLModelLoader.getInstance().getClearIndexBySQL(sqls);
	}
	/**
	 * 由请求Map 带权限 更新数据库
	 * @param query
	 * @param rid
	 * @return
	 * @throws QDevelopException
	 */
	
	public int getQueryUpdate(Map query,SecurityUserBean sub) throws QDevelopException{
		return getQueryUpdate(getQueryBean(query,sub));
	}
	
	
	public int getQueryUpdate(Map query,SecurityUserBean sub,IUpdateHook iuh) throws QDevelopException{
		return getQueryUpdate(getQueryBean(query,sub),iuh);
	}

	/**
	 * 由请求生成缓存KEY
	 * @param data
	 * @param rids
	 * @return
	 */
	public static String toQueryKey(Map<String,String> data,String[] rids){
		Map<String,String>  tmp = new HashMap<String, String>(data);
		StringBuffer sb = new StringBuffer();
		sb.append(tmp.get("index"))
		.append("(")
		.append(tmp.get("page")==null?"":tmp.get("page"))
		.append(",")
		.append(tmp.get("maxNum")==null?"":tmp.get("maxNum"))
		.append(",")
		.append(tmp.get("order")==null?"":tmp.get("order"))
		.append(")");
		if(rids!=null){
			sb.append("^").append(rids[0]);
			if(rids.length>2)sb.append(rids[2]);
			sb.append("^");
		}
		tmp.remove("index");
		tmp.remove("page");
		tmp.remove("maxNum");
		tmp.remove("order");
		tmp.remove("allCount");
		if(tmp.size()>0)
			sb.append("@").append(get32MD5(tmp.toString()));
		tmp.clear();
		tmp = null;
		return sb.toString();
	}
	
	public static void shutdown(){
		QScheduleFactory.getInstance().shutdown();
		MemCachedImpl.getInstance().shutdown();
		ConnectFactory.shutdown();
	}

	private static String get32MD5(String args){ 
		char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'}; 
		try { 
			byte[] strTemp = args.getBytes(); 
			MessageDigest mdTemp = MessageDigest.getInstance("MD5"); 
			mdTemp.update(strTemp); 
			byte[] md = mdTemp.digest(); 
			int j = md.length; 
			char str[] = new char[j * 2]; 
			int k = 0; 
			for (int i = 0; i < j; i++) { 
				byte byte0 = md[i]; 
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
				str[k++] = hexDigits[byte0 & 0xf]; 
			} 
			return new String(str); 
		} 
		catch (Exception e){ 
			return null; 
		} 
	} 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
}
