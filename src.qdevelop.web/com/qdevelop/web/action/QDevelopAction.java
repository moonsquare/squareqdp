package com.qdevelop.web.action;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.qdevelop.bean.ResultBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.standard.IPagination;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QString;

public class QDevelopAction  extends ActionSupport{
	private static final long serialVersionUID = -8264644590203244726L;

	/**消息结果**/
	public static final String MSG = "msg";
	/**错误信息**/
	public static final String ERROR = "friendError";
	/**结果信息**/
	public static final String RESULT = "result";
	/**登录地址**/
	public static final String LOGIN = "login";

	boolean  isTranspose = false;

	Map<String,String> transposeShows;
	/**
	 * 获取请求参数
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map getParamMap(){
		HttpServletRequest req = ServletActionContext.getRequest();
		Map paramMap = new HashMap();
		Enumeration paramNames = req.getParameterNames();
		String key;
		String[] value;
		while (paramNames.hasMoreElements()) {
			key = (String) paramNames.nextElement();
			value = req.getParameterValues(key);
			if(value!=null&&value.length==1){
				paramMap.put(key, value[0]);
			}else {
				StringBuffer tmp = new StringBuffer();
				int len = value.length;
				for(int i=0;i<len;i++){
					if(i>0)tmp.append(";");
					tmp.append(value[i]);
				}
				paramMap.put(key, tmp.toString());
			}
		}
		paramMap.remove("$r");//去除随机数信息
		paramMap.remove("$v");//去除单点登录信息
		return paramMap;
	}

	protected String[] getLoginUserInfo(SecurityUserBean sub){
		if(sub==null)return null;
		return new String[]{sub.getRoles(),sub.getSelfRole(),sub.getLoginName()}; 
	}

	protected SecurityUserBean getUserInfo(){
		return (SecurityUserBean)ActionContext.getContext().getSession().get(QDevelopConstant.USER_SESSION_KEY);
	}

	protected Map<String,Object> getSession(){
		return ActionContext.getContext().getSession();
	}

	protected int maxNum = 15,page=1;
	protected boolean isReversal=false,isLazyPagination = false,isLazyLoad=false;

	/**
	 * 初始化兼容EasyUI请求参数的分页请求数据
	 * @param query
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void initPagination(Map query){
		/**兼容EasyUI Datagrid数据请求格式**/
		if(query.get("rows")!=null){
			if(query.get("maxNum")==null)
				query.put("maxNum", query.get("rows"));
			query.remove("rows");
		}

		if(query.get("sort")!=null){
			query.put("order", QString.append(query.get("sort")," ",query.get("order")));
			query.remove("sort");
		}

		if(query.get("isReversal")!=null){
			isReversal = Boolean.parseBoolean((String)query.get("isReversal"));
			query.remove("isReversal");
		}

		if(query.get("maxNum")!=null){
			try{
				maxNum = Integer.parseInt(String.valueOf(query.get("maxNum")));
			} catch (NumberFormatException e){
				maxNum = 10;
				QLog.getInstance().systemWarn("分页参数格式化错误:"+query.toString());
			}
		}

		if(query.get("page")!=null){
			try{
				page = Integer.parseInt(String.valueOf(query.get("page")));
			} catch (NumberFormatException e){
				page = 1;
				QLog.getInstance().systemWarn("分页参数格式化错误:"+query.toString());
			}
		}

		if(query.get("lazyPagination")!=null){
			isLazyPagination = Boolean.parseBoolean((String)query.get("lazyPagination"));
			query.remove("lazyPagination");
			if(isLazyPagination){
				query.put("allCount", String.valueOf(maxNum));
			}
		}
		if(query.get("lazyLoad")!=null){
			isLazyLoad = Boolean.parseBoolean(String.valueOf(query.get("lazyLoad")));
			query.remove("lazyLoad");
		}

		/**数据反转参数删除**/
		query.remove("reversalFields");
		query.remove("reversalShows");

	}

	/**
	 * 表单反转初始化
	 * @param query
	 */
	@SuppressWarnings("rawtypes")
	protected void initTranspose(Map query){
		if(null == query.get("transposeShows")){
			return;
		}
		/**transposeShows=ID:编号|NAME:名称|...**/
		String [] _transposeShows = query.get("transposeShows").toString().split("\\|");
		query.remove("transposeShows");
		transposeShows = new HashMap<String, String>();
		for(String i : _transposeShows){
			String[] temp = i.split(":");
			if(temp.length < 2)return;
			transposeShows.put(temp[0], temp[1]);
		}
		//FIXME 去除分页参数 保证查询出所有所需数据
		query.remove("page");
		query.remove("maxNum");
		isTranspose = true;
	}

	/**
	 * 获取懒加载分页总数的方法
	 * @param rb
	 * @return
	 */
	protected int getLazyPageSize(ResultBean rb){
		if(!isLazyPagination){
			if(rb instanceof IPagination)
				return ((IPagination)rb).getAllCount();
		}
		if(rb.size()<maxNum)
			return page*maxNum;
		else 
			return page*maxNum+1;
	}

}
