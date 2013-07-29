package com.qdevelop.lang;


/**
 * 常量类 
 * @author Janson.Gu
 *
 */
public class QDevelopConstant {
	
	public static final String YEAR = "yyyy"; 
	public static final String MONTH = "yy/MM"; 
	public static final String DAY = "MM/dd"; 
	public static final String HOUR = "HH(dd)"; 
	public static final String MINUTE = "HH:mm"; 
	public static final String SECOND = "mm:ss"; 
	
	/**数据库缓存策略，对应的配置文件的名称**/
	public static String CACHE_NAME_RESULTBEAN_TACTICS = "s_";
	
	/**图像结果缓存策略，对应的配置文件的名称**/
	public static final String CACHE_NAME_JFREECHART_TACTICS = "QD_Chart";
	
	/**SQL配置缓存策略，对应的配置文件的名称**/
	public static final String CACHE_NAME_SQLMODEL_TATICS = "QD_SQL";
	
	/**数据结构配置缓存策略，对应的配置文件的名称**/
	public static final String CACHE_NAME_DBSTRUTS_TATICS = "QDevelopCache_Data_Struts_Tactics";
	
	/**默认配置缓存策略，对应的配置文件的名称**/
	public static final String CACHE_NAME_DEFAULT_TATICS = "QdevelopDefalut";
	
	public static final String AUTO_SEARCH_MARK = "1>0";
	
	/**用户缓存KEY***/
	public static final String USER_SESSION_KEY = "QDEVELOP_LOGIN_SESSION_KEY";
	
	public static final String USER_SINGLE_LOGIN_KEY = "USER_SINGLE_LOGIN_KEY";
	
	public static final String WEB_ONLINE_CHECK_KEY = "WEB_ONLINE_CHECK_KEY";
	
	public static final String WEB_FOCUS_URL_KEY = "_FOCUS_URL_SESSION_KEY";
	
	public static final String WEB_CHECK_CODE = "checkCode";
	
	public static final String WEB_FOCUS_URL_FUNCTION_KEY = "WEB_FOCUS_URL_FUNCTION_KEY";
	
	public static final String WEB_FOCUS_FUNCTION_NAME_KEY = "WEB_FOCUS_FUNCTION_NAME_KEY";
	
	public static final String WEB_FOCUS_VALIDATE_INFO = "WEB_FOCUS_VALIDATE_INFO";
	
	public static final String _QD_CACHE="QDEVELOP_CACHE";
	public static final String _QD_MAC_CACHE = "MAC_CACHE_KEY";
	
	public static final String LOG_SECURITY = "security";
	public static final String LOG_DEVELOP = "qdevelop";
	public static final String LOG_SQL = "sql";
	
	public static final String JAAS_VALIDATOR_CACHE_KEY = "JAAS_VALIDATOR_CACHE_KEY";
	public static final String JAAS_GET_VALIDATOR_CACHE_KEY = "JAAS_GET_VALIDATOR_CACHE_KEY";
	
	public static final String DEFAULT_UNIQUEKEY = "ID";
	
	public static String LOG_HEADER,LOG_USER,PROJECT_PATH,SYSTEM_NAME="QDevelop";

	
}
