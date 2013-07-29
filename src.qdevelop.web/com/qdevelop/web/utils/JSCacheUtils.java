package com.qdevelop.web.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

/**
 * 设置浏览器缓存工具类
 * @author Janson.Gu
 *
 */
public class JSCacheUtils {
	
	public static boolean isCached(String etag){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/javascript");   
		String ifNoneMatch = ServletActionContext.getRequest().getHeader("If-None-Match");
		if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		}
		response.setHeader("ETag", etag);
		return false;
	}
	
	public static boolean isCached(String etag,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("text/javascript");   
		String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		}
		response.setHeader("ETag", etag);
		return false;
	}
}
