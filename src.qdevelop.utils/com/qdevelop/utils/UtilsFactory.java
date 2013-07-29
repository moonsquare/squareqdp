package com.qdevelop.utils;

import java.io.File;
import java.net.URL;

import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.utils.cache.ICache;

public class UtilsFactory {
	/**
	 * 公用HashMap缓存类
	 * @return
	 */
	public static ICache cacheByMap(){
		return QCache.mapCache();
	}
	/**
	 * 公用EH缓存
	 * @return
	 */
	public static ICache cacheByEH(){
		return QCache.ehCache();
	}
	
//	/**
//	 * 公用带索引的缓存，可以实现模糊删除；主用于ResultBean缓存 
//	 * @return
//	 */
//	public static ICache cacheByIndex(){
//		return QCache.indexCache();
//	}
	
	/**
	 * 配置文件工具
	 * @return
	 */
	public static QProperties properties(){
		return QProperties.getInstance();
	}
	
	public static QSource source(){
		return QSource.getInstance();
	}
	
	public static String getProjectPath(String ... others){
		StringBuffer sb = new StringBuffer();
		String root = getProjectPath();
		sb.append(root == null ? "." : root);
		for(String s:others){
			sb.append(s);
		}
		return sb.toString();
	}
	
	public static String getProjectPath(){
		if(QDevelopConstant.PROJECT_PATH!=null)return QDevelopConstant.PROJECT_PATH;
		File libsPath;
		try {
			URL url =  QSource.getInstance().getResource("qdevelop.properties");
			String webInf = url.toString().replaceAll("^file:|WEB-INF.+?$|qdevelop\\.properties", "");
			libsPath = new  File(webInf+"/WEB-INF/lib");
			if(libsPath.exists()){
				System.out.println("find project path ===> "+webInf);
				QDevelopConstant.PROJECT_PATH = toCommonPath(webInf);
				return QDevelopConstant.PROJECT_PATH;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String resourcePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		libsPath = new File(resourcePath+"/WEB-INF/lib");
		if(libsPath.exists()){
			System.out.println("find project path ===> "+resourcePath);
			QDevelopConstant.PROJECT_PATH = toCommonPath(resourcePath);
			return QDevelopConstant.PROJECT_PATH;
		}

		libsPath = new File(resourcePath+"/../lib");
		if(libsPath.exists()){
			String t = libsPath.getParentFile().getParent();
			System.out.println("find project path ===> "+t);
			QDevelopConstant.PROJECT_PATH = toCommonPath(t);
			return QDevelopConstant.PROJECT_PATH ;
		}
		return null;
	}
	
	private static String toCommonPath(String path){
		String t = path.replaceAll("\\\\", "/");
		if(!t.startsWith("/")) return "/"+t;
		return t;
	}
	
	
	/**
	 * 日志工具
	 * @return
	 */
	public static QLog log(){
		return QLog.getInstance();
	}
	
}
