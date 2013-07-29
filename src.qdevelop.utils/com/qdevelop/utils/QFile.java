package com.qdevelop.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import com.qdevelop.utils.files.FileFilter;
import com.qdevelop.utils.files.IQFileList;
import com.qdevelop.utils.files.IQFileRead;

@SuppressWarnings({"rawtypes"})
public abstract class QFile extends QFileRead implements IQFileRead,IQFileList{
	
	public final void listFiles(String filePath){
		listFiles(new File(filePath),null);
	}
	/**
	 * 遍历文件夹
	 * @param filePath
	 * @param fileFilter
	 */
	public final void listFiles(String filePath,String fileFilter){
		listFiles(new File(filePath),new FileFilter(fileFilter));
	}
	
	public final void listFiles(File f){
		listFiles(f,null);
	}
	
	
	/**
	 * 遍历文件夹
	 * @param f
	 * @param filter
	 */
	public final void listFiles(File f,FileFilter filter){
		if(f.isDirectory()){
			File[] fs ;
			if(filter==null) fs = f.listFiles();
			else fs = f.listFiles(filter);
			for(File _f : fs){
				if(_f.isDirectory()){
					disposeFileDirectory(_f);
					listFiles(_f,filter);
				}else{
					disposeFile(_f);
				}
			}
		}else{
			disposeFile(f);
		}
	}
	/**
	 * 遍历文件后，处理文件
	 */
	public abstract void disposeFile(File f);

	/**
	 * 遍历文件后，处理文件夹
	 */
	public abstract void disposeFileDirectory(File f);

	
	public static final void writeFile(String filePath,Object obj,String encode)throws Exception{
		
		OutputStreamWriter fw=null;
		try {
			if(encode!=null)
				fw=new OutputStreamWriter(new  FileOutputStream(filePath),encode);
			else 
				fw=new OutputStreamWriter(new  FileOutputStream(filePath));
			if(obj instanceof String){
				fw.write(String.valueOf(obj));
			}else if(obj instanceof List){
				List tmp = (List)obj;
				for(Object o:tmp){
					fw.write(String.valueOf(o));
				}
			}
			System.out.println("Write File:"+filePath);
		} catch (Exception e) {
			throw e;
		}finally{
			if(fw!=null)
				fw.close();
		}

	}
	
	public final boolean isFileNewer(File f,Date date){
		return f.lastModified() > date.getTime();
	}
	
	public static final void copyFile(File src,File dst){
		InputStream in=null;
    	OutputStream out=null;
        try {
			 in = new FileInputStream(src);
			 out = new FileOutputStream(dst);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
			    out.write(buf, 0, len);
			}
		}catch (Exception e) {			
			System.out.println(e.getMessage());
		}finally{
			try {
				if(in!=null)
				in.close();
				if(out!=null)
				out.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
			
		}
	}


}
