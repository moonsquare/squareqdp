package com.qdevelop.core.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.qdevelop.utils.files.TailFile;

public class AsynUpdateReloadSQL extends TailFile{
	HashMap<String,Boolean> monitor;String fileName;int begin, end,  idx, mSize;
	ArrayList<String> collect;
	public AsynUpdateReloadSQL( HashMap<String,Boolean> monitor,String fileName,int begin,int end,final int idx,ArrayList<String> collect){
		this.monitor=monitor;
		this.fileName=fileName;
		this.begin=begin;
		this.end=end;
		this.idx=idx;
		mSize = monitor.size();
		this.collect = collect;
		//		System.out.println(begin +" --- " + end + " ---- "+idx+"-"+mSize);
	}

//	Pattern split = Pattern.compile("\\[|\\]");
	Pattern target = Pattern.compile("^\\[.+\\]$");
	String lastMark;

	@Override
	public void getReadLine(String lineStr) {
		if(lineStr!=null ){
			String[] tmp = getLogSql(lineStr);
			if(tmp != null){
				if(monitor.get(tmp[0])==null){
					collect.add(new StringBuffer().append(tmp[0]).append("--").append(tmp[2]).append("--").append(tmp[1].trim()).toString());
				}
				if(lastMark==null || !lastMark.equals(tmp[0])){
					lastMark = tmp[0];
					idx++;
				}
			}
		}
	}
	
	/**
	 * 0:mark 1:sql 2:connect
	 * @param linStr
	 * @return
	 */
	public String[] getLogSql(String linStr){
		String[] t = new String[3];
		String[] tmp = linStr.split(" ");
		StringBuffer sql = new StringBuffer();
		for(String s:tmp){
			if(target.matcher(s).find() && t[0]==null){
				t[0] = s.substring(1, s.length()-1);
			}else if(target.matcher(s).find() && t[2]==null){
				t[2] = s.substring(1, s.length()-1);
			}else if(t[0]!=null){
				sql.append(s).append(" ");
			}
		}
		t[1] = sql.toString();
		return t;
	}

	@Override
	public boolean isBreak(String lineStr) {
		return mSize ==  idx;
	}

	public int getIdx(){
		return this.idx;
	}

}
