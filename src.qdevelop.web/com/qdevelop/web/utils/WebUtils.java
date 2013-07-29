package com.qdevelop.web.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.cache.CacheFactory;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.TreeBean;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;

@SuppressWarnings({ "unchecked","rawtypes" })
public class WebUtils {
	//	private static int headerIndex=0;
	private static String header;
	private static int headerLen;

	public static String getRomveHeaderURI(HttpServletRequest request,String url){
		//		if(url==null)return "";
		//		if(headerIndex == 0 || header == null) {
		//			if(url.indexOf("http")>-1){
		//				header = new StringBuffer().append(request.getServerName()).append(request.getServerPort()==80?"":":"+request.getServerPort()).append(request.getContextPath()).toString();
		//				if(url.indexOf(header)>-1)headerIndex = url.indexOf(header)+header.length() + 1;
		//			}else{
		//				header = request.getContextPath();
		//				if(url.indexOf(header)>-1)headerIndex = url.indexOf(header)+header.length() + 1;
		//			}
		//		}
		//		if(url.indexOf(header)==-1)return url;
		//		return url.substring(headerIndex);
		if(url==null)return "";
		if(header == null) {
			if(url.indexOf("http")==-1){
				header = request.getContextPath();
			}
			
			if(header==null || header.length()==0){
				header = new StringBuffer().append(request.getServerName()).append(request.getServerPort()==80?"":":"+request.getServerPort()).append(request.getContextPath()).toString();
			}
			headerLen = header.length()+1;
			System.out.println("current header ======>  "+header);
		}
		if(url.indexOf(header)==-1||headerLen>url.length())return url;
		return url.substring(url.indexOf(header)+headerLen);
	}

	//	public static String getRequestURI(HttpServletRequest request){
	//		return getRomveHeaderURI(request,request.getRequestURI());
	//	}
	public static Pattern isNeedCacheStruts = Pattern.compile("SELECT [^\\*]+? FROM");

	public static String formatterUrl(HttpServletRequest request){
		if(QDevelopConstant.SYSTEM_NAME.equals("QDevelop3.0")){
			QDevelopConstant.SYSTEM_NAME = request.getContextPath().replace("/", "");
			if(QDevelopConstant.SYSTEM_NAME.length()==0)QDevelopConstant.SYSTEM_NAME = "/";
		}
		Enumeration paramNames = request.getParameterNames();
		StringBuffer sb = new StringBuffer();
		String key;
		String[] value;
		while (paramNames.hasMoreElements()) {
			key = (String) paramNames.nextElement();
			value = request.getParameterValues(key);
			for(String tmp:value){
				if(!key.equals("$r")&&!key.equals("$v"))
					sb.append("&").append(key).append("=").append(tmp);
			}
		}
		if(sb.length()>0){
			return new StringBuffer().append(request.getRequestURL()).append("?").append(sb).toString();
		}
		return request.getRequestURL().toString().substring(7).replaceAll(QString.append("^.+?",QDevelopConstant.SYSTEM_NAME), "");
	}

	public static String args(HttpServletRequest request){
		Enumeration paramNames = request.getParameterNames();
		StringBuffer sb = new StringBuffer();
		String key;
		//		String[] value;
		while (paramNames.hasMoreElements()) {
			key = (String) paramNames.nextElement();
			if(!key.equals("$r")&&!key.equals("$v"))
				sb.append("&").append(key).append("=").append(request.getParameter(key));
			//			value = request.getParameterValues(key);
			//			for(String tmp:value){
			//				if(!key.equals("$r")&&!key.equals("$v"))
			//					sb.append("&").append(key).append("=").append(tmp);
			//			}
		}
		return sb.length()==0?"":sb.substring(1).toString();
	}

	public static boolean isLogined(Map session){
		if(!QProperties.isSecurityInterceptor)return true;
		return getSecurityUserBean(session)==null ? false : true;
	}

	public static boolean checkSingleLogin(Map session,String userIp,String userName){
		if(!QProperties.isLoginSingle)return false;
		SecurityUserBean sub = getSecurityUserBean(session);
		if(sub==null)return false;
		return sub.userIP.equals(userIp)&&sub.getLoginName().equals(userName);
	}

	public static boolean checkSingleLogin(SecurityUserBean sub,String userIp,String userName){
		if(sub==null)return false;
		return !sub.userIP.equals(userIp)&&sub.getLoginName().equals(userName);
	}

	public static SecurityUserBean getSecurityUserBean(Map session){
		return (SecurityUserBean)session.get(QDevelopConstant.USER_SESSION_KEY);
	}

