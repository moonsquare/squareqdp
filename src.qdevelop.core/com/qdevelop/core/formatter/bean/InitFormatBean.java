package com.qdevelop.core.formatter.bean;

import java.util.HashMap;

import org.dom4j.Element;

/**
 * 初始化自定义formatter参数Bean
 * @author Janson
 *
 */
public class InitFormatBean extends HashMap<String,String>{
	private static final long serialVersionUID = 2622052873089433197L;

	public InitFormatBean(Element config,String[] paramKeys){
		for(String key:paramKeys){
			if(key.indexOf("^")>-1){
				String[] kk = key.split("\\^");
				for(String k:kk){
					this.put(k, config.attributeValue(k));
				}
			}else{
				this.put(key, config.attributeValue(key));
			}
		}
	}
	
	public String getConfig(String key){
		return this.get(key);
	}
	public String getConfig(String key,boolean isUpcase){
		String val = this.get(key);
		if(val==null)return null;
		return isUpcase?val.toUpperCase():val;
	}
	
	public String[] getConfig(String key,String split){
		return this.getConfig(key, split, false);
	}
	public String[] getConfig(String key,String split,boolean isUpcase){
		String val = this.get(key) ;
		if(val==null)return null;
		return isUpcase?this.get(key).toUpperCase().split(split):this.get(key).split(split);
	}
	
	public String getLoopConfig(String ... key){
		String val;
		for(String k:key){
			val = this.get(k);
			if(val!=null)return val;
		}
		return null;
	}
}
