package com.qdevelop.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import web.QD;

import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.UtilsFactory;

/**
 * 直接由开发环境转换成生产环境的资源
 * 1.1 	增加对JS/CSS的GZIP压缩
 * 		增加对文件的按编码读取
 * @author Janson.Gu
 *
 */
public class ResourcePublishServlet extends HttpServlet{
	static String deployPath,queryPath,encode="utf-8";


	public ResourcePublishServlet() {
		super();
	}

	public void init() throws ServletException {
		deployPath = this.getServletConfig().getInitParameter("path") == null?UtilsFactory.getProjectPath():this.getServletConfig().getInitParameter("path");
		String uriLoopConfig = QProperties.getInstance().getProperty("resource_loop_uri");
		if(uriLoopConfig!=null){
			QD.loopResource = uriLoopConfig.split("\\|");
			QD.loopSize = QD.loopResource.length;
		}
		QD.version = QProperties.getInstance().getProperty("res_version") == null?"1.0":QProperties.getInstance().getProperty("res_version");
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -6633262799968529291L;
	private void initApplication(HttpServletRequest request){
		if(queryPath==null){
			queryPath = request.getContextPath();
			if(QD.loopResource == null){
				QD.loopResource = new String[]{queryPath};
				QD.loopSize = QD.loopResource.length;
			}
		}
	}
	
	/**
	 * 判定是否需要按编码来压缩资源
	 * @param uri
	 * @return
	 */
	private boolean isTxtFile(String uri){
		return uri.endsWith(".js")||uri.endsWith(".css");
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		String uri = request.getRequestURI();
		String _v = request.getParameter("$v") == null ? QD.version : request.getParameter("$v");
		String etag = String.valueOf((uri.hashCode()+_v.hashCode()));
//				new StringBuffer().append(uri.hashCode()+_v.hashCode()).toString();
		String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		initApplication(request);
		StringBuffer memCollect = null;
		OutputStream stream = null ;
		try{   
			response.setHeader("ETag", etag);
			request.setCharacterEncoding(encode);
			response.setContentType(ResourceHelper.getInstance().getType(uri));  
			
			boolean isTxtFile = isTxtFile(uri);
			
			if(isTxtFile){
				String encoding = request.getHeader("Accept-Encoding");
				if (encoding != null && encoding.indexOf("gzip") != -1){  
					response.setHeader("Content-Encoding" , "gzip");  
					stream = new GZIPOutputStream(response.getOutputStream());  
				}else if (encoding != null && encoding.indexOf("compress") != -1){  
					response.setHeader("Content-Encoding" , "compress");  
					stream = new ZipOutputStream(response.getOutputStream());  
				}else {  
					stream = response.getOutputStream();  
				}  
			}else{
				stream=response.getOutputStream();
			}

			String file =  URLDecoder.decode(uri.substring(uri.lastIndexOf("/")+1),encode);
			
			String path = new StringBuffer().append("/").append(deployPath).append("/").append(uri.substring(uri.indexOf(queryPath)+queryPath.length(),uri.lastIndexOf("/"))).toString();
			if(file.indexOf("*")==-1 && file.indexOf("|")==-1){
				sendJs(stream,new File(path,file),isTxtFile,memCollect);
			}else if(file.indexOf("|")>-1){
				if(QProperties.isDebug)System.out.println(file);
				String type = uri.substring(uri.lastIndexOf("."));
				String [] files = file.substring(0,file.lastIndexOf(".")).split("\\|");
				for(String f : files){
					if(f.indexOf("^")==-1){
						sendJs(stream,new File(path,new StringBuffer().append(f).append(type).toString()),isTxtFile,memCollect);
					}else{
						sendJs(stream,new File(path,new StringBuffer().append(f.replaceAll("\\^", "/")).append(type).toString()),isTxtFile,memCollect);
					}
				}
			}else if(file.indexOf("*")>-1){
				if(QProperties.isDebug)System.out.println(file);
				File p = new File(path);
				if(p.exists()){
					String type = uri.substring(uri.lastIndexOf("."));
					File[] files = p.listFiles();
					String fName = file.substring(file.lastIndexOf("."));
					if(fName.equals("*")){
						for(File f : files){
							if(f.getName().endsWith(type)){
								sendJs(stream,f,isTxtFile,memCollect);
							}
						}
					}else{
						if(file.startsWith("*")){
							String filter = file.substring(1);
							for(File f : files){
								if(f.getName().endsWith(filter)){
									sendJs(stream,f,isTxtFile,memCollect);
								}
							}
						}else if(fName.endsWith("*")){
							String filter = fName.substring(fName.length()-1);
							for(File f : files){
								if(f.getName().endsWith(type) && f.getName().startsWith(filter)){
									sendJs(stream,f,isTxtFile,memCollect);
								}
							}
						}else{
							Pattern _check = Pattern.compile(fName.replaceAll("\\*", ".+"));
							for(File f : files){
								if(f.getName().endsWith(type) && _check.matcher(f.getName()).find()){
									sendJs(stream,f,isTxtFile,memCollect);
								}
							}
						}
					}
				}
			}
		}catch(Exception e){   
			e.printStackTrace();   
		}finally{   
			if(stream!=null){   
				stream.close();   
			}   
		}  
	}

	private void sendJs(OutputStream stream,File f,boolean txtFile,StringBuffer memcached) throws IOException{
		if(!f.exists()){
			System.out.println("[文件不存在]\t"+f.getAbsolutePath());
			return;
		}
		InputStream input=null; 
		try {
//			if(txtFile)
//				input =  new BufferedReader(new InputStreamReader(new FileInputStream(f),encode));
//			else 
//				input =  new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			input =  new BufferedInputStream(new FileInputStream(f));
			int readBytes=0;   
			while((readBytes=input.read())!=-1){   
				stream.write(readBytes);   
				if(memcached!=null)memcached.append(readBytes);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(input!=null)
				input.close();   
		}
	}


	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		doGet(request,response);
	}

}
