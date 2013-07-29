package com.qdevelop.cache.utils;

public class CacheUtils {

	/**
	 * 
	 * TODO 生成索引栈的索引 
	 * 
	 * @param key
	 * @param config
	 * @return
	 */
	public static String stackKey(String key,String config){
		return new StringBuffer().append(CLang.INDEX_PRERFIX).append(config).append(stackIndex(key)).toString();
	}

	public static String itemKey(String key,String config){
		return new StringBuffer().append(config).append(key).toString();
	}

	public static String stackIndex(String key){
		return key == null ? null : key.replaceAll("\\(.+\\)|@.+$", "");
	}

	public static String getStackIndexFromKeyConfig(String keyConfig){
		if(keyConfig.indexOf("@")==-1)return null;
		return stackKey(keyConfig.substring(0,keyConfig.indexOf("@")),keyConfig.substring(keyConfig.indexOf("@")+1));
	}

}
