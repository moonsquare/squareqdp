package com.qdevelop.utils.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.qdevelop.utils.QFile;

public class FilesZip extends QFile implements IQFileList{
	public static void main(String[] args) {
		if(args==null||args.length==0) 
			FilesZip.test();
		else if(args.length==1)
			FilesZip.compress(args[0], null);
		else if(args.length==2)
			FilesZip.compress(args[0], args[1]);
		else if(args.length==3){
			FilesZip.compress(args[0], args[1], args[2]);
		}
	}    

	public static void compress(String path,String filter){
		new FilesZip().run(path, filter);
	}
	public static void compress(String path,String filter,String maxsize){
		FilesZip fz = new FilesZip();
//		fz.setMaxSize(maxsize.toLowerCase());
		fz.run(path, filter);
	}
	public static void test(){
		FilesZip.zipRootPath = "zaozhuang";
		FilesZip.compress("E:\\workspace\\zaozhuang\\WebRoot", "!*.svn|!*.bak");
	}

	private ZipOutputStream zos;
	private int position = 0,zipIdx=0; 
	private File outFile;
	private File source;
//	private long maxSize = -1;
	public static String zipRootPath;

//	public void setMaxSize(String size){
//		try {
//			if(size.indexOf("m")>-1){
//				maxSize = Long.parseLong(size.replaceAll("m|b", ""))*1024*1024;
//			}else if(size.indexOf("k")>-1){
//				maxSize = Long.parseLong(size.replaceAll("k|b", ""))*1024;
//			}else{
//				maxSize = Long.parseLong(size);
//			}
//		} catch (NumberFormatException e) {
//			maxSize = -1;
//		}
//	}

	public void run(String path,String filter){
		try {
			source = new File(path);
			if(!source.exists()){
				System.out.println(source.getAbsolutePath()+"文件不存在！");
				return;
			}
			position = source.getAbsolutePath().length();
			if(zipRootPath==null)
				zipRootPath = source.getName();
			init(source);
			listFiles(source,filter==null?null:new FileFilter(filter));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(zos!=null){
				try {
					zos.finish();
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	private void init(File _path){
		outFile = new File(_path.getParent()+"/"+zipRootPath.replaceAll("\\..+?$", "")+".part"+(zipIdx++)+".zip");
		try {
			if(zos!=null){
				zos.finish();
				zos.close();
			}
			zos = new ZipOutputStream(new FileOutputStream(outFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("OUT FILE:"+outFile.getAbsolutePath());
	}

	@Override
	public void disposeFile(File f) {
		if(f.length()==0)return;
		System.out.println(f.getAbsolutePath());
		ZipEntry entry = new ZipEntry(zipRootPath+f.getAbsolutePath().substring(position).replaceAll("\\\\", "/"));
		InputStream is = null;
		try{
			//将条目保存到Zip压缩文件当中
			zos.putNextEntry(entry);
			//从文件输入流当中读取数据，并将数据写到输出流当中.
			is = new FileInputStream(f);            
			int length = 0;
			int bufferSize = (int)f.length();
			byte[] buffer = new byte[bufferSize];
			while((length=is.read(buffer,0,bufferSize))>=0){
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
		}catch(IOException ex){
			ex.printStackTrace();
		} finally {
			try{
				if(is != null)is.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}  
	}

	@Override
	public void disposeFileDirectory(File f) {
		String zipPath = zipRootPath+f.getAbsolutePath().substring(position).replaceAll("\\\\", "/")+"/";
		ZipEntry entry = new ZipEntry(zipPath);
		try {
			zos.putNextEntry(entry);
			zos.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void disposeFileDate(String data) {

	}



}
