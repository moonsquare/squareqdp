package com.qdevelop.utils.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 缓存索引
 * @author Janson.Gu
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class QDevelopCacheIndex extends HashMap<String,List>{
	private static final long serialVersionUID = 7353422238171801224L;

	public void addCacheIndex(String key,String configName){
		String tmp = append(key.replaceAll("\\(.+\\)|@.+?$|\\^.+\\^", ""),"@",configName);
		List tmpIndex = this.get(tmp);
		if(tmpIndex == null){
			tmpIndex = new ArrayList();
		}
		tmpIndex.add(key);
		this.put(tmp, tmpIndex);
	}
	
	/**
	 * 同一个config里 多个index查找
	 * @param configName
	 * @param keyIndex
	 * @return
	 */
	public List<String> findKeys(String configName,String ... keyIndex){
		List<String> tmpKeys = new ArrayList();
		List<String> tmp;
		String cacheIndex;
		Iterator<String> itor = this.keySet().iterator();
		while(itor.hasNext()){
			cacheIndex = itor.next();
			for(String ki : keyIndex){
				if(cacheIndex.indexOf(ki)>-1 && cacheIndex.endsWith(configName)){
					tmp = this.get(cacheIndex);
					for(String realKey : tmp){
						tmpKeys.add(realKey);
					}
					break;
				}
			}
		}
		return tmpKeys;
	}
	
	/**
	 * 不同config，不同index查找
	 * 
	 * @param param 格式:index@config|...|index@config
	 * @return
	 */
	public Map findKeys(String param){
		Map<String,List> returnMap = new HashMap();
		List<String> tmpKeys;
		List<String> tmp;
		String config;
		Pattern findPattern = Pattern.compile(param);
		Iterator<String> itor = this.keySet().iterator();
		String keysIndex;
		while(itor.hasNext()){
			keysIndex = itor.next();
			if(findPattern.matcher(keysIndex).find()){
				tmp = this.get(keysIndex);
				if(tmp!=null){
					config = keysIndex.substring(keysIndex.indexOf("@")+1,keysIndex.length());
					tmpKeys = returnMap.get(config);
					if(tmpKeys==null)tmpKeys = new ArrayList();
					for(String realKey:tmp){
						tmpKeys.add(realKey);
					}
					returnMap.put(config, tmpKeys);
				}
			}
		}
		return returnMap;
	}
	
	/**
	 * 清除因长时间不存在的缓存索引
	 * @param key
	 * @param cacheName
	 */
	public void removeIndex(String key,String cacheName){
		if(key == null)return;
		String tmp = append(key.replaceAll("\\(.+?\\)|@.+?$|\\^.+\\^", ""),"@",cacheName);
		List tmpIndex = this.get(tmp);
		if(tmpIndex!=null){
			tmpIndex.remove(key);
		}
	}
	
	public void removeIndex(String configName){
		List<String> tmp;
		configName = append("@",configName);
		Iterator<String> itor = this.keySet().iterator();
		String keysIndex;
		while(itor.hasNext()){
			keysIndex = itor.next();
			if(keysIndex.endsWith(configName)){
				tmp = this.get(keysIndex);
				if(tmp!=null)
					tmp.clear();
				this.remove(keysIndex);
			}
		}
	}

	public void clear(){
		super.clear();
	}
	private String append(Object ... s){
		StringBuffer sb = new StringBuffer();
		for(Object _s:s)sb.append(_s);
		return sb.toString();
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		Iterator<String> itor = this.keySet().iterator();
		List<String> tmp;String key;
		while(itor.hasNext()){
			key = itor.next();
			tmp = this.get(key);
			sb.append(key).append("\r\n");
			for(String t  : tmp){
				sb.append("\t").append(t).append("\r\n");
			}
		}
		return sb.toString();
	}
	
}
