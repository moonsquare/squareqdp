package com.qdevelop.core.datasource;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.cache.CacheFactory;
import com.qdevelop.cache.clear.ClearCacheQueue;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.bean.FooterBean;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.connect.IConnect;
import com.qdevelop.core.formatter.LoopQueryBaseformatter;
import com.qdevelop.core.sqlmodel.SQLCondition;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.sqlmodel.SQLModelParser;
import com.qdevelop.core.standard.IFooter;
import com.qdevelop.core.standard.IPagination;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.core.standard.IUpdateHook;
import com.qdevelop.core.utils.QueryBeanFormatter;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QCache;
import com.qdevelop.utils.QClass;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QVerifyAbstract;

@SuppressWarnings({ "unchecked","rawtypes" })
public class DataBaseFactory extends QVerifyAbstract{
	private  static HashMap<String,ResultBean> resultCache = new HashMap<String,ResultBean>();
	public DataBaseFactory() throws QDevelopException{
		super();
	}

	public static DataBaseFactory getInstance() throws QDevelopException{
		return new DataBaseFactory();
	}

	/**
	 * 扩展结果请求Bean	
	 * @param qb
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean queryResult(QueryBean qb)throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)qb;
		if(_queryBean.resultBean==null)return select(qb);
		ResultBean rb = (ResultBean) QClass.getInstanceClass(_queryBean.resultBean);
		return select(qb,rb);
	}

	/**
	 * 
	 * TODO 根据配置获取结果集的类 
	 * 
	 * @param qb
	 * @return
	 */
	private ResultBean getResultClass(QueryBean qb){
		String resultClass = ((DBQueryBean)qb).resultBean;
		if(resultClass==null)return new DBResultBean();
		ResultBean _resultBean = resultCache.get(resultClass);
		if(_resultBean == null){
			_resultBean = (ResultBean) QClass.getInstanceClass(resultClass);
			resultCache.put(resultClass, _resultBean);
		}
		return _resultBean.clone();
	}
	/**
	 * 默认请求结果DBResultBean
	 * @param qb
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean select(QueryBean qb) throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)qb;
		ResultBean rb =  (DBResultBean)select(qb,getResultClass(qb));
		if(_queryBean.getAllCount()>1 && rb instanceof IPagination){
			((IPagination)rb).setAllCount(_queryBean.getAllCount());
		}
		FooterBean fb = SQLModelLoader.getInstance().getFooterBeanByIndex(_queryBean.getSqlIndex());
		if(fb!=null && rb instanceof IFooter){
			ArrayList _footer = null;
			String footerKey = null;
			if(_queryBean.isCacheAble()){
				footerKey = fb.getFooterKey(_queryBean.getCacheKey());
				_footer = (ArrayList)CacheFactory.secondCache().get(footerKey, _queryBean.getCacheConfig());
			}

			if(_footer == null){
				if(rb instanceof IPagination)
					fb.setAll(((IPagination)rb).getAllCount());
				String args = SQLModelLoader.getInstance().getParamKey(fb.getFooterSQL());
				String footerSQL = SQLModelParser.getInstance().parserQuerySQL(new String(fb.getFooterSQL()),args==null?new String[]{}:args.split("\\|"),_queryBean.getQueryData(),true,null);
				_footer = fb.formatterFooter(selectSingle(footerSQL,_queryBean.getConnect()));
				if(_queryBean.isCacheAble())
					CacheFactory.secondCache().add(footerKey,null,_footer,_queryBean.getCacheConfig());
			}
			((IFooter)rb).setFooter(_footer);
		}
		_queryBean.clearQueryData();//清理数据
		return rb;
	}
	
	public ResultBean select(QueryBean qb,IResultFormatter[] _formatter) throws QDevelopException{
		return select(qb,new DBResultBean(),_formatter);
	}
	
	public ResultBean select(QueryBean qb,ResultBean rb) throws QDevelopException{
		return select(qb,rb,null);
	}
	
	
	/**
	 * 主查询方法
	 * @param qb
	 * @param rb
	 * @param _formatter
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean select(QueryBean qb,ResultBean rb,IResultFormatter[] _formatter) throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)qb;
		if(_queryBean.isCacheAble()){
			ResultBean _rb = (ResultBean)CacheFactory.secondCache().get(_queryBean.getCacheKey(), _queryBean.getCacheConfig());
			if(_rb!=null){
				rb = formatterResultBean(_queryBean,_rb,_formatter);
				return rb;
			}
		}
		IConnect ic = ConnectFactory.getInstance(_queryBean.getConnect());
		Connection conn=null;
		try {
			conn = ic.getConnection();
			return select(qb,rb,conn,ic.getDataBase(),_formatter);
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * TODO 将cache中的结果再重新让formatter类重新执行一遍
	 * @param qb
	 * @param rb
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean formatterResultBean(DBQueryBean qb,ResultBean rb,IResultFormatter[] _formatter) throws QDevelopException{
		if(qb==null || rb==null)return null;
		if(_formatter==null)_formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(qb.getSqlIndex());
		if(_formatter != null){
			DBStrutsBean struts = null;
			for(IResultFormatter rf:_formatter){
				rf.initFormatter(rf.isNeedStruts()?initDBStrutsBean(qb,struts):null);
				if(rf instanceof LoopQueryBaseformatter){
					((LoopQueryBaseformatter) rf).setMasterCondition(qb.getQueryData());
				}
			}
			for(int i=0;i<rb.size();i++){
				Map data = rb.getResultMap(i);
				if(data!=null){
					for(IResultFormatter rf:_formatter){
						rf.formatter(data,rf.isNeedStruts()?initDBStrutsBean(qb,struts):null);
					}
				}
			}
			for(IResultFormatter rf:_formatter){
				rf.flush(rb);
			}
		}
		if(qb.getAfterRun()!=null){
			try {
				ResultBean tmp = (ResultBean)new QueryBeanFormatter().reflectRun(qb.getAfterRun(), "disposeResultBean", new Object[]{rb,qb});
				if(tmp!=null){
					rb = tmp;
				}
			} catch (QDevelopException e) {
				QLog.getInstance().systemError(new StringBuffer().append(qb.getSqlIndex()).append(" run ").append(qb.getAfterRun()));
			}
		}
		return rb;
	}

	private DBStrutsBean initDBStrutsBean(DBQueryBean qb,DBStrutsBean struts){
		if(struts!=null)return struts;
		struts = QueryFromDataBaseImp.getInstance().getDBStrutsBean(qb);
		return struts;
	}
	
	public ResultBean select(QueryBean qb,ResultBean rb,Connection conn,String databse) throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)qb;
		if(_queryBean.isCacheAble()){
			ResultBean _rb = (ResultBean)CacheFactory.secondCache().get(_queryBean.getCacheKey(), _queryBean.getCacheConfig());
			if(_rb!=null){
				rb = formatterResultBean(_queryBean,_rb,null);
				return rb;
			}
		}
		return select(qb,rb,conn,databse,null);
	}
	
	/**
	 * 
	 * TODO （描述方法的作用） 
	 * 
	 * @param qb
	 * @param rb
	 * @param ic
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean select(QueryBean qb,ResultBean rb,Connection conn,String databse,IResultFormatter[] _formatter) throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)qb;
		long start=0;
		if(_queryBean.isDebug)start=System.currentTimeMillis();
		try {
			QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
			if(_queryBean.isPagination() && rb instanceof IPagination){
				if(_queryBean.allCount < 1){
					Integer allCount = null;
					if(_queryBean.isCacheAble()){
						allCount = (Integer)CacheFactory.secondCache().get(_queryBean.getCountCacheKey(), _queryBean.getCacheConfig());
					}
					if(allCount == null){
						allCount = Integer.parseInt(queryFromDataBaseImp.selectSingle(_queryBean.getSQLByCount(),conn, null).get("CN").toString());
						if(QProperties.isDebug)System.out.println("All Count Query : "+_queryBean.getSqlIndex());
						if(_queryBean.isCacheAble())
							CacheFactory.secondCache().add(_queryBean.getCountCacheKey(),_queryBean.getQueryContent(), allCount, _queryBean.getCacheConfig());
					}
					_queryBean.allCount = allCount;
				}
				((IPagination)rb).initPagination(_queryBean.getNowPage(),_queryBean.getMaxNum(),_queryBean.allCount);
				queryFromDataBaseImp.select(_queryBean.getSQLByOrder(), conn,rb,_queryBean.getNowPage(),_queryBean.getMaxNum(), null,databse.equals("MYSQL"));
			}else{
				queryFromDataBaseImp.select(_queryBean.getSQLByOrder(), conn,rb, null);
			}

			if(_queryBean.isCasCache())
				CacheFactory.secondCache().add(_queryBean.getCacheKey(),_queryBean.getQueryContent(), (Serializable)rb, _queryBean.getCacheConfig());
			QLog.getInstance().sqlLogger(_queryBean);
			ResultBean _rb = formatterResultBean(_queryBean,rb,_formatter);
			if(_queryBean.isDebug)QLog.getInstance().systemDebugger(new StringBuffer().append(_queryBean.sqlIndex).append(" [select] ").append((System.currentTimeMillis()-start)).append(" ms").toString());
			return _rb;
		} catch (QDevelopException e) {
			QLog.getInstance().sqlError(_queryBean);
			throw e;
		}
	}


	/**
	 * 
	 * TODO 指定某列，进行缓存拆分查询，提高缓存利用率
	 * 
	 * @param query 普通请求参数
	 * @param multiValueField 针对某列需要拆分
	 * @param values	拆分的值列表
	 * @return
	 * @throws QDevelopException
	 */
	public ResultBean getResult(HashMap<String,String> query,String multiValueField,String ... values) throws QDevelopException{
		if(query.get("index") == null || multiValueField == null || values == null || values.length == 0)throw new QDevelopException("参数传递错误，请检查参数合法性");
		DBResultBean rb = new DBResultBean();
		Connection conn = null;
		QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
		CoreFactory cf = CoreFactory.getInstance();
		try {
			for(String v:values){
				Map<String,String> complexQuery = (HashMap<String,String>)query.clone();
				complexQuery.put(multiValueField, v);
				DBQueryBean qb =  cf.getQueryBean(complexQuery);
				DBResultBean ctb = qb.isCacheAble()?(DBResultBean) CacheFactory.secondCache().get(qb.getCacheKey(),  qb.getCacheConfig()):null;
				if(ctb == null){
					ctb = new DBResultBean();
					try {
						if(conn == null)conn = ConnectFactory.getInstance(qb.getConnect()).getConnection();
						IResultFormatter[] _formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(qb.getSqlIndex());
						ctb = (DBResultBean) queryFromDataBaseImp.select(qb.getSQLByOrder(), conn,ctb, _formatter);
						if(qb.getAfterRun()!=null){
							DBResultBean tmp = (DBResultBean)new QueryBeanFormatter().reflectRun(qb.getAfterRun(), "disposeResultBean", new Object[]{ctb,qb});
							if(tmp!=null){
								ctb = tmp;
							}
						}
						if(qb.isCacheAble())
							CacheFactory.secondCache().add(qb.getCacheKey(),qb.getQueryContent(), (Serializable)ctb, qb.getCacheConfig());
					} catch (QDevelopException e) {
						QLog.getInstance().sqlError(qb);
					}
				}
				rb.setResultList(ctb.getResultList());
				qb.clearQueryData();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rb;
	}

	public Map<String,Object> selectSingle(String sql,String connect)throws QDevelopException{
		Map<String,Object> result;
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(connect).getConnection();
			QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
			result = queryFromDataBaseImp.selectSingle(sql, conn, null);
			QLog.getInstance().sqlLogger(sql);
			return result;
		} catch (QDevelopException e) {
			QLog.getInstance().sqlError(sql);
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Map selectSingle(QueryBean queryBean)throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)queryBean;

		if(_queryBean.isCacheAble()){
			if(_queryBean.isCacheAble()){
				Serializable object = CacheFactory.secondCache().get(_queryBean.getCacheKey(), _queryBean.getCacheConfig());
				if(object != null && object instanceof DBResultBean){
					return formatterResultBean(_queryBean, (DBResultBean)object,null).getResultMap(0);
				}
			}
		}

		DBResultBean rb = new DBResultBean();
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();
			QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
			Map result = queryFromDataBaseImp.selectSingle(_queryBean.getSQLByOrder(), conn, null);
			if(result!=null){
				rb.add(result);
				if(_queryBean.isCasCache())
					CacheFactory.secondCache().add(_queryBean.getCacheKey(),_queryBean.getQueryContent(), (Serializable) rb, _queryBean.getCacheConfig());
				result = formatterResultBean(_queryBean,rb,null).getResultMap(0);
			}
			_queryBean.clearQueryData();//清理数据
			QLog.getInstance().sqlLogger(_queryBean);
			return result;
		} catch (QDevelopException e) {
			QLog.getInstance().sqlError(_queryBean);
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public TreeBean selectTree(QueryBean queryBean,TreeBean tree)throws QDevelopException{
		DBQueryBean _queryBean = (DBQueryBean)queryBean;
		if(_queryBean.isCacheAble()){
			ResultBean _tree = (ResultBean)CacheFactory.secondCache().get(_queryBean.getCacheKey(), _queryBean.getCacheConfig());
			if(_tree!=null && _tree instanceof TreeBean){
				tree = (TreeBean)formatterResultBean(_queryBean,_tree,null);
				return tree;
			}
		}

		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();
			QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
			queryFromDataBaseImp.selectTree(_queryBean.getSQLByOrder(), conn, tree, null);
			if(_queryBean.isCasCache())
				CacheFactory.secondCache().add(_queryBean.getCacheKey(),_queryBean.getQueryContent(), tree, _queryBean.getCacheConfig());
			tree = (TreeBean) formatterResultBean(_queryBean,tree,null);
			_queryBean.clearQueryData();//清理数据
			QLog.getInstance().sqlLogger(_queryBean);
			return tree;
		} catch (QDevelopException e) {
			QLog.getInstance().sqlError(_queryBean);
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int update(QueryBean queryBean) throws QDevelopException{
		return update(queryBean,null);
	}

	/**
	 * 增加基于队列式更新数据库机制（非实时更新）
	 * @param queryBean
	 * @return
	 * @throws QDevelopException
	 */
	public int asynUpdate(QueryBean queryBean) throws QDevelopException{
		return AsynUpdateFactory.getInstance().addPool((DBQueryBean)queryBean);
	}

	/**
	 * 更新语句
	 * @param queryBean
	 * @return
	 * @throws QDevelopException
	 */

	public int update(QueryBean queryBean,IUpdateHook[] iuh)throws QDevelopException{
		long timer = 0; 
		if(QProperties.isDebug)timer = System.currentTimeMillis();
		if(queryBean == null)return 0;
		DBQueryBean _queryBean = (DBQueryBean)queryBean;
		if(_queryBean.depends == null){
			Connection conn = null; 
			try {
				conn = ConnectFactory.getInstance(_queryBean.getConnect()).getConnection();

				/**支撑支持条件的SQL配置语法执行**/
				if(_queryBean.isCondition){
					SQLCondition sc =  new SQLCondition(_queryBean,conn);
					if(!sc.isExec())
						return sc.getResult();
				}

				int result = update(_queryBean,conn,iuh);
				if(_queryBean.isDebug)QLog.getInstance().systemDebugger(new StringBuffer().append(_queryBean.sqlIndex).append(" [update] ").append((System.currentTimeMillis()-timer)).append(" ms").toString());
				return result;
			} catch (QDevelopException e) {
				QLog.getInstance().sqlError(_queryBean);
				throw e;
			}finally{
				try {
					if(conn!=null)conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}else{
			List<DBQueryBean> collect = new ArrayList<DBQueryBean>();
			loopGetQueryBean(_queryBean,collect);
			int size = collect.size();
			DBQueryBean[] queryBeans = new DBQueryBean[size];
			for(int i=0;i<size;i++){
				queryBeans[i] = collect.get((size-1-i));
			}
			int i= update(queryBeans,iuh);
			if(QProperties.isDebug)System.out.println(new StringBuffer().append("update[depends] use time: ").append(System.currentTimeMillis()-timer));
			return i;
		}

	}

	public int update(DBQueryBean _queryBean,Connection conn,IUpdateHook[] iuh)throws QDevelopException{
		int result=0;
		if(_queryBean.isSingleSQL()){ 
			result = QueryFromDataBaseImp.getInstance().update(_queryBean.getSql(), conn,iuh,_queryBean.user==null?"":_queryBean.user);
		}else{
			result = QueryFromDataBaseImp.getInstance().updateBatch(_queryBean.getQuery(),conn,iuh,_queryBean.user==null?"":_queryBean.user,_queryBean.fetchIndex,_queryBean.judgeIndex);
		}

		if(_queryBean.getAfterRun()!=null){
			new QueryBeanFormatter().reflectRun(_queryBean.getAfterRun(), "disposeResultBean", new Object[]{null,_queryBean});
		}
		_queryBean.clearQueryData();
		if(result>0 && _queryBean.isCacheAble())ClearCacheQueue.getInstance().addCasSqls(_queryBean.getQuery());
		return result;
	}

	private void loopGetQueryBean(DBQueryBean qb,List<DBQueryBean> collect){
		if(qb==null || qb.depends==null )return;
		collect.add(qb);
		String[] depends = qb.depends.replaceAll(" |^,|,$", "").split(",");
		for(int i=depends.length-1;i>=0;i--){
			Map query = new HashMap(qb.getQueryData());
			query.put("index", depends[i]);
			/**执行其他的定义的参数格式化类**/
			new QueryBeanFormatter().executeFormatQueryParam(query, null,null);
			DBQueryBean dependsQB = SQLModelParser.getInstance().getQueryBean(query, null, null);
			if(dependsQB.depends!=null)
				loopGetQueryBean(dependsQB,collect);
			else collect.add(dependsQB);
		}
	}

	public int update(QueryBean[] queryBean ,IUpdateHook ... iuh)throws QDevelopException{
		int result = 0;
		String[] conns = new String[queryBean.length];
		String[][] sqls = new String[queryBean.length][];
		Integer[][] fetchIndex = new Integer[queryBean.length][];
		Integer[][] judgeIndex = new Integer[queryBean.length][];
		try {
			String user="";
			for(int i=0;i<queryBean.length;i++){
				DBQueryBean qb = ((DBQueryBean)queryBean[i]);
				if(qb.user!=null)user = qb.user;
				fetchIndex[i] = qb.fetchIndex;
				judgeIndex[i] = qb.judgeIndex;
				conns[i] = qb.getConnect();
				sqls[i] = qb.getQuery();
			}
			result =  QueryFromDataBaseImp.getInstance().updateBatch(sqls,conns,iuh,user,fetchIndex,judgeIndex);
			for(int i=0;i<queryBean.length;i++){
				DBQueryBean qb = ((DBQueryBean)queryBean[i]);
				if(qb.getAfterRun()!=null){
					new QueryBeanFormatter().reflectRun(qb.getAfterRun(), "disposeResultBean", new Object[]{null,qb});
				}
				if(result>0 && qb.isCacheAble())ClearCacheQueue.getInstance().addCasSqls(qb.getQuery());
				qb.clearQueryData();
			}
			return result;
		} catch (Exception e) {
			throw new QDevelopException(e);
		}
		
	}

	public DBResultBean getQueryStruts(QueryBean qb)throws QDevelopException{
		return getQueryStruts(((DBQueryBean)qb).getConnect(),qb.getSql(),((DBQueryBean)qb).isCacheAble());
	}

	public DBResultBean getQueryStruts(String sql,boolean isCacheAble)throws QDevelopException{
		return getQueryStruts(sql,QueryBean.CONNECT_DEFAULT,isCacheAble);
	}

	public DBResultBean getQueryStruts(String connctConfig,String sql,boolean isCacheAble)throws QDevelopException{
		DBResultBean rb=null;String key = null;
		if(isCacheAble){
			key = getSelectTableNames(sql);
			rb = (DBResultBean)QCache.ehCache().getCache(key);
			if(rb!=null)return rb;
		}
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(connctConfig).getConnection();
			rb =  QueryFromDataBaseImp.getInstance().getStrutsBySQL(sql, conn);
			if(isCacheAble)QCache.ehCache().setCache(key,rb);
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rb;
	}


	/**
	 * 自定义执行的SQL语句
	 * @param sql
	 * @return
	 * @throws QDevelopException
	 */
	public int update(String[] sql)throws QDevelopException{
		return update(QueryBean.CONNECT_DEFAULT,sql,null,false);
	}

	public int update(String[] sql,IUpdateHook[] iuh)throws QDevelopException{
		return update(QueryBean.CONNECT_DEFAULT,sql,iuh,false);
	}


	/**
	 * 自定义执行的SQL语句
	 * @param sql
	 * @return
	 * @throws QDevelopException
	 */

	public int update(List sql)throws QDevelopException{
		return update(QueryBean.CONNECT_DEFAULT,sql,null,false);
	}


	public int update(List sql,IUpdateHook ... iuh)throws QDevelopException{
		return update(QueryBean.CONNECT_DEFAULT,sql,iuh,false);
	}
	public int update(String connectName,String[] sql,IUpdateHook[] iuh)throws QDevelopException{
		return update(connectName,sql,iuh,false);
	}
	public int update(String connectName,String sql,IUpdateHook[] iuh)throws QDevelopException{
		return update(connectName,sql,iuh,false);
	}
	/**
	 * 自定义执行的SQL语句
	 * @param connectName 	配置名称
	 * @param sql			执行的SQL
	 * @param iuh			执行SQL过程中的钩子程序
	 * @param isAsyc		是否异步更新缓存 	默认:false
	 * @return	影响记录数
	 * @throws QDevelopException
	 */
	public int update(String connectName,String[] sql,IUpdateHook[] iuh,boolean isAsyc)throws QDevelopException{
		long timer = 0; 
		if(QProperties.isDebug)timer = System.currentTimeMillis();
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(connectName).getConnection();
			int result = QueryFromDataBaseImp.getInstance().updateBatch(sql,conn,iuh,null,null,null);
			if(result>0)ClearCacheQueue.getInstance().addCasSqls(sql);
			if(QProperties.isDebug)System.out.println(new StringBuffer().append("update[sqls] use time: ").append(System.currentTimeMillis()-timer));

			return result;
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int update(String connectName,String sql,IUpdateHook[] iuh,boolean isAsyc)throws QDevelopException{
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(connectName).getConnection();
			int result = QueryFromDataBaseImp.getInstance().update(sql,conn,iuh,null);
			if(result>0)ClearCacheQueue.getInstance().addCasSqls(sql);
			return result;
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int update(String connectName,List<String> sql,IUpdateHook[] iuh)throws QDevelopException{
		return update(connectName,sql,iuh,false);
	}

	/**
	 * 自定义执行的SQL语句
	 * @param connectName
	 * @param sql
	 * @return
	 * @throws QDevelopException
	 */
	public int update(String connectName,List<String> sql,IUpdateHook[] iuh,boolean isAsyc)throws QDevelopException{
		long timer = 0; 
		if(QProperties.isDebug)timer = System.currentTimeMillis();
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(connectName).getConnection();
			int result = QueryFromDataBaseImp.getInstance().updateBatch(sql,conn,iuh,null,null,null);
			if(result>0)ClearCacheQueue.getInstance().addCasSqls(sql.toArray(new String[]{}));
			if(QProperties.isDebug)System.out.println(new StringBuffer().append("update[sqls] use time: ").append(System.currentTimeMillis()-timer));
			return result;
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 自定义数据库配置请求存储过程
	 * @param connectName
	 * @param procedureName
	 * @param params
	 * @throws QDevelopException
	 */
	public void queryProcedure(String connectName,String procedureName,Object params)throws QDevelopException{
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance(connectName).getConnection();
			QueryFromDataBaseImp.getInstance().queryProcedure(procedureName, params);
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 请求默认数据库的存储过程 
	 * @param procedureName
	 * @param params
	 * @throws QDevelopException
	 */
	public void queryProcedure(String procedureName,Object params)throws QDevelopException{
		QueryFromDataBaseImp.getInstance().queryProcedure(procedureName, params);
	}



	//	//是否数据权限控制,自动创建权限关联表
	//	public static void validateDataAuthorize(DBQueryBean _queryBean,Connection conn,QueryFromDataBaseImp queryFromDataBaseImp) throws QDevelopException{
	//		//		if(!_queryBean.isAuthorize()||QCache.mapCache().isCached(_queryBean.authorizedTable, "com.qdevelop.core.datasource.QueryFactory"))return;
	//		//		queryFromDataBaseImp.queryProcedure("{CALL ROLETABLEPROCESS(?)}",_queryBean.authorizedTable , conn);
	//		//		QCache.mapCache().setCache(_queryBean.authorizedTable,"", "com.qdevelop.core.datasource.QueryFactory");
	//	}
	//
	//	private void autoInsertDataAuthorize(DBQueryBean _queryBean,Connection conn,QueryFromDataBaseImp queryFromDataBaseImp) throws QDevelopException{
	//		//		if(!_queryBean.isAuthorize())return;
	//		//		queryFromDataBaseImp.queryProcedure("{CALL INSERTDATASECURITY(?,?,?,?)}",new String[]{
	//		//				_queryBean.getTargetTable(),
	//		//				_queryBean.getUniKey(),
	//		//				_queryBean.authorizedTable,
	//		//				_queryBean.getUserRoleId()
	//		//		} , conn);
	//	}

	private String getSelectTableNames(String sql){
		String[] tbs = sql.toUpperCase().split("FROM | JOIN ");
		StringBuffer sb = new StringBuffer();
		for(String tb:tbs){
			tb = tb.replaceAll(" WHERE .+| ORDER.+| GROUP.+|\\(.+", "").trim().replaceAll(" .+,$|\\$\\[.+?\\]", "");
			if(tb.length()>1&&!tb.startsWith("SELECT")&&!tb.startsWith("(")){
				if(tb.indexOf(",")==-1)
					sb.append(tb.replaceAll(" .+?$|\\)", ""));
				else{
					for(String ss : tb.split(" .+,"))
						sb.append(ss.trim().replaceAll(" .+?$|\\)", ""));
				}
			}
		}
		return sb.toString();
	}
}
