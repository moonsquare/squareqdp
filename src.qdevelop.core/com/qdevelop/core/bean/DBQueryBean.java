package com.qdevelop.core.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.utils.QProperties;

/**
 * 数据库请求QueryBean
 * @author Janson.Gu
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DBQueryBean implements QueryBean,Serializable,Cloneable{

	public DBQueryBean clone(){
		try {
			return (DBQueryBean) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	public DBQueryBean(){}
	public DBQueryBean(String sql){
		this.sql = sql;
	}

	public DBQueryBean(Map<String,String> queryParam){
		this.sqlIndex = queryParam.get("index");
		this.nowPage = parseInt( queryParam.get("page"));
		this.maxNum = parseInt( queryParam.get("maxNum"));
		this.order = queryParam.get("order");
		if(queryParam.get("allCount")!=null)
			this.allCount = parseInt(queryParam.get("allCount"));
		this.isDebug = Boolean.parseBoolean(String.valueOf(queryParam.get("isDebug")));
		if(queryParam.get("cacheAble")!=null)
			this.isCacheAble = Boolean.parseBoolean(queryParam.get("cacheAble"));
		else 
			this.isCacheAble = null;

		this.clearBaseInfo(queryParam);
		this.data = new HashMap();

		java.util.Iterator itor = queryParam.entrySet().iterator();
		Entry es;
		while(itor.hasNext()){
			es = (Entry)itor.next();
			if(es.getValue()!=null){
				this.data.put(String.valueOf(es.getKey()), String.valueOf(es.getValue()));
			}
		}

		String[] filter = QProperties.getInstance().getProperty("DBQueryBean.filter.key")==null?null:
			QProperties.getInstance().getProperty("DBQueryBean.filter.key").split("\\|");
		if(filter!=null){
			for(String key:filter){
				this.data.remove(key);
			}
		}
		queryParam.clear();
	}
	public static String[] clearParam = new String[]{"index","page","maxNum","order","allCount","cacheAble","isReversal","lazyPagination","lazyLoad","isDebug","treeRootShow","treeRichData","treeKey","treeParentKey","treeRootParentValue","treeTextKey"};

	public HashMap clearBaseInfo(Map queryParam){
		for(String key:clearParam){
			queryParam.remove(key);
		}
		return (HashMap)queryParam;
	}

	private static final long serialVersionUID = -4478229107846214285L;
	public String cacheKey;
	public String queryType,cacheConfig,beforeRun,afterRun,order,sql,targetTable,uniTable,authorizedTable,
	uniKey,explain,connect,database,userRoleId,sqlIndex="",clearCache,user,userId,updateHook,resultBean,depends;
	public String[] /*仅查询autoSearch方式控制指定可使用的参数列表*/args;
	public ArrayList<String> sqls;
	public Integer[] fetchIndex;
	public Integer[] judgeIndex;
	public int nowPage,maxNum,allCount;
	public boolean isSelect,isCallBackSeqID,isCondition=false;
	/**是否需要调试**/
	public boolean isDebug=false;
	/**是否需要记日志**/
	public boolean isLog;
	/**异步清理cache**/
	public boolean isAsync;

	public Boolean isCacheAble;

	public HashMap<String,Object> data;

	/**
	 * 获取请求参数的克隆数据
	 * @return
	 */
	public Map getQueryDataClone(){
		if(data==null)return null;
		return (HashMap)data.clone();
	}
	public Map getQueryData(){
		return data;
	}

	public String[] getComplexQueryParam(){
		if(data ==null || data.get("param")==null)return null;
		return String.valueOf(data.get("param")).split(";");
	}
	public void clearQueryData(){
		if(this.data!=null)this.data.clear();
	}
	public boolean isNullQueryData(String key){
		return data.get(key)==null;
	}
	public String getQueryData(String key){
		return data.get(key) == null ? "" : String.valueOf(data.get(key));
	}


	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setUserRoleId(String userRoleId) {
		this.userRoleId = userRoleId;
	}

	public String getCountCacheKey(){
		if(!this.isAuthorize)
			return this.getCacheKey().replaceAll("\\(.+\\)|\\^.+\\^", "");
		else
			return this.getCacheKey().replaceAll("\\(.+\\)", "");
	}

	//	public String getModelKey(){
	//		return this.getCacheKey().replaceAll("\\(.+\\)|\\^.+\\^", "");
	//	}
	public boolean isWorkFlow() {
		return isWorkFlow;
	}

	public void setWorkFlow(boolean isWorkFlow) {
		this.isWorkFlow = isWorkFlow;
	}

	public boolean isAuthorize() {
		return isAuthorize;
	}

	public void setAuthorize(boolean isAuthorize) {
		this.isAuthorize = isAuthorize;
	}

	public void setConnect(String connect) {
		this.connect = connect==null?CONNECT_DEFAULT:connect;
	}

	public boolean isWorkFlow,isAuthorize;

	public int getPaginationStart(){
		return this.maxNum*(this.nowPage-1);
	}

	public boolean isDataSecurity() {
		return isAuthorize;
	}

	public String[] getQuery(){
		return this.sql!=null?new String[]{getSql()}:this.sqls.toArray(new String[]{});
	}
	/**
	 * 获取该SQL查询记录总数
	 * @return
	 */
	public String getSQLByCount(){
		if(getSql().toLowerCase().indexOf("sum") == -1 && getSql().toLowerCase().indexOf(" group ") == -1 && getSql().toLowerCase().indexOf(" having ") == -1)
			return getSql().replaceAll("SELECT ", "select ").replaceAll(" FROM ", " from ").replaceAll("^select.+?from ", "select count(1) as cn from ");
		else 
			return new StringBuffer().append("select count(1) as cn from (").append(getSql()).append(") qd_c").toString();
	}

	public String getLogSql(){
		if(this.isPagination()){
			return new StringBuffer().append(this.getSQLByOrder()).append(" limit ").append(maxNum*(nowPage-1))
					.append(",").append(maxNum).toString();
		}else return this.getSQLByOrder();
	}

	public String getSQLByOrder(){
		if(this.getOrder()==null){
			return getSql();
		}else if(getSql().toUpperCase().indexOf(" ORDER ")==-1){
			return new StringBuffer().append(getSql()).append(" order by ").append(this.getOrder()).toString();
		}else 
			return new StringBuffer().append("select * from (").append(getSql()).append(") qd_o order by ").append(this.getOrder()).toString();
	}

	public String getSql() {
		if(sql==null&&sqls!=null&&sqls.size()>0)return sqls.get(0);
		if(isAuthorize && isSelect){
			if(this.getUserRoleId().equals("0"))return sql;//超级管理员跳过权限控制
			if(database.equals("ORACLE")){/**执行ORACLE数据权限SQL**/
				return new StringBuffer().append("select q_a.*").append(isWorkFlow?",q_s.wid as wf_idx":"").append(" from (").append(this.sql)
						.append(") q_a,(select distinct id as r_id").append(isWorkFlow?",wid":"").append("  from ").append(this.authorizedTable)
						.append(" where regexp_like (rid,'").append(this.getUserRoleId()).append("') or regexp_like(sid,'")
						.append(this.getUserRoleId()).append("|ALL')) q_s where q_s.r_id=q_a.")
						.append(this.getUniKey()).toString();
			}else if(database.equals("MYSQL")){/**执行MYSQL数据权限SQL**/
				return new StringBuffer().append("select q_a.*").append(isWorkFlow?",q_s.wid as wf_idx":"").append(" from (").append(this.sql)
						.append(") q_a join (select distinct id as r_id").append(isWorkFlow?",wid":"").append("  from ").append(this.authorizedTable)
						.append(" where rid regexp '").append(this.getUserRoleId()).append("' or sid regexp'")
						.append(this.getUserRoleId()).append("|ALL') q_s on q_s.r_id=q_a.")
						.append(this.getUniKey()).toString();
			}
		}
		return sql;
	}

	public String getDataManageSql(){
		if(sql==null&&sqls!=null&&sqls.size()>0)return sqls.get(0);
		if(isAuthorize && isSelect){
			if(database.equals("ORACLE")){/**执行ORACLE数据权限SQL**/
				return new StringBuffer().append("select q_a.*").append(isWorkFlow?",q_s.wid as wf_idx":"").append(",q_s.sid,q_s.rid").append(" from (").append(this.sql)
						.append(") q_a,(select id as r_id,sid,rid").append(isWorkFlow?",wid":"").append("  from ").append(this.getUniTable())
						.append(") q_s where q_s.r_id(+)=q_a.")
						.append(this.getUniKey()).toString();
			}else if(database.equals("MYSQL")){/**执行MYSQL数据权限SQL**/
				return new StringBuffer().append("select q_a.*").append(isWorkFlow?",q_s.wid as wf_idx":"").append(",q_s.sid,q_s.rid").append(" from (").append(this.sql)
						.append(") q_a left join (select  id as r_id,sid,rid").append(isWorkFlow?",wid":"").append("  from ").append(this.getUniTable())
						.append(") q_s on q_s.r_id = q_a.")
						.append(this.getUniKey()).toString();
			}
		}
		return sql;
	}

	public List<String> getSqls(){
		return sqls;
	}

	public void addSql(String sql){
		if(sqls==null)sqls = new ArrayList();
		if(sql!=null)
			sqls.add(sql);
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setTargetTable(String table){
		this.targetTable = table;
	}

	public String getTargetTable(){
		return (this.targetTable == null?getUniTable().replace("qd_s_", ""):this.targetTable);
	}

	public String getUniTable() {
		return (uniTable.startsWith("qd_s_")?uniTable:new StringBuffer().append("qd_s_").append(uniTable.replaceAll(",", "_")).toString());
	}

	public void setUniTable(String uniTable) {
		this.uniTable = uniTable;
	}

	public String getUniKey() {
		return uniKey==null?"ID":uniKey;
	}

	public void setUniKey(String uniKey) {
		this.uniKey = uniKey;
	}

	public String getUserRoleId(){
		if(userRoleId==null)return "NULL";
		if(userRoleId.indexOf(";")>-1){
			userRoleId = userRoleId.replaceAll(";", "|");
		}
		return userRoleId;
	}

	public String getExplain() {
		return explain;
	}

	public void setExplain(String explain) {
		this.explain = explain;
	}

	public String getConnect(){
		if(connect==null)return CONNECT_DEFAULT;
		return connect;
	}

	@Override
	public void setQuery(Object query) {
		sql = String.valueOf(query);
	}

	public String getSqlIndex() {
		return sqlIndex;
	}

	public void setSqlIndex(String sqlIndex) {
		this.sqlIndex = sqlIndex;
	}

	public String getClearCache() {
		return clearCache;
	}

	public void setClearCache(String clearCache) {
		this.clearCache = clearCache;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public String getCacheConfig() {
		return cacheConfig==null?QDevelopConstant.CACHE_NAME_RESULTBEAN_TACTICS:cacheConfig;
	}

	public void setCacheConfig(String cacheConfig) {
		this.cacheConfig = cacheConfig;
	}

	public String getBeforeRun() {
		return beforeRun;
	}

	public void setBeforeRun(String beforeRun) {
		this.beforeRun = beforeRun;
	}

	public String getAfterRun() {
		return afterRun;
	}

	public void setAfterRun(String afterRun) {
		this.afterRun = afterRun;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey == null ? null : cacheKey.replaceAll(" ", "");
	}

	public int getNowPage() {
		return nowPage;
	}

	public void setNowPage(int nowPage) {
		this.nowPage = nowPage;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}

	public int getAllCount() {
		return allCount;
	}

	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}

	public boolean isSelect() {
		return isSelect;
	}

	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	public void setSqls(ArrayList<String> sqls) {
		this.sqls = sqls;
	}


	@Override
	public boolean isCacheAble() {
		if(isCacheAble!=null&&!isCacheAble)return false;
		return QProperties.globeCache && ((cacheConfig==null || cacheKey==null)?false:true);
	}


	public void setCacheAble(boolean isCacheAble){
		if(this.isCacheAble==null)
			this.isCacheAble = isCacheAble;
	}

	/**
	 * 
	 * TODO 后台指定缓存了，前台不需要缓存主动更新 
	 * 
	 * @return
	 */
	public boolean isCasCache(){
		return QProperties.globeCache && ((cacheConfig==null || cacheKey==null)?false:true);
	}

	@Override
	public boolean isPagination() {
		return (nowPage>0&&maxNum>1)?true:false;
	}
	public boolean isSingleSQL(){
		return this.sql!=null||this.sqls==null||this.sqls.size()==1;
	}

	@Override
	public String toKey() {
		return this.getCacheKey();
	}
	private int parseInt(String _boolean){
		if(_boolean==null)return -1;
		return Integer.parseInt(_boolean.trim());
	}



	public String getQueryContent(){
		try {
			StringBuffer sb= new StringBuffer();
			sb.append("{index:'").append(this.sqlIndex).append("'");
			if(this.isPagination()){
				sb.append(",page:").append(this.nowPage)
				.append(",maxNum:").append(this.maxNum);
			}
			if(this.order!=null){
				sb.append(",order:'").append(this.order).append("'");
			}
			if(this.allCount>0)sb.append(",allCount:").append(this.allCount);
			Iterator<Entry<String, Object>> itor = this.data.entrySet().iterator();
			while(itor.hasNext()){
				Entry<String, Object> query = itor.next();
				if(query.getValue()!=null){
					String val = String.valueOf(query.getValue());
					if(val.indexOf("|")>-1){
						String[] tts = val.split("\\|");
						for(String t:tts){
							sb.append(",").append(query.getKey().toUpperCase()).append(":'").append(t).append("'");
						}
					}else{
						sb.append(",").append(query.getKey().toUpperCase()).append(":'").append(val).append("'");
					}
				}
			}
			return sb.append("}").toString();
		} catch (Exception e) {
			System.out.println(data.toString());
			e.printStackTrace();
		}
		return null;
	}

	public String toString(){
		StringBuffer sb =  new StringBuffer();
		sb.append(this.sqlIndex).append(" : ").append(this.isCacheAble()).append(this.isCacheAble()?this.getCacheKey():"").append("\r\n");
		sb.append("params:").append(this.data).append("\r\n");
		String[] sql = this.getQuery();
		for(String s:sql){
			sb.append(s).append("\r\n");
		}
		return sb.toString();
	}
}