	public SecurityUserBean checkOnline(String validateInfo,String userIp,Map session)throws Exception{
		String[] tmp = validateInfo.split("@");
		if(tmp.length<2)return null;
		Integer num = (Integer)session.get(QDevelopConstant.WEB_ONLINE_CHECK_KEY);
		if(num!=null && num > 3){
			throw new QDevelopException(QProperties.getInstance().getProperty("LoginAction.ValidatorLogin.error"));
		}
		Map query = new HashMap();
		query.put("loginname", tmp[0]);
		query.put("password", tmp[1]);
		SecurityUserBean sub = check(query,userIp);
		if(sub==null){
			if(num==null)num = 0;
			else num += 1;
			session.put(QDevelopConstant.WEB_ONLINE_CHECK_KEY,num);
			QLog.getInstance().securityError(QString.append("userName:",tmp[0],"\tloginIp:",userIp,"\tonline Validator fail (",num,")"));
			throw new QDevelopException(QProperties.getInstance().getProperty("LoginAction.ValidatorLogin.errorLoginInfo"));
		}	
		QLog.getInstance().securityLogger(QString.append("userName:",sub.loginName,"\tloginIp:",sub.userIP,"\tRole:",sub.getRoles(),"\tonline Validator success"));
		session.remove(QDevelopConstant.WEB_ONLINE_CHECK_KEY);
		return sub;
	}

	public static String getUserIp(HttpServletRequest  request){
		if(request==null)request = ServletActionContext.getRequest();
		String ip = request.getHeader("X-Real-IP"); 

		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip) || ip.indexOf("115.182.92.")>-1) {
			ip =  request.getHeader("X-Forwarded-For");
		}

		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip) || "127.0.0.1".equalsIgnoreCase(ip)) {
			ip =  request.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip) || "127.0.0.1".equalsIgnoreCase(ip)) {
			ip =   request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip) || "127.0.0.1".equalsIgnoreCase(ip)) {
			ip =   request.getRemoteAddr();
		}
		return ip;
	}

	public SecurityUserBean check(Map queryMap,String userIp) throws QDevelopException{
		queryMap.put("index", "userCheckLogin");
		QueryBean qb  = CoreFactory.getInstance().getQueryBean(queryMap);
		SecurityUserBean sub;
		if(qb.isCacheAble()){
			sub = (SecurityUserBean)CacheFactory.secondCache().get(qb.getCacheKey(), qb.getCacheConfig());
			if(sub!=null)return sub;
		}
		ResultBean rb =  CoreFactory.getInstance().getQueryResult(qb);
		if(rb.size()==0)return null;
		Map data = rb.getResultMap(0);
		if(data==null){
			return null;
		}
		sub = new SecurityUserBean(data);
		sub.setSelfRid(sub.rid);
		sub.setRid(getUserAllRoles(sub.id,sub.rid,sub.gid));
		sub.setUserIP(userIp);
		sub.setLoginTime(QDate.getNow("yyyy-MM-dd HH:mm:ss"));
		return sub;
	}

	private String getUserAllRoles(String uid,String rid,String gid) throws QDevelopException{
		Map tmp = new HashMap();
		tmp.put("index", "quaryUserRole");
		tmp.put("uid", uid);
		tmp.put("rid", rid==null?"'null'":rid.replaceAll("^;|;$", "").replaceAll(";|\\|", ","));
		tmp.put("gid", gid);
		Map result = CoreFactory.getInstance().getQueryResultSingle(tmp);
		Iterator<String> itor = result.values().iterator();
		Set userAllRoles = new HashSet();

		userAllRoles.add(rid);

		String _r;String[] _rs;
		while(itor.hasNext()){
			_r = itor.next();
			if(_r!=null){
				_rs = _r.split(";|\\|");
				for(String _sr:_rs){
					userAllRoles.add(_sr);
				}
			}
		}

		Map query = new HashMap();
		query.put("index", "quaryRoles");
		TreeBean tb = CoreFactory.getInstance().getQueryTree(query);
		if(tb!=null){
			List<Map> child = tb.getChilds(rid);
			if(child!=null){
				for(Map roleData : child){
					userAllRoles.add(roleData.get("ID"));
				}
			}

			String relation = (String)result.get("RELATION");
			if(relation!=null){
				String[] _rids = relation.split(";|\\|");
				for(String _rr : _rids){
					child = tb.getChilds(_rr);
					if(child!=null){
						for(Map roleData : child){
							userAllRoles.add(roleData.get("ID"));
						}
					}
				}
			}
		}
		return QString.append(userAllRoles, "|");
	}
}
