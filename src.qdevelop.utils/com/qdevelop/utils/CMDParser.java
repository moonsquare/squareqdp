package com.qdevelop.utils;

import java.util.HashMap;
import java.util.Map;

public class CMDParser {
	
	/**
	 * 请求String 转 Map 请求<br>
	 * 例： "-index ceshi -version 1.0"
	 * @param args
	 */
	public CMDParser(String[] args){
		if(args==null || args.length==0)return;
		for(int i=0;i<args.length;i=i+2){
			cmds.put(args[i].replace("-", ""), args[i+1]);
		}
	}
	
	/**
	 * 请求String 转 Map 请求<br>
	 * 例： "index=cheshi&version=1.0"
	 * @param args
	 */
	public CMDParser(String args){
		if(args==null || args.length()==0)return;
		String [] tmp = args.split("&");
		for(String t : tmp){
			cmds.put(t.substring(0,t.indexOf("=")),t.substring(t.indexOf("=")+1));
		}
	}
	
	/**
	 * 请求String 转 Map 请求<br>
	 * 例：" index:ceshi|version:1.0","\\|"  
	 * @param args
	 * @param split
	 */
	public CMDParser(String args,String split){
		if(args==null || args.length()==0)return;
		String[] tmp = args.split(split);
		for(String t : tmp){
			cmds.put(t.substring(0,t.indexOf(":")),t.substring(t.indexOf(":")+1));
		}
	}
	
	
	private  Map<String,String> cmds = new HashMap<String,String>();
	
	/**
	 * 获取某一个参数的值
	 * @param param
	 * @return
	 */
	public String getParamer(String param){
		return cmds.get(param);
	}
	
	/**
	 * 获取所参数的值
	 * @return
	 */
	public Map<String,String> getParamers(){
		return cmds;
	}
	
	public int getInt(String param){
		return Integer.parseInt(this.getParamer(param));
	}
	
	public double getDouble(String param){
		return Double.parseDouble(this.getParamer(param));
	}
	
	public boolean getBoolean(String param){
		return Boolean.parseBoolean(this.getParamer(param));
	}
	
	
}
