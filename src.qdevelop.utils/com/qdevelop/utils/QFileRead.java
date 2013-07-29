package com.qdevelop.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class QFileRead {
	public final void readFile(File filePath,String encode) throws Exception{
		readFile(new FileInputStream(filePath),encode);
	}
	public final void readFile(String filePath,String encode) throws Exception{
		readFile(new FileInputStream(filePath),encode);
	}
	
	public final void readFile(InputStream fileStream,String encode) throws Exception{
		init();
		InputStreamReader read = null;
		try {
			if(encode!=null)
				read = new InputStreamReader (fileStream,encode);   
			else
				read = new InputStreamReader (fileStream);   
			BufferedReader reader=new BufferedReader(read);   
			String line;   
			while ((line = reader.readLine()) != null) {   
				disposeFileDate(line);   
			}
		} catch (Exception e) {
			throw e;
		}finally{
			if(read!=null)
				read.close();
		}

	}
	/**
	 * 读取文件后，处理具体的文件每行数据
	 */
	public abstract void disposeFileDate(String data);
	
	public void init(){
		
	}
}
