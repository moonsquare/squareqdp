package com.qdevelop.utils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.utils.files.FileFilter;
import com.qdevelop.utils.files.IQFileLoader;
import com.qdevelop.utils.files.QResoureReader;


/**
 * 系统文件配置读取工具
 * @author Janson.Gu
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class QProperties extends HashMap implements IQFileLoader{

	public static boolean 
	isDebug,isCacheDebug,
	isLoginWithLastURL,
	isSecurityInterceptor,
	isResourceOnlyFromJar,
	globeCache=true,
	isLoginSingle = false;

	public String system_root;
	public static final long serialVersionUID = 2611983668838094884L;
	private static QProperties _QProperties = new QProperties();
	public static QProperties getInstance(){
		return _QProperties;
	}

	public boolean isQDevelopCall(String keys){
		return String.valueOf(serialVersionUID).equals(keys);
	}

	public QProperties(){
		reload();
	}

	@Override
	public void clear() {

	}

	public String getRootPath(){
		return UtilsFactory.getProjectPath();
	}


	@Override
	public synchronized void reload() {
		super.clear();
		loadProperties("qdevelop.properties");
		try {
			File path = UtilsFactory.source().getResourceAsFile("properties");
			if(path.exists()){
				File[] files = path.listFiles(new FileFilter("*.properties"));
				for(File ff : files){
					loadProperties(ff.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		isDebug = this.getBoolean("QDevelop_Debug");
		isCacheDebug = this.getBoolean("QDevelop_Cache_Debug");
		isSecurityInterceptor = this.getBoolean("QDevelop_SecurityInterceptor"); 
		isLoginSingle = this.getBoolean("QDevelop_Security_singleLogin");
		if(QDevelopConstant.PROJECT_PATH==null)
			QDevelopConstant.PROJECT_PATH = this.getProperty("System_Root_Path");
		isResourceOnlyFromJar = this.getBoolean("QDevelop_isResourceOnlyFromJar");
		isLoginWithLastURL = this.getBoolean("QDevelop_LoginWithLastURL");
		if(getProperty("QDevelop_globe_cache")!=null)
			globeCache = this.getBoolean("QDevelop_globe_cache");
		
		if(this.getProperty("QDevelop_system_name")==null){
			if(UtilsFactory.getProjectPath() == null){
				QDevelopConstant.SYSTEM_NAME = "qdevelop4";
			}else{
				QDevelopConstant.SYSTEM_NAME = new File(UtilsFactory.getProjectPath()).getName();
			}
		}else{
			QDevelopConstant.SYSTEM_NAME = this.getProperty("QDevelop_system_name");
		}
		
		QDevelopConstant.CACHE_NAME_RESULTBEAN_TACTICS = this.getProperty("QDevelop_Config_default")==null?"s_r":this.getProperty("QDevelop_Config_default");
		loadPropertiesFromJars();
	}

	private Object getCache(String key){
		return this.get(key);
	}
	private void setCache(String key,Object value){
		this.put(key,value);
	}

	/**
	 * 将JAR包中所有的资源都加载到系统配置管理中
	 * @param fileName
	 * @param is
	 */
	private void loadPropertiesFromJars(){
		new QResoureReader(){
			@Override
			public void desposeFile(final String jarName,final String fileName,final InputStream is) {
				try {
					new QFileRead(){

						@Override
						public void disposeFileDate(String data) {
							if(data.indexOf("=")>-1){
								data = data.trim().replaceAll(" *= *", "=");
								String tmpKey = data.substring(0, data.indexOf("="));
								if(getCache(tmpKey)==null)
									setCache(tmpKey, data.substring(data.indexOf("=")+1));
							}
						}

					}.readFile(is, "utf-8");
					System.out.println(QString.append("loading Properties：",jarName,"!",fileName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.findPath("properties", ".properties");
	}

	private void loadProperties(final String filePath){
		System.out.println(QString.append("loading Properties：",filePath));
		try {
			new QFileRead(){
				String tmpKey;
				@Override
				public void disposeFileDate(String data) {
					data = data.trim().replaceAll(" *= *", "=");
					if(data.length()>0&&!data.startsWith("#")&&data.indexOf("=")>-1){
						tmpKey = data.substring(0, data.indexOf("="));
						if(data.startsWith("$include")){
							System.out.println("properties ===> "+data);
						}else{
							if(getCache(tmpKey)==null)
								setCache(tmpKey, data.substring(data.indexOf("=")+1));
						}
					}
				}

			}.readFile(UtilsFactory.source().getSourceAsStream(filePath), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean addPropertiesSource(String file){
		return true;
	}

	/**
	 * 当有值
	 * @param value
	 * @return
	 */
	public String parseValue(String value){
		if(value==null || value.indexOf("${")==-1)return value;
		String[] keys = value.replaceAll("\\}.+?\\{", "|").replaceAll("^\\$\\{|^.+\\$\\{|\\}.+?$|\\}$", "").split("\\|");
		String _val = new String(value);
		for(String key:keys){
			_val = _val.replace(QString.append("${",key,"}"), (String)this.get(key));
		}
		return _val;
	}

	//	Pattern _findParam = Pattern.compile("\\$\\{.+?\\}|\\$\\[.+?\\]");

	/**
	 * 
	 */
	public String getProperty(String key,String ... paramValue){
		String tmp = getProperty(key);
		if(tmp==null)return null;
		for(int i=0;i<paramValue.length;i++){
			if(paramValue[i]!=null)
				tmp = tmp.replace(QString.append("[",(i+1),"]"), paramValue[i]);
		}
		return tmp;
	}

	public String getProperty(String key){
		Object val = this.get(key);
		if(val==null)return null;
		String tmp = String.valueOf(val);
		if(tmp.indexOf("${")>-1){
			tmp = parseValue(tmp);
			this.put(key, tmp);
		}
		return tmp;
	}

	public String getString(String key){
		return getProperty(key);
	}

	public int getInt(String key)throws NumberFormatException {
		return (int)getDouble(key);
	}

	public double getDouble(String key) throws NumberFormatException {
		String tmp = getProperty(key);
		if(tmp==null)return -0;
		if(tmp.indexOf("*")>-1){//支持简单乘法计算
			String[] tmps = tmp.split("\\*");
			double value =1.0;
			for(int i=0;i<tmps.length;i++){
				value = value*Double.parseDouble(tmps[i].trim());
			}
			return value;
		}
		return Double.parseDouble(tmp);
	}

	public boolean getBoolean(String key){
		if(getProperty(key)==null)
			return false;
		return Boolean.parseBoolean(getProperty(key));
	}

	/**
	 * 获取复杂类型配置 <br>
	 * 配置数据源按 Json字符串格式配置<br>
	 * 取值也是按Json方式取
	 * @param propertyKey
	 * @return
	 */
	public Object getJsonValue(String propertyKey){
		if(this.get(propertyKey)!=null) return getProperty(propertyKey);
		Object tmp = getPropertyComplex(propertyKey);
		this.put(propertyKey,tmp);
		return tmp;
	}

	private Object getPropertyComplex(String propertyKey){
		if(propertyKey.indexOf(".")>-1||propertyKey.indexOf("[")>-1){
			try {
				Object tmpValue = null;
				String[] keys = propertyKey.split("\\.");
				int index=0;int idx;Object[] objs;
				for(String key:keys){
					if(index==0){
						tmpValue = this.get(key.replaceAll("\\[[0-9]\\]", "").trim().replaceAll(";$", ""));
						if(tmpValue==null)return null;
						if(!key.replaceAll("\\[[0-9]\\]", "").equals(key)){
							idx = Integer.parseInt(key.replaceAll(".+?\\[|\\]$", ""));
							objs = QJson.getObjectArrayFromJson(String.valueOf(tmpValue));
							if(idx>objs.length)return null;
							tmpValue = objs[idx];
						}else tmpValue = QJson.getMapFromJson(QJson.getJSONString(tmpValue));
					}else if(index == keys.length-1){
						tmpValue = ((Map)tmpValue).get(key.replaceAll("\\[[0-9]\\]", ""));
						if(!key.replaceAll("\\[[0-9]\\]", "").equals(key)){
							idx = Integer.parseInt(key.replaceAll(".+?\\[|\\]$", ""));
							objs = QJson.getObjectArrayFromJson(String.valueOf(tmpValue));
							if(idx>objs.length)return null;
							return objs[idx];
						}
						return tmpValue;
					}else{
						tmpValue = ((Map)tmpValue).get(key.replaceAll("\\[[0-9]\\]", ""));
						if(!key.replaceAll("\\[[0-9]\\]", "").equals(key)){
							idx = Integer.parseInt(key.replaceAll(".+?\\[|\\]$", ""));
							objs = QJson.getObjectArrayFromJson(String.valueOf(tmpValue));
							if(idx>objs.length)return null;
							tmpValue = objs[idx];
						}
					}
					index++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//		QProperties.getInstance().getJsonValue("testVale[0].aa");
		// TODO Auto-generated method stub
		//		System.out.println(QProperties.getInstance().getJsonValue("testVale[0].aa"));
		//		System.out.println(QProperties.getInstance().getComplexValue("testVale[1].bb"));
		//		System.out.println(QProperties.getInstance().getComplexValue("testVale[1].bb.cc"));
		//		System.out.println(QProperties.getInstance().getProperty("test_include_values","Janson"));
	}

}
