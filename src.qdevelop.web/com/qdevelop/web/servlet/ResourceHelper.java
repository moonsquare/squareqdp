package com.qdevelop.web.servlet;

import java.util.HashMap;

public class ResourceHelper {
	private static ResourceHelper _SourceHelper = new ResourceHelper();
	public static ResourceHelper getInstance(){
		return _SourceHelper;
	}
	private HashMap<String,String>  fileType = new HashMap<String,String>();
	public ResourceHelper(){
		fileType.put(".xml", "text/xml");
		fileType.put(".html", "text/html");
		fileType.put(".pdf", "application/pdf");
		fileType.put(".gif", "image/gif");
		fileType.put(".jpeg", "image/jpeg");
		fileType.put(".png", "image/png");
		fileType.put(".jpg", "image/jpeg");
		fileType.put(".mp3", "audio/mpeg");
		fileType.put(".swf", "application/x-shockwave-flash");
		fileType.put(".doc", "application/msword");
		fileType.put(".xls", "application/vnd.ms-excel");
		fileType.put(".ppt", "application/vnd.ms-powerpoint");
		fileType.put(".txt", "text/plain");
		fileType.put(".js", "text/javascript");
		fileType.put(".css", "text/css");
	}
	
	public String getType(String url){
		return fileType.get(url.substring(url.lastIndexOf(".")));
	}
}
