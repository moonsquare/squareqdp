package com.qdevelop.utils;

import java.io.File;

import com.qdevelop.utils.files.FileFilter;

public abstract class QFileList {
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

}
