package com.qdevelop.web.action;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.utils.cache.MapCache;
import com.qdevelop.web.utils.WebUtils;

@SuppressWarnings({"unchecked","rawtypes"})
public class CheckLoginAction extends QDevelopAction{

	private static final long serialVersionUID = -3353156051045325275L;
	String msg,lastUrl;

	public String execute() throws QDevelopException{
		Map<String,Object> session = ActionContext.getContext().getSession();
		SecurityUserBean sub = (SecurityUserBean)session.get(QDevelopConstant.USER_SESSION_KEY);
		if(sub == null){
			msg  = (String) session.get("msg");
			return Action.LOGIN;
		}
		/**当单点登录时，检查**/
		if(QProperties.isLoginSingle){
			SecurityUserBean lsub = (SecurityUserBean)MapCache.getInstance().getCache(sub.loginName);
			String userIp = WebUtils.getUserIp(null);
			if(lsub!=null && !userIp.equals(lsub.userIP)){
				session.remove(QDevelopConstant.USER_SESSION_KEY);
				msg = UtilsFactory.properties().getProperty("LoginAction.single.login.error",sub.loginName,userIp);
				return Action.LOGIN;
			}
		}

		return SUCCESS;
	}

	public String loginOut() throws QDevelopException{
		Map<String,Object> session = ActionContext.getContext().getSession();
		SecurityUserBean sub = (SecurityUserBean)session.get(QDevelopConstant.USER_SESSION_KEY);
		if(sub!=null && QProperties.isLoginSingle){
			MapCache.getInstance().removeCache(sub.loginName);
		}
		session.remove("msg");
		session.remove(QDevelopConstant.USER_SESSION_KEY);
		return SUCCESS;
	}

	public String checkSingleLogin() throws QDevelopException{
		if(QProperties.isLoginSingle){
			HttpServletRequest  request = ServletActionContext.getRequest();
			String loginName = request.getParameter("loginname");
			String userIp = WebUtils.getUserIp(request);
			SecurityUserBean sub = (SecurityUserBean)MapCache.getInstance().getCache(loginName);
			if(loginName!=null && sub!=null && !sub.userIP.equals(userIp)){
				msg = QString.append(sub.getLoginName(),"|",sub.getUserIP(),"|",sub.getLoginTime());
				return SUCCESS;
			} 
		}
		msg = "";
		return SUCCESS;
	}

	public String checkLogin() throws QDevelopException{
		Map<String,Object> session = ActionContext.getContext().getSession();
		/**暂时将全局path参数放置在这**/
		session.put("QDevelopDomain",  ServletActionContext.getRequest().getContextPath());
		Map<String,String> queryMap = this.getParamMap();
		String curLoginName = queryMap.get("loginname");
		if(curLoginName==null||queryMap.get("password")==null){
			errorMsg(UtilsFactory.properties().getProperty("LoginAction.userCheck.error"),session);
			return SUCCESS;
		}
		
		/**判断是否需要验证码来验证**/
		boolean randomNeed = queryMap.get("randomNeed") == null ? true : Boolean.parseBoolean((String)queryMap.get("randomNeed"));
		if(randomNeed && !String.valueOf(queryMap.get("randomNum")).toLowerCase().equals(String.valueOf(session.get(QDevelopConstant.WEB_CHECK_CODE)))){
			errorMsg(UtilsFactory.properties().getProperty("LoginAction.randomNum.error"),session);
			return SUCCESS;
		}

		//		queryMap.put("password", QString.get64MD5(queryMap.get("password")));

		String userIp = WebUtils.getUserIp(null);
		SecurityUserBean sub = new WebUtils().check(queryMap,userIp);

		if(sub==null){
			errorMsg(UtilsFactory.properties().getProperty("LoginAction.userCheck.error"),session);
			QLog.getInstance().securityError(QString.append("userName:",curLoginName,"\tloginIp:",userIp));
			return SUCCESS;
		}

		if(sub.getUserInfo("LOCK_IP_ADDR")!=null){
			if(sub.getUserInfo("LOCK_IP_ADDR") instanceof String){
				Pattern _Pattern = Pattern.compile(String.valueOf(sub.getUserInfo("LOCK_IP_ADDR")));
				if(!_Pattern.matcher(userIp).find()){
					QLog.getInstance().securityError(QString.append("userName:",sub.name,"\tloginIp:",sub.userIP,"\tRole:",sub.getRoles(),"\tcity",sub.getUserInfo("CITY")));
					throw new QDevelopException("对不起，您的登录IP不在允许范围之内...");
				}
			}else{
				String[] iplock = (String[])sub.getUserInfo("LOCK_IP_ADDR");
				if(!ArrayUtils.contains(iplock, userIp)){
					QLog.getInstance().securityError(QString.append("userName:",sub.name,"\tloginIp:",sub.userIP,"\tRole:",sub.getRoles(),"\tcity",sub.getUserInfo("CITY"),"\tID: ",sub.id,"\ttype: ",sub.getUserInfo("USERTYPE")));
					throw new QDevelopException("对不起，您的登录IP不在允许范围之内。");
				}
			}

		}
		session.put(QDevelopConstant.USER_SESSION_KEY, sub);
		boolean isCoerceLogin = false; 
		if(QProperties.isLoginSingle){
			if(MapCache.getInstance().getCache(sub.loginName)!=null){
				isCoerceLogin = true;
			}
			MapCache.getInstance().setCache(sub.loginName, sub);
		}

		QLog.getInstance().securityLogger(QString.append("userName:",sub.name,"\tloginIp:",sub.userIP,"\tRole:",sub.getRoles(),"\tisCoerceLogin:",isCoerceLogin,"\tcity",sub.getUserInfo("CITY"),"\tID: ",sub.id,"\ttype: ",sub.getUserInfo("USERTYPE")));
		this.msg = "SUCCESS:"+ QProperties.getInstance().getProperty("login_"+sub.userType);
		if(this.msg.equals("SUCCESS:null"))this.msg = "SUCCESS:index.action";
		//增加对登陆后，平台请求层重新生成功能
		session.put("tagVersion", Math.random());
		
		return SUCCESS;
	}

	public String getLastUrl() {
		Map session = ActionContext.getContext().getSession();
		if(QProperties.isLoginWithLastURL)return (String)session.get(QDevelopConstant.WEB_FOCUS_URL_KEY);

		int userType = ((SecurityUserBean)session.get(QDevelopConstant.USER_SESSION_KEY)).userType;
		lastUrl = QProperties.getInstance().getProperty("login_"+userType);

		return lastUrl==null?"index.action":lastUrl;
	}

	

	public void setLastUrl(String lastUrl) {
		this.lastUrl = lastUrl;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void errorMsg(String msg,Map session){
		this.msg = msg;
		session.put("msg", msg);
	}


}
