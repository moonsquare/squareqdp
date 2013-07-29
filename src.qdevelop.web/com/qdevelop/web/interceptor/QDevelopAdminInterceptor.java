package com.qdevelop.web.interceptor;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QProperties;
import com.qdevelop.web.utils.SecurityFactory;
import com.qdevelop.web.utils.WebUtils;

public class QDevelopAdminInterceptor extends AbstractInterceptor{
	private static final long serialVersionUID = 76120738153519508L;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		SecurityUserBean sub = WebUtils.getSecurityUserBean(ActionContext.getContext().getSession());
		String _url = ServletActionContext.getRequest().getRequestURI();
		String fun = SecurityFactory.getInstance().checkHasRoleAndReturnFunctionName(_url, sub.getRoles());
		if(fun == null){
			throw new QDevelopException(QProperties.getInstance().getProperty("QDevelopException.msg.fail.function", SecurityFactory.getInstance().getErrorInfo(_url,sub.getRoles())));
		}
		String ips = QProperties.getInstance().getProperty("qdevelop_admin_address");
		if(ips==null){
			throw new QDevelopException("对不起，您还没有配制管理员访问IP范围[properties中缺配置qdevelop_admin_address]！");
		}
		String[] ipsAllowd = ips.split("\\|");
		for(String ip : ipsAllowd){
			if( sub.getUserIP().indexOf(ip) > -1 ){
				return invocation.invoke();
			}
		}
		throw new QDevelopException("对不起，您["+sub.getUserIP()+"]不在管理员工具的IP访问允许范围之内！");
	}
}
