package com.qdevelop.utils.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import com.qdevelop.utils.UtilsFactory;

public abstract class QResoureReader {

	public void findFiles(String fileName){
		File[] libs = getEnvironmentJars();
		if(libs==null)return;
		for(File f:libs){
			if(!f.isFile())continue;
			JarFile file=null;
			try {
				file = new JarFile(f);
				Enumeration<JarEntry> entrys = file.entries();
				while(entrys.hasMoreElements()){
					JarEntry jar = entrys.nextElement();
					if(jar.getName().endsWith(fileName)){
						desposeFile(f.getName(),fileName,file.getInputStream(jar));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					if(file!=null)file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void findPath(String filePath,String filter,File[] libs){
		Pattern finder = Pattern.compile("/?"+filePath+"/");
		if(libs==null)return;
		for(File f:libs){
			if(!f.isFile())continue;
			JarFile file=null;
			try {
				file = new JarFile(f);
				Enumeration<JarEntry> entrys = file.entries();
				while(entrys.hasMoreElements()){
					JarEntry jar = entrys.nextElement();
					if(finder.matcher(jar.getName()).find() && jar.getName().endsWith(filter)){
						desposeFile(f.getName(),jar.getName(),file.getInputStream(jar));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					if(file!=null)file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void findPath(String filePath,String filter){
		findPath(filePath,filter,getEnvironmentJars());
	}

	private File[] getEnvironmentJars(){
		File libsPath  = new File(UtilsFactory.getProjectPath("/WEB-INF/lib"));
		if(libsPath.exists()){
			return libsPath.listFiles(new FileFilter(".jar"));
		}
		System.out.println("*********** not fond web libs,load from class path *************");
		File[] libs = null;
		String[] jars = System.getProperty("java.class.path").split(";");
		if(jars.length>1){
			libs = new File[jars.length-1];
			for(int i=1;i<jars.length;i++){
				libs[i-1] = new File(jars[i]);
			}
		}
		return libs;
	}

	public abstract void desposeFile(final String jarName,final String fileName,final InputStream is);
}
