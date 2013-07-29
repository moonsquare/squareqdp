package com.qdevelop.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.UtilsFactory;

public class ResourceServlet extends HttpServlet{

	private static final long serialVersionUID = 2709959843866468423L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		ServletOutputStream stream=null;   
		BufferedInputStream input=null;   
		String uri = request.getRequestURI();
		try{   
			String etag = String.valueOf(uri.hashCode());
			String ifNoneMatch = request.getHeader("If-None-Match");
			if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			response.setHeader("ETag", etag);
			
			stream=response.getOutputStream();
			if(uri.equalsIgnoreCase("js")||uri.equalsIgnoreCase("css"))
				request.setCharacterEncoding("utf-8");
			response.setContentType(ResourceHelper.getInstance().getType(uri));   
			String file = uri.substring(uri.indexOf("QDevelop")+9);
			if(QProperties.isResourceOnlyFromJar){
				input=new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));   
			}else{
				File f = new File(UtilsFactory.getProjectPath("/QDevelop/",file));
				if(f.exists()){
//					File 
					input =  new BufferedInputStream(new FileInputStream(f));
				}else {
					input=new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));
				}
			}
			int readBytes=0;   
			while((readBytes=input.read())!=-1){   
				stream.write(readBytes);   
			}   
		}catch(Exception e){   
			System.out.println(QString.append("Not Found Resource:",uri));   
		}finally{   
			if(stream!=null){   
				stream.close();   
			}   
			if(input!=null){   
				input.close();   
			}   
		}   

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		doGet(request,response);
	}
}
