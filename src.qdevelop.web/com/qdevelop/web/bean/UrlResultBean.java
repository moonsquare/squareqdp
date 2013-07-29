package com.qdevelop.web.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.web.utils.UrlParamSort;

@SuppressWarnings({"unchecked","rawtypes"})
public class UrlResultBean extends HashMap<String,UrlBean> implements ResultBean,Serializable,Cloneable{
	public UrlResultBean clone(){
		return (UrlResultBean) super.clone();
	}
	private static final long serialVersionUID = -2763227589610797297L;

	private UrlParamSort urlParamSort = new UrlParamSort(); 

	private HashMap<String,List> urlIndex = new HashMap();
	
	@Override
	public void addResult(Map<String,Object> data) {
		if(isExist(data))return;
		String url = (String)data.get("LINKURL");
		if(url!=null && !url.equals("null")){
			String [] tmp = url.trim().replace("./", "").split("\\?");
			List t = urlIndex.get(tmp[0]);
			if(t == null){
				t = new ArrayList();
			}
			Object[] urlPattern = new Object[2];
			if(tmp.length==1){
				urlPattern[0] = Pattern.compile(".+?");
			}else{
				urlPattern[0] = Pattern.compile(formatterUrlPath(tmp[1]));
			}
			urlPattern[1] = String.valueOf(data.get("ID"));
			t.add(urlPattern);
			urlIndex.put(tmp[0], t);
		}
		this.put(String.valueOf(data.get("ID")), new UrlBean(data));
	}

	public boolean checkHasRole(String url,String role){
		UrlBean ub = getUrlBean(url);
		if(ub==null)return true;
		return ub.hasRole(role);
	}
	
	public boolean isExist(Map<String,Object> data){
		return this.get(String.valueOf(data.get("ID"))) != null;
	}
	public String checkHasRoleAndReturnFunctionName(String url,String role){
		UrlBean ub = getUrlBean(url);
		if(ub==null)return "";
		if(ub.hasRole(role))return ub.getName();
		else return null;
	}

	public String getNameByUrl(String url){
		UrlBean ub = getUrlBean(url);
		if(ub==null)return null;
		return ub.getName();
	}

	public String getNamesByUrl(String url){
		UrlBean ub = getUrlBean(url);
		if(ub==null)return null;
		List tmp = new ArrayList();
		tmp.add(ub.getName());
		UrlBean pub = this.get(ub.getPid());
		while(pub!=null){
			tmp.add(pub.getName());
			pub = this.get(pub.getPid());
		}
		StringBuffer sb = new StringBuffer();
		for(int i = tmp.size()-1;i>-1;i--){
			sb.append("|").append(tmp.get(i));
		}
		return sb.substring(1);
	}

	public String[] getSecurityErrorInfo(String url){
		UrlBean ub = getUrlBean(url);
		if(ub==null)return null;
		String[] info = new String[3];
		info[1] = ub.getUrlRole();

		List tmp = new ArrayList();
		tmp.add(ub.getName());
		UrlBean pub = this.get(ub.getPid());
		while(pub!=null){
			tmp.add(pub.getName());
			pub = this.get(pub.getPid());
		}
		StringBuffer sb = new StringBuffer();
		for(int i = tmp.size()-2;i>-1;i--){
			sb.append(" > ").append(tmp.get(i));
		}
		info[0] = sb.length()>0?sb.substring(3):"";
		info[2] = "";
		return info;
	}

	public UrlBean getUrlBean(String url){
		String idx = findURLIndex(url);
		if(idx == null)return null;
		return this.get(idx);
	}

	private String findURLIndex(String url){
		String[] tmp = url.replaceAll("^/", "").split("\\?");
		List<Object[]> indexs = urlIndex.get(tmp[0]);
		if(indexs==null)return null;

		String target;
		if(tmp.length==1){
			target = " ";
		}else{
			target = parseUrlPath(tmp[1]);
		}
		for(Object[] partten : indexs ){
			if(((Pattern)partten[0]).matcher(target).find()){
				return (String)partten[1];
			}
		}
		return null;
	}

	@Override
	public void flush() {
	}

	private String formatterUrlPath(String path){
		StringBuffer sb = new StringBuffer();
		String[] p = path.replaceAll("^&|&$", "").split("&");
		Arrays.sort(p,urlParamSort);
		for(int i=0;i<p.length;i++){
			if(i>0)sb.append(".+");
			sb.append(p[i]);
		}
		return sb.toString();
	}

	private String parseUrlPath(String path){
		StringBuffer sb = new StringBuffer();
		String[] p = path.replaceAll("^&|&$", "").split("&");
		Arrays.sort(p,urlParamSort);
		for(int i=0;i<p.length;i++){
			if(i>0)sb.append("&");
			sb.append(p[i]);
		}
		return sb.toString();
	}

	@Override
	public List getResultList() {
		return null;
	}

	@Override
	public Map getResultMap(Object i) {
		return null;
	}

	public void clear(){
		urlParamSort = null;
		urlIndex.clear();
		urlIndex = null;
		super.clear();
	}


	@Override
	public void setResultList(List<Map<String, Object>> result) {
		// TODO Auto-generated method stub

	}

}
