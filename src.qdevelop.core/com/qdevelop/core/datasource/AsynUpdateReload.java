package com.qdevelop.core.datasource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.qdevelop.utils.QLog;
import com.qdevelop.utils.files.TailFile;

public class AsynUpdateReload extends ArrayList<String> implements Runnable{
	private Logger system = QLog.getInstance().getLog("system");
	private Logger ayscRunning = QLog.getInstance().getLog("asynRunning");
	private int size=0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1398321037864129332L;
	String reloadDate;
	public AsynUpdateReload(String reloadDate){
		this.reloadDate = reloadDate;
	}
	public AsynUpdateReload(){
	}

	@Override
	public void run() {
		String ayncRunPath = QLog.getInstance().getLogConfig("log4j.appender.asynRunning.file")+(reloadDate==null?"":"."+reloadDate);
		String ayncSqlPath = QLog.getInstance().getLogConfig("log4j.appender.asynSql.file")+(reloadDate==null?"":"."+reloadDate);

		if(!new File(ayncRunPath).exists() || !new File(ayncSqlPath).exists())return;

		final HashMap<String,Boolean> tmp = new HashMap<String,Boolean>();
		new TailFile(){
			Pattern clean = Pattern.compile("\\(.+\\)|\\[.+\\]| ");
			@Override
			public void getReadLine(String lineStr) {
				if(lineStr!=null && lineStr.indexOf("[executed]")>-1 && lineStr.indexOf("[INFO ]")>-1){
					tmp.put(clean.matcher(lineStr).replaceAll(""), true);
				}
			}
			@Override
			public boolean isBreak(String lineStr) {
				return false;
			}

		}.read(ayncRunPath, QLog.getInstance().getLogConfig("log4j.appender.asynRunning.Encoding"), 0, 100);
		loadNoRunSql(tmp,ayncSqlPath,0,200,0);
		tmp.clear();
		int idx = this.size();
		if(idx>0){
			HashMap<String,ArrayList<String>> collect = new HashMap<String,ArrayList<String>>();
			while(idx-->0){
				String logger = this.get(idx);
				system.info(new StringBuffer().append("[sync] ").append(logger).toString());
				String[] tt = logger.split("--");
				if(tt.length==3){
					size++;
					ArrayList<String> array = collect.get(tt[1]);
					if(array==null){
						array = new ArrayList<String>();
						collect.put(tt[1], array);
					}
					array.add(tt[2]);
					ayscRunning.info(new StringBuffer().append("[executed] ").append(tt[0]).append(" (load running)").toString());
				}
			}
			Iterator<Entry<String, ArrayList<String>>> itor = collect.entrySet().iterator();
			while(itor.hasNext()){
				Entry<String, ArrayList<String>> item = itor.next();
				DataBaseFactory.getInstance().update(item.getKey(), item.getValue(), null);
			}
			collect.clear();
		}
		this.clear();
	}
	
	public int getSize(){
		return size;
	}

	public void loadNoRunSql(final HashMap<String,Boolean> monitor,String fileName,int begin,int end,final int idx){
		try {
			AsynUpdateReloadSQL aursql = new AsynUpdateReloadSQL(monitor,fileName,begin,end,idx,this);
			aursql.read(fileName, QLog.getInstance().getLogConfig("log4j.appender.asynSql.Encoding"), begin, end);
			if(!aursql.isBreak(null)){
				loadNoRunSql(monitor,fileName,end+1,end*2,aursql.getIdx());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public static void main(String[] args) {
		new AsynUpdateReload(null).run();
	}
}
