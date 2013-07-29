package com.qdevelop.web.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.qdevelop.cache.CacheFactory;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.web.bean.UrlResultBean;

@SuppressWarnings({ "unchecked","rawtypes" })
public class SecurityFactory {
	private static SecurityFactory _SecurityFactory = new SecurityFactory();
	public static SecurityFactory getInstance(){return _SecurityFactory;}
	private DBQueryBean  query;
	
	private String menuKey = "menuManage";


	public UrlResultBean getUrlResultBean(){
		if(query==null){
			Map _query = new HashMap();
			_query.put("index", "menuManage");
			_query.put("target", "UrlResultBean");
			query = CoreFactory.getInstance().getQueryBean(_query);
		}
		UrlResultBean urlResultBean = (UrlResultBean)CacheFactory.secondCache().get(menuKey, QDevelopConstant.CACHE_NAME_RESULTBEAN_TACTICS);
		if(urlResultBean==null){
			urlResultBean = new UrlResultBean();
			Connection conn = null; 
			try {
				conn = ConnectFactory.getInstance(query.getConnect()).getConnection();
				QueryFromDataBaseImp queryFromDataBaseImp = QueryFromDataBaseImp.getInstance();
//				DataBaseFactory.validateDataAuthorize(query,conn,queryFromDataBaseImp);
				IResultFormatter[] _formatter = SQLModelLoader.getInstance().getFormatterBeanByIndex(query.getSqlIndex());
				urlResultBean = (UrlResultBean) queryFromDataBaseImp.select(query.getSql(), conn, urlResultBean, _formatter);
//				if(query.isCacheAble())
				CacheFactory.secondCache().add(menuKey,query.getQueryContent(), urlResultBean, QDevelopConstant.CACHE_NAME_RESULTBEAN_TACTICS);
			} catch (QDevelopException e) {
				throw e;
			} catch (Exception ex) {
				ex.printStackTrace();
			}finally{
				try {
					if(conn!=null)conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return urlResultBean;
	}  
	public DBQueryBean getUrlQuery(){
		return query;
	}

	public boolean isAcceptURL(String url,String role){
		return getUrlResultBean().checkHasRole(url, role);
	}
	public String checkHasRoleAndReturnFunctionName(String url,String role){
		return getUrlResultBean().checkHasRoleAndReturnFunctionName(url, role);
	}

	public String[] getErrorInfo(String url,String userRole){
		Map tmp = new HashMap();
		tmp.put("index", "quaryRoles");
		TreeBean tb = CoreFactory.getInstance().getQueryTree(tmp);
		String[] info = getUrlResultBean().getSecurityErrorInfo(url);
		if(info[1]!=null){
			info[1] = tb.getTextById(info[1].split(";|\\|"));
		}
		if(userRole!=null)
			info[2] = tb.getTextById(userRole.split(";|\\|"));
		return info;
	}


	public static void main(String[] args){
		//			UrlResultBean urb  = SecurityFactory.getInstance().getUrlResultBean();
		////			long sys = System.currentTimeMillis();
		////			for(int i=0;i<10000;i++){
		//			System.out.println(urb.getNameByUrl("business/BusinessControl.action?type=userAttrDistribute&name=jasn"));
		//			System.out.println(urb.getNamesByUrl("event/index.action?index=ass"));
		////			System.out.println(urb.getNameByUrl("business/BusinessControl.action"));
		////			}
		//			System.out.println(urb.checkHasRole("business/BusinessControl.action?type=userAttrDistribute&name=jasn","1004"));
		//			System.out.println(urb.checkHasRole("event/index.action?index=ass","1003|1006"));
		//			System.out.println(System.currentTimeMillis()-sys);
		//			getNamesByUrl
		System.out.println("http://192.168.1.111:8080/mpb/json/easyGrid.action?&order=desc&index=selectbusiness&sort=UPDATE_TIME&rows=15&page=1".replaceAll("http://|^.+?/", ""));
	}
}
