package com.qdevelop.core.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ognl.OgnlException;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.standard.IBeforeRun;
import com.qdevelop.core.standard.IPaginationFormatter;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

@SuppressWarnings({ "unchecked","rawtypes" })
public class QueryBeanFormatter {
	//	public static QueryBeanFormatter getInstance(){
	//		return new QueryBeanFormatter();
	//	}
	/**
	 * before run method
	 * @param param
	 * @param qb
	 * @return
	 */
	public void executeDisposeQueryBean(Map param , DBQueryBean qb)  throws QDevelopException{
		if(qb.beforeRun == null)return;
		DBQueryBean tmp = (DBQueryBean)reflectRun(qb.beforeRun, "disposeQueryBean", new Object[]{qb,param});
		if(tmp!=null)qb = tmp;
	}

	/**
	 * 由Index找到相关的配置后 格式化请求参数;<br>
	 * 不支持多index方式
	 * @param query
	 * @param sub
	 * @throws OgnlException 
	 */
	public void executeFormatQueryParam(Map query,SecurityUserBean sub,Map<String,Object> session) throws QDevelopException{
		String index = (String)query.get("index");
		if(index ==null || index.indexOf(";")>-1)return;

		IPaginationFormatter ipf = SQLModelLoader.getInstance().getPaginationFormatter(index);
		if(ipf!=null){
			query = ipf.formatterPagination(query);
		}

		Iterator<IBeforeRun> formatter = SQLModelLoader.getInstance().getBeforeFormatter(index);
		if(formatter == null) return;
		try {
			while(formatter.hasNext()){
				formatter.next().disposeQueryMap(query, sub, session);
			}
		} catch (QDevelopException e) {
			throw e;
		}catch (Exception e) {
			e.printStackTrace();
			QLog.getInstance().systemError(e);
		}
	}

	public void executeFormatterParam(String paramKey,String valueKey,Map param,SecurityUserBean sub,Map<String,Object> session) {
		Object value;
		if(sub!=null&&valueKey.startsWith("user.")){
			value = sub.getUserInfo(valueKey.replace("user.", ""));
		}else{
			if(session!=null && session.get(valueKey)!=null){
				value = session.get(valueKey);
			}else{
				value = QProperties.getInstance().getJsonValue(valueKey);
			}
		}
		if(value!=null)param.put(paramKey,value);
	}

	/**
	 * 运用Java反射机制执行某个方法
	 * @param claSSName	类名
	 * @param method	方法名
	 * @param param		参数
	 * @return
	 * @throws Exception
	 */
	public Object reflectRun(String claSSName,String method,Object[] param) throws QDevelopException {
		try{
			Class c =Class.forName(claSSName);
			Constructor con = c.getDeclaredConstructor();
			con.setAccessible(true);
			Object oo = con.newInstance();
			Method methlist[] = c.getDeclaredMethods(); 
			for (int i = 0; i < methlist.length; i++) { 
				Method m = methlist[i]; 
				if(m.getName().equals(method)){
					Class[] partypes = m.getParameterTypes();
					if(partypes.length>0&&param!=null&&param.length>0){
						Object[] arglist = new Object[param.length];
						for(int j=0;j<partypes.length;j++){
							Object val = j>param.length?null:param[j];
							if(val!=null){
								if(partypes[j].equals(QueryBean.class)){
									arglist[j] = (QueryBean)val;
								}else if(partypes[j].equals(DBQueryBean.class)){
									arglist[j] = (DBQueryBean)val;
								}else if(partypes[j].equals(ResultBean.class)){
									arglist[j] = (ResultBean)val;
								}else if(partypes[j].equals(DBResultBean.class)){
									arglist[j] = (DBResultBean)val;
								}else if(partypes[j].equals(TreeBean.class)){
									arglist[j] = (TreeBean)val;
								}else if(partypes[j].equals(Map.class)||partypes[j].equals(HashMap.class)){
									arglist[j] = (Map)val;
								}else arglist[j] = val;
							}
						}
						return m.invoke(oo, arglist);
					}
					return m.invoke(oo, new Object[]{});
				}
			}
		} catch (QDevelopException e) {
			throw e;
		}catch (Exception e) {
			e.printStackTrace();
			QLog.getInstance().systemError(e);
		}
		return null;
	}
}
