package com.qdevelop.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.struts2.ServletActionContext;

import com.qdevelop.core.standard.IDownLoad;

public class DownLoadAction extends QDevelopAction implements IDownLoad{

	private static final long serialVersionUID = 6885955546304012989L;
	private String inputPath;
	private String fileName,outName;

	public String getOutName() {
		return outName;
	}

	public void setOutName(String outName) {
		this.outName = outName;
	}

	public String getFileName() {
		return outName==null?fileName:outName;
	}

	public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	private String getFileWithInputPath(){
		if(inputPath!=null && inputPath.length()>1){
			return new StringBuffer().append(inputPath).append(inputPath.endsWith("/")?"":"/").append(fileName).toString();
		}
		return fileName;
	}

	public InputStream getDownloadFile(){
		try {
			File src = new File(getFileWithInputPath());
			if(src.exists())
				return new FileInputStream(src);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return ServletActionContext.getServletContext().getResourceAsStream(getFileWithInputPath());
	}

	public String getDownloadChineseFileName() {
		String downloadChineseFileName = getFileName().substring(getFileName().lastIndexOf("/")+1);
		try {
			downloadChineseFileName = new String(downloadChineseFileName.getBytes(), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return downloadChineseFileName;
	}

	public String execute() throws Exception{
		return SUCCESS;
	}
	public static void main(String[] args) {
		System.out.println("D:\\WEB-SERVER\\tomcat\\download_tmps\\1328778124254.csv".indexOf("\\"));
		
	}
}
