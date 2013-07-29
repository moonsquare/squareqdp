package com.qdevelop.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.utils.files.IQFileLoader;
import com.qdevelop.utils.files.QResoureReader;

@SuppressWarnings("unchecked")
public class QLog implements IQFileLoader{
	public static Pattern isArgs;
	private ILog iLog;
	private static QLog _QLog = new QLog();
	public static QLog getInstance(){return _QLog;}
	private Map<String,Logger> logCache;
	private final String[] defaultLog = new String[]{"operater","sql","system","security"};
	private Properties logProps;
	public QLog(){
		if(logCache==null)reload();
	}

	@Override
	public void clear() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public synchronized void reload() {

		QProperties.getInstance();
		isArgs = Pattern.compile("(\\{)SYSNAME(\\})");

		final Properties props = new Properties();
		try {
			InputStream is = QSource.getInstance().getSourceAsStream("qdevelop.log.properties"); 
			if(is!=null)props.load(is);
			Iterator itor = props.keySet().iterator();
			while(itor.hasNext()){
				String key = (String)itor.next();
				String value = props.getProperty(key);
				props.setProperty(key, formatter(value));
			}
		} catch (Exception e) {
			System.out.println("not found project log4j config [ qdevelop.log.properties]");
		}

		try {	
			new QResoureReader(){
				@Override
				public void desposeFile(final String jarName,final String fileName,final InputStream is) {
					System.out.println("load log4j properties from jar "+jarName+"!"+fileName);
					Properties pro = new Properties();
					try {
						pro.load(is);
						Iterator itor = pro.keySet().iterator();
						while(itor.hasNext()){
							String key = (String)itor.next();
							String value = pro.getProperty(key);
							if(key ==null || value==null || props.containsKey(key) )continue;
							props.setProperty(key, formatter(value));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}.findFiles("qdevelop.log.properties");

			PropertyConfigurator.configure(props);
			logProps = props;

			if(logCache!=null)logCache.clear();
			logCache = new HashMap();
			for(String _l:defaultLog){
				logCache.put(_l, Logger.getLogger(_l));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public String getLogConfig(String key) {
		return logProps.getProperty(key);
	}

	private String formatter(String value){
		if(!isArgs.matcher(value).find())return value;
		return value.replace("{SYSNAME}", QDevelopConstant.SYSTEM_NAME);
	}


	public Logger getLog(String logName){
		if(logCache.get(logName)==null){
			logCache.put(logName, Logger.getLogger(logName));
		}
		return logCache.get(logName);
	}

	public void info(String logName,Object logInfo){
		getLog(logName).info(toLogger(logInfo));
		if(iLog!=null)iLog.log(logName, ILog.INFO_LEVEL, logInfo, null, null);
	}
	public void warn(String logName,Object logInfo){
		getLog(logName).warn(toLogger(logInfo));
		if(iLog!=null)iLog.log(logName, ILog.WARN_LEVEL, logInfo, null, null);
	}
	public void debug(String logName,Object logInfo){
		getLog(logName).debug(toLogger(logInfo));
		if(iLog!=null)iLog.log(logName, ILog.DEBUG_LEVEL, logInfo, null, null);
	}
	public void error(String logName,Object logInfo){
		errorPrint(logInfo);
		getLog(logName).error(toLogger(logInfo));
		if(iLog!=null)iLog.log(logName, ILog.ERROR_LEVEL, logInfo, null, null);
	}

	public void operaterLogger(Object operaterInfo){
		logCache.get(defaultLog[0]).info(toLogger(operaterInfo));
		if(iLog!=null)iLog.log("operater", ILog.INFO_LEVEL, operaterInfo, null, null);
	}

	public void operaterError(Object operaterInfo){
		logCache.get(defaultLog[0]).error(toLogger(operaterInfo));
		if(iLog!=null)iLog.log("operater", ILog.ERROR_LEVEL, operaterInfo, null, null);
	}

	public void sqlError(String sqlInfo){
		errorPrint(sqlInfo);
		logCache.get(defaultLog[1]).info(toLogger(sqlInfo));
		if(iLog!=null)iLog.log("sql", ILog.ERROR_LEVEL, sqlInfo, null, null);
	}

	public void sqlLogger(String sqlInfo){
		logCache.get(defaultLog[1]).info(toLogger(sqlInfo));
		if(iLog!=null)iLog.log("sql", ILog.INFO_LEVEL, sqlInfo, null, null);

	}

	public void sqlLogger(DBQueryBean sqlInfo){
		if(!sqlInfo.isLog)return;
		if(sqlInfo.isSingleSQL()){
			logCache.get(defaultLog[1]).info(new StringBuffer().append("[").append(sqlInfo.sqlIndex).append("] [").append(sqlInfo.connect).append("] [").append(QDevelopConstant.SYSTEM_NAME).append("] [").append(sqlInfo.user==null?"":sqlInfo.user).append(sqlInfo.allCount > 0 ? "":"-"+sqlInfo.allCount).append("] ").append(sqlInfo.getLogSql()).toString());
			if(iLog!=null)iLog.log("sql", ILog.INFO_LEVEL, sqlInfo.getLogSql(), sqlInfo, null);
		}else{
			Logger log = logCache.get(defaultLog[1]);
			for(String sql : sqlInfo.getSqls()){
				log.info(new StringBuffer().append("[").append(sqlInfo.sqlIndex).append("] [").append(sqlInfo.connect).append("] [").append(QDevelopConstant.SYSTEM_NAME).append("] [").append(sqlInfo.user==null?"":sqlInfo.user).append("|").append(sqlInfo.userId).append("] ").append(sql).toString());
				if(iLog!=null)iLog.log("sql", ILog.INFO_LEVEL, sql, sqlInfo, null);
			}
		}
	}

	public void sqlError(DBQueryBean sqlInfo){
		if(sqlInfo==null)return;
		if(sqlInfo.isSingleSQL()){
			errorPrint(sqlInfo.getLogSql());
			logCache.get(defaultLog[1]).error(new StringBuffer().append("[").append(sqlInfo.sqlIndex).append("] [").append(sqlInfo.connect).append("] [").append(QDevelopConstant.SYSTEM_NAME).append("] [").append(sqlInfo.user).append("] ").append(sqlInfo.getLogSql()).toString());
			if(iLog!=null)iLog.log("sql", ILog.ERROR_LEVEL, sqlInfo.getLogSql(), sqlInfo, null);
		}else{
			Logger log = logCache.get(defaultLog[1]);
			for(String sql : sqlInfo.getSqls()){
				errorPrint(sql);
				log.error(new StringBuffer().append("[").append(sqlInfo.sqlIndex).append("] [").append(sqlInfo.connect).append("] [").append(QDevelopConstant.SYSTEM_NAME).append("] [").append(sqlInfo.user).append("] ").append(sql).toString());
				if(iLog!=null)iLog.log("sql", ILog.ERROR_LEVEL, sql, sqlInfo, null);
			}
		}
	}

	public void securityLogger(Object securityInfo){
		logCache.get(defaultLog[3]).info(toLogger(securityInfo));
		if(iLog!=null)iLog.log("security", ILog.INFO_LEVEL, securityInfo, null, null);
	}
	public void securityError(Object securityInfo){
		logCache.get(defaultLog[3]).error(toLogger(securityInfo));
		if(iLog!=null)iLog.log("security", ILog.ERROR_LEVEL, securityInfo, null, null);
	}

	public void systemLogger(Object info){
		logCache.get(defaultLog[2]).info(toLogger(info));
		if(iLog!=null)iLog.log("system", ILog.INFO_LEVEL, info, null, null);
	}

	public void systemWarn(Object info){
		logCache.get(defaultLog[2]).warn(toLogger(info));
		if(iLog!=null)iLog.log("system", ILog.WARN_LEVEL, info, null, null);

	}

	public void systemDebugger(Object info){
		logCache.get(defaultLog[2]).debug(toLogger(info));
		if(iLog!=null)iLog.log("system", ILog.DEBUG_LEVEL, info, null, null);

	}

	public void systemError(Object info,Throwable e){
		errorPrint(info);
		logCache.get(defaultLog[2]).error(toLogger(info),e);
		if(iLog!=null)iLog.log("system", ILog.ERROR_LEVEL, info, null, e);

	}
	public void systemError(Object info){
		errorPrint(info);
		logCache.get(defaultLog[2]).error(toLogger(info));
		if(iLog!=null)iLog.log("system", ILog.ERROR_LEVEL, info, null, null);

	}
	private static SimpleDateFormat _defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private void errorPrint(Object info){
		System.out.println(new StringBuffer().append("[ERROR] ").append(_defaultFormat.format(new Date())).append("\t").append(info.toString()));
	}

	private String toLogger(Object info){
		if(QDevelopConstant.SYSTEM_NAME == null)return info.toString();
		return new StringBuffer().append("[").append(QDevelopConstant.SYSTEM_NAME).append("] ").append(info).toString();
	}
}
