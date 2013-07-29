package com.qdevelop.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.qdevelop.lang.QDevelopException;

/**
 * 
 * 获取系统资源类
 * @author Janson.Gu
 *
 */
public class QSource {
	private static  QSource _QSource = new QSource();
	public static QSource getInstance(){return _QSource;}

	public  String getRootPath(){
		 return UtilsFactory.getProjectPath();
	}

	/**
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public  URL getResource(String resource)  throws Exception{
		File tmp = new File(resource);
		if(tmp.exists())return tmp.toURI().toURL();
		boolean hasLeadingSlash = resource.startsWith( "/" );
		String stripped = hasLeadingSlash ? resource.substring(1) : resource;
		URL _url = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if ( classLoader != null ) {
			_url = classLoader.getResource( resource );
			if ( _url == null && hasLeadingSlash ) {
				_url = classLoader.getResource( stripped );
			}
		}
		if ( _url == null ) {
			_url = ClassLoader.getSystemResource( resource );
		}
		if ( _url == null && hasLeadingSlash ) {
			_url = ClassLoader.getSystemResource( stripped );
		}
		if ( _url == null ) {
			throw new Exception( resource + " not found!" );
		}
		return _url;
	}

	//	private void appendToList(ArrayList<File> fileList,File f,String subfix,HashMap<String,Object> tmp){
	//		if(f.isDirectory()){
	//			File[] files = f.listFiles();
	//			for(File ff : files ){
	//				if(tmp.get(ff.getAbsolutePath())==null && ff.getName().endsWith(subfix)){
	//					fileList.add(ff);
	//					tmp.put(ff.getAbsolutePath(), new Object());
	//				}
	//			}
	//		}else{
	//			if( tmp.get(f.getAbsolutePath())==null && f.getName().endsWith(subfix)){
	//				fileList.add(f);
	//				tmp.put(f.getAbsolutePath(), new Object());
	//			}
	//		}
	//	}

	/**
	 * 
	 * TODO 获取文件列表 
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public List<InputStream> getResources(String resource) throws QDevelopException{
		ArrayList<InputStream> fileList = new ArrayList<InputStream>();
		try {
			InputStream stream = null;
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			if ( classLoader != null ) {
				stream = classLoader.getResourceAsStream( resource );
				if(stream!=null)fileList.add(stream);
			}
			if ( stream == null ) {
				stream = ClassLoader.getSystemResourceAsStream( resource );
				if(stream!=null)fileList.add(stream);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileList;
	}


//	public List<QSourceBean> getResourceFromJars(String fileName){
//		File[] libs=null;
//		File libsPath = new File(this.getRootPath()+"/WEB-INF/lib");
//		if(libsPath.exists()){
//			libs = libsPath.listFiles(new FileFilter(".jar"));
//		}else{
//			String[] jars = System.getProperty("java.class.path").split(";");
//			if(jars.length>1){
//				libs = new File[jars.length-1];
//				for(int i=1;i<jars.length;i++){
//					libs[i-1] = new File(jars[i]);
//				}
//			}
//		}
//		if(libs==null)return null;
//		ArrayList<QSourceBean> result = new ArrayList<QSourceBean>();
//		for(File f:libs){
//			JarFile file=null;
//			try {
//				file = new JarFile(f);
//				Enumeration<JarEntry> entrys = file.entries();
//				while(entrys.hasMoreElements()){
//					JarEntry jar = entrys.nextElement();
//					if(jar.getName().endsWith(fileName)){
//						result.add(new QSourceBean(f.getName(),fileName,file.getInputStream(jar)));
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}finally{
//				try {
//					if(file!=null)file.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return result;
//	}
	

	/**
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public  InputStream getSourceAsStream(String resource) throws Exception{
		File tmp = new File(resource);
		if(tmp.exists())return new FileInputStream(tmp);
		boolean hasLeadingSlash = resource.startsWith( "/" );
		String stripped = hasLeadingSlash ? resource.substring(1) : resource;
		InputStream stream = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if ( classLoader != null ) {
			stream = classLoader.getResourceAsStream( resource );
			if ( stream == null && hasLeadingSlash ) {
				stream = classLoader.getResourceAsStream( stripped );
			}
		}
		if ( stream == null ) {
			stream = ClassLoader.getSystemResourceAsStream( resource );
		}
		if ( stream == null && hasLeadingSlash ) {
			stream = ClassLoader.getSystemResourceAsStream( stripped );
		}
		if ( stream == null ) {
			throw new Exception( resource + " not found!" );
		}
		return stream;
	}

	/**
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public  File getResourceAsFile(String resource) throws Exception{
		File tmp = new File(resource);
		if(tmp.exists())return tmp;
		URL url = getResource(resource);
		if(url==null)return null;
		return new File(url.toURI());
	}
}
