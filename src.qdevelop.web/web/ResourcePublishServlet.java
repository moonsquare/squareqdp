package web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 直接由开发环境转换成生产环境的资源
 * @author Janson.Gu
 *
 */
public class ResourcePublishServlet extends HttpServlet{
	static String deployPath,queryPath; 
	
	
	public ResourcePublishServlet() {
		super();
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
	
	public void init() throws ServletException {
		deployPath = this.getServletConfig().getInitParameter("path") == null?getServletContext().getRealPath("/"):this.getServletConfig().getInitParameter("path");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6633262799968529291L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		if(queryPath==null){
			queryPath = request.getContextPath();
		}
		String uri = request.getRequestURI();
		String _v = request.getParameter("$v") == null ? "1.0" : request.getParameter("$v");
		String etag = new StringBuffer().append(uri.hashCode()).append(_v).toString();
		String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		
		ServletOutputStream stream=null;   
		BufferedInputStream input=null;   
		try{   
			response.setHeader("ETag", etag);
			stream=response.getOutputStream();
			request.setCharacterEncoding("utf-8");
			response.setContentType(getType(uri));  
			File f = new File(new StringBuffer().append("/").append(deployPath).append("/").append(uri.substring(uri.indexOf(queryPath)+queryPath.length())).toString());
			if(!f.exists()) throw new Exception(f.getAbsolutePath()+"文件不存在！");
			input =  new BufferedInputStream(new FileInputStream(f));
			int readBytes=0;   
			while((readBytes=input.read())!=-1){   
				stream.write(readBytes);   
			}   
		}catch(Exception e){   
			System.out.println(append("Root:",deployPath,"\tNot Found Resource:",e.getMessage()));   
		}finally{   
			if(stream!=null){   
				stream.close();   
			}   
			if(input!=null){   
				input.close();   
			}   
		}  
	}
	
	private String append(String ... ss){
		StringBuffer sb = new StringBuffer();
		for(String s:ss)sb.append(s);
		return sb.toString();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		doGet(request,response);
	}
	
	private HashMap<String,String>  fileType = new HashMap<String,String>();
	
	public String getType(String url){
		return fileType.get(url.substring(url.lastIndexOf(".")));
	}

}
