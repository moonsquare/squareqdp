package com.qdevelop.utils;

import java.io.InputStream;

public class QSourceBean {
	private InputStream inputStream;
	private String fileName;
	private String jarName;
	
	public QSourceBean(String jarName,String fileName,InputStream inputStream){
		this.jarName = jarName;
		this.fileName = fileName;
		this.inputStream = inputStream;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getJarName() {
		return jarName;
	}
	public void setJarName(String jarName) {
		this.jarName = jarName;
	}
	
}
