package com.qdevelop.core.formatter;

import java.util.Map;

public abstract class LoopQueryBaseformatter extends AbstractFormatter{
	private String loopIndex;
	private Map<String,String> marsterCondition;
	private String[] args;
	
	public void setMasterCondition(Map<String,String> marsterCondition){
		this.marsterCondition=marsterCondition;
	}
	public Map<String,String> getMasterCondition(){
		return this.marsterCondition;
	}
	
	public void joinQuery(Map<String,String> query){
		if(marsterCondition==null || args==null || args.length == 0)return ;
		for(String arg:args){
			query.put(arg,marsterCondition.get(arg));
		}
	}
	
	public void setArgs(String[] args){
		this.args = args;
	}
	
	public void setIndex(String index){
		this.loopIndex = index;
	}
	
	public String getIndex(){
		return this.loopIndex;
	}
}
