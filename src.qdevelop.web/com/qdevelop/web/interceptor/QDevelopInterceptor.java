package com.qdevelop.web.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QClass;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.utils.cache.MapCache;
import com.qdevelop.web.bean.UrlBean;
import com.qdevelop.web.bean.UrlResultBean;
import com.qdevelop.web.standard.IButtonCtl;
import com.qdevelop.web.utils.SecurityFactory;
import com.qdevelop.web.utils.WebUtils;

public class QDevelopInterceptor extends AbstractInterceptor{
	public static Boolean  isButtonCtrl;
	public static IButtonCtl _IButtonCtl;
	private static final long serialVersionUID = -2897260993232780084L;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		if(!QProperties.isSecurityInterceptor)return invocation.invoke();
		try {
			HttpServletRequest request = ServletActionContext.getRequest();
			if(isPass(request)){
				QLog.getInstance().systemLogger(new StringBuffer().append(request.getParameter("$v")).append("\t login in [").append(WebUtils.getUserIp(request)).append("]"));
				return invocation.invoke();
			}
			Map<String,Object> session = ActionContext.getContext().getSession();
			SecurityUserBean sub = validateUser(request,session);
			String _url = WebUtils.formatterUrl(request);
			request.setAttribute("com.qdevelop.web.interceptor.current.url", _url);
			if(sub == null){
				session.put(QDevelopConstant.WEB_FOCUS_URL_KEY, QString.append("/",QDevelopConstant.SYSTEM_NAME,_url));
				return Action.LOGIN;
			}
			if(QProperties.isLoginSingle){
				SecurityUserBean lsub = (SecurityUserBean)MapCache.getInstance().getCache(sub.loginName);
				String userIp = ServletActionContext.getRequest().getRemoteAddr();
				if(lsub!=null && !userIp.equals(lsub.userIP)){
					session.remove(QDevelopConstant.USER_SESSION_KEY);
					throw new QDevelopException(UtilsFactory.properties().getProperty(
							"LoginAction.single.login.error",sub.loginName,lsub.userIP));
				}
			}
			if(isButtonCtrl==null){
				if(QProperties.getInstance().get("QDevelop_button_control")==null){
					isButtonCtrl = false;
				}else{
					isButtonCtrl=true;
					_IButtonCtl = (IButtonCtl)QClass.getInstanceClass(QProperties.getInstance().getProperty("QDevelop_button_control"));
				}
				
			}
			if(isButtonCtrl){
				String uri = WebUtils.getRomveHeaderURI(request,request.getRequestURI());
				UrlResultBean urlBean = SecurityFactory.getInstance().getUrlResultBean();
				UrlBean ub  = urlBean.getUrlBean(uri);
				if(ub!=null){
					_IButtonCtl.verifyUserButtonAuthorization(ub.getId(), sub.getRid().split(",|\\|"), request);
				}
			}

		} catch (Exception e) {
			throw e;
		}
		return invocation.invoke();
	}

	protected boolean isPass(HttpServletRequest request){
		if(request.getParameter("$v")==null)
			return false;
		if(QProperties.getInstance().isQDevelopCall(request.getParameter("$v")))
			return true;
		String key = QProperties.getInstance().getProperty(request.getParameter("$v"));
		if(key==null)return false;
		return Boolean.parseBoolean(key);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected SecurityUserBean validateUser(HttpServletRequest request,Map session) throws Exception{
		String validatorUserInfo = request.getParameter("$v");
		if(validatorUserInfo != null ){
			if(session.get(QDevelopConstant.WEB_FOCUS_VALIDATE_INFO)==null || 
					!String.valueOf(session.get(QDevelopConstant.WEB_FOCUS_VALIDATE_INFO)).equals(validatorUserInfo)){
				SecurityUserBean sub = new WebUtils().checkOnline(validatorUserInfo, request.getRemoteAddr(),session); 
				if(sub==null)return null;
				session.put(QDevelopConstant.USER_SESSION_KEY, sub);
				return sub;
			}
		}
		return (SecurityUserBean)session.get(QDevelopConstant.USER_SESSION_KEY);
	}

}
