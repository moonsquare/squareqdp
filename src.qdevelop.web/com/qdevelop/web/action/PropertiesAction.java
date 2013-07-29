package com.qdevelop.web.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.utils.QJson;
import com.qdevelop.utils.QProperties;

public class PropertiesAction extends QDevelopAction{

	private static final long serialVersionUID = -8687875422002409362L;

	InputStream out;String propertiesKey;

	public String execute() throws Exception{
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setHeader("Cache-Control", "no-cache");
		SecurityUserBean sub = this.getUserInfo();
		Map<String,Object> session = this.getSession();
		if(propertiesKey.indexOf("|")>-1 || propertiesKey.indexOf("[")>-1){
			if(propertiesKey.indexOf("|")>-1){
				String[] keys = propertiesKey.split("\\|");
				StringBuffer values = new StringBuffer();
				for(String key : keys ){
					Object value = queryResultByKey(key,sub,session);
					values.append(toJsString(key,objToString(value)));
				}
				out = new ByteArrayInputStream(values.toString().getBytes("UTF-8"));
			}else if(propertiesKey.indexOf("[")>-1){
				String pKey = propertiesKey.substring(0,propertiesKey.indexOf("["));
				String[] vKeys = propertiesKey.substring(propertiesKey.indexOf("[")+1,propertiesKey.indexOf("]")).split(",");
				StringBuffer values = new StringBuffer();
				values.append("var ").append(pKey).append("={");
				for(int i=0;i<vKeys.length;i++){
					String value = objToString(queryResultByKey(vKeys[i],sub,session));
					boolean isStringOnly = value ==null || ( value.indexOf("{")==-1 && value.indexOf("[")==-1) ? true : false;
					values.append(i>0?",":"").append(vKeys[i]).append(":").append(isStringOnly?"'":"").append(value).append(isStringOnly?"'":"");
				}
				values.append("};");
				out = new ByteArrayInputStream(values.toString().getBytes("UTF-8"));
			}
		}else{
			Object value = queryResultByKey(propertiesKey,sub,session);
			out = new ByteArrayInputStream(toJsString(propertiesKey.replaceAll("\\..+?$|\\[.+?$", ""),objToString(value)).getBytes("UTF-8"));
		}
		return SUCCESS;
	}
	private String objToString(Object v) throws Exception{
		if(v==null)return null;
		if(v instanceof String || v instanceof Integer || v instanceof Double || v instanceof Float)return String.valueOf(v);
		return QJson.getJSONString(v);
	}
	private Object queryResultByKey(String key,SecurityUserBean sub,Map<String,Object> session){
		if(sub!=null && sub.getUserInfo(key)!=null){
			return sub.getUserInfo(key);
		}
		if(session!=null && session.get(key)!=null){
			return session.get(key);
		}
		return QProperties.getInstance().getJsonValue(propertiesKey);
	}

	private String toJsString(String key,String value){
		if(value!=null){
			boolean isStringOnly = value ==null || ( value.indexOf("{")==-1 && value.indexOf("[")==-1) ? true : false;
			return new StringBuffer().append("var ").append(key).append("=").append(isStringOnly?"'":"").append(value).append(isStringOnly?"'":"").append(";").toString();
		}else{
			return new StringBuffer().append("var ").append(key).append("=null;").toString();
		}
	}

	public InputStream getOut() {
		return out;
	}

	public void setOut(InputStream out) {
		this.out = out;
	}

	public String getPropertiesKey() {
		return propertiesKey;
	}

	public void setPropertiesKey(String propertiesKey) {
		this.propertiesKey = propertiesKey;
	}
}
