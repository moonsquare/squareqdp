package com.qdevelop.web.interceptor;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.cache.sync.OperaterLogQueue;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.web.utils.WebUtils;

public class QDevelopJsonInteceptor  extends QDevelopInterceptor{
	private static final long serialVersionUID = -1168032880604561282L;
	public static final Logger operater = QLog.getInstance().getLog("operater");

	private static final Pattern filter = Pattern.compile("addUrl\\.action|clear\\.action|checkRole\\.action");
	public String intercept(ActionInvocation invocation) throws Exception {
		if(!QProperties.isSecurityInterceptor)return invocation.invoke();
		HttpServletRequest request = ServletActionContext.getRequest();
		if(isPass(request)){//隐式注入权限控制
			QLog.getInstance().systemLogger(new StringBuffer().append(request.getParameter("$v")).append("\t login in [").append(WebUtils.getUserIp(request)).append("]"));
			return invocation.invoke();
		}
		Map<String,Object> session = ActionContext.getContext().getSession();
		SecurityUserBean sub = validateUser(request,session);
		if(sub==null){
			throw new QDevelopException(UtilsFactory.properties().getProperty("LoginAction.noLogin.error"));
		}
		String uri = WebUtils.getRomveHeaderURI(request,request.getRequestURI());
		//user_id,user_name,fun_name,uri,content,oper_time,ip
		if(!filter.matcher(uri).find()){
			String refer = WebUtils.getRomveHeaderURI(request,request.getHeader("Referer"));
			String args = WebUtils.args(request);
			operater.info(new StringBuffer().append("[").append(sub.getName()).append("] (").append(request.getRemoteAddr()).append(") ").append(args).append(" from:[/").append(refer).append("]"));
			OperaterLogQueue.getInstance().addLog(new Object[]{
					sub.id,sub.name,refer,uri,args,System.currentTimeMillis(),sub.userIP	
			});
		}
		return invocation.invoke();
	}
}
