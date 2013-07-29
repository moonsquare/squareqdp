package com.qdevelop.web.action;

import java.util.Map;

import org.apache.struts2.ServletActionContext;

import web.QD;

import com.qdevelop.cache.CacheFactory;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.sqlmodel.SQLModelParser;
import com.qdevelop.core.utils.CoreUtils;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.QVerify;
import com.qdevelop.web.bean.UrlResultBean;
import com.qdevelop.web.utils.SecurityFactory;
import com.qdevelop.web.utils.WebUtils;

public class SystemControlAction extends QDevelopAction{
	private static final long serialVersionUID = 2554919864116316982L;
	Object msg;
	String index;

	public String systemReload() throws Exception{
		CoreUtils.systemReload();
		QD.init(QProperties.getInstance().getProperty("resource_loop_uri"));
		QD.version = QProperties.getInstance().getProperty("res_version") == null?"1.0":QProperties.getInstance().getProperty("res_version");
		msg = "恭喜！基础配置数据重载完成，系统数据缓存清理完成！";
		return SUCCESS;
	}

	@SuppressWarnings("unchecked")
	public String addUrl2Index(){
		Map<String,Object> data = getParamMap();
		String url = WebUtils.getRomveHeaderURI(ServletActionContext.getRequest(), (String)data.get("LINKURL")).replaceAll("\\?.+$", "");
		data.put("ID", QString.get32MD5(url));
		data.put("LINKURL", url);
//		System.out.println(data);
		UrlResultBean urlBean = SecurityFactory.getInstance().getUrlResultBean();
		if(!urlBean.isExist(data)){
			urlBean.addResult(data);
			DBQueryBean query = SecurityFactory.getInstance().getUrlQuery();
			CacheFactory.secondCache().add(query.getCacheKey(),query.getQueryContent(), urlBean, query.getCacheConfig());
		}
		return SUCCESS;
	}

	public String reloadCert() throws Exception{
		QVerify.getInstance().init();
		msg = QVerify.getInstance().getCertMsg();
		return SUCCESS;
	}

	public String clearCacheByIndex() throws QDevelopException{
		if(index!=null && index.length() > 1){
			try {
				CasIndexArray casIndeArray =  CacheFactory.secondCache().getCacheIndexs(index,SQLModelParser.getInstance().getAttrbuteByIndex(index,"cacheConfig"));
				//				CacheFactory.secondCache().getTacties().mergeAsynAddCache(casIndeArray);
				if(casIndeArray!=null){
					String[] keys = casIndeArray.getLastCacheKeys(CacheFactory.secondCache().getTacties(), 5);
					for(String key:keys)
						CacheFactory.secondCache().remove(key,null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			msg = "true";
		}
		return SUCCESS;
	}

	public Object getMsg() {
		return msg;
	}

	public void setMsg(Object msg) {
		this.msg = msg;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}


}
