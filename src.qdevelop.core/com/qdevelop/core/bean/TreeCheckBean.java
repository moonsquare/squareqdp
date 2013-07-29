package com.qdevelop.core.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeCheckBean extends HashMap<String,String>{
	private static final long serialVersionUID = 5556213898443358073L;
	
	String[] checkValues;
	public TreeCheckBean(Object checkedValue){
		if(checkedValue!=null){
			if(checkedValue instanceof String){
				checkValues = String.valueOf(checkedValue).split("\\|");
			}else if(checkedValue instanceof String[]){
				checkValues = (String[])checkedValue;
			}
		}
		
	}
	
	public String[] getCheckValues(){
		return checkValues;
	} 
	
	public void init(Map<String,Object> data,String targetKey,String targetParentKey){
		if(data!=null){
			this.put(String.valueOf(data.get(targetKey)), String.valueOf(data.get(targetParentKey)));
		}
	}
	
	public boolean isChecked(String id){
		return this.get(id)==null?false:true;
	}
	
	public String getStated(List<String> childs,String _id){
		for(String id:childs){
			if(_id.equals(this.get(id))){
				return "open";
			}
		}
		return "closed";
	}
}
