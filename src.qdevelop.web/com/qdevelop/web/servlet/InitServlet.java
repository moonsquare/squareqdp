package com.qdevelop.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


@SuppressWarnings("serial")
public class InitServlet extends HttpServlet {

	public InitServlet() {
		super();
	}

	public void init() throws ServletException {
		//		RootPath.serverRoot = this.getServletConfig().getInitParameter("path");
		//		if(RootPath.serverRoot == null || RootPath.serverRoot.trim().length()<4)
		//		RootPath.serverRoot = getServletContext().getRealPath("/");
	}

	public void destroy(){

	}

}
