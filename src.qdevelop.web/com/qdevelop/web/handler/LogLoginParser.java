package com.qdevelop.web.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;

public class LogLoginParser extends ArrayList<Map<String,String>> implements ILogParser{
	
	private static final long serialVersionUID = 3574993935130585787L;
	private Pattern filter,finder;
	int max;

	public LogLoginParser(String sys, String user, String logintime,int max){
		if(QProperties.getInstance().getProperty("QDevelop_LoginShow_Filter")!=null)
			filter = Pattern.compile(QProperties.getInstance().getProperty("QDevelop_LoginShow_Filter"));
		finder = Pattern.compile(QString.append(
				logintime==null?"":logintime,
				".+?\\[",
				sys==null?".+?":sys,
				"\\] userName:",
				user==null?"":user,
				".+?"));
		this.max = max;
	}

	@Override
	public boolean isTarget(String logContent) {
//		System.out.println(logContent);
		return finder.matcher(logContent).find() &&(filter==null || !filter.matcher(logContent).find());
	}

	@Override
	public String parser(String logContent) {
		return logContent.replaceAll("\t|Role.+?$|\\[", "").replaceAll("loginIp:|\\] userName:|\\]", "|");
	}

	/**
	 * STATUS|TIME|SYS|USER|IP
	 */
	private final String[] KEYS = new String[]{"STATUS","DATE","SYS","USER","IP"};
	@Override
	public void collect(String afterParseLog) {
		if(afterParseLog.indexOf("|")==-1)return;
		String[] tmp = afterParseLog.split("\\|");
		Map<String,String> data = new HashMap<String,String>(KEYS.length);
		for(int i=0;i<KEYS.length;i++){
			if(i==0){
				tmp[i] = tmp[i].trim().equals("INFO")?"1":"0";
			}
			data.put(KEYS[i], tmp[i].trim());
		}
		this.add(data);
	}

	@Override
	public int getMax() {
		return this.max;
	}

	@Override
	public File getLogFile() throws QDevelopException{
		return LogSourceReader.getInstance().getLoginLog();
	}

	@Override
	public int getStart() {
		return 0;
	}
}
