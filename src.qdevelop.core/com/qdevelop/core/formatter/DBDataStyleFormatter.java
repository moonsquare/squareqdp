package com.qdevelop.core.formatter;

import java.util.Map;

import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;

public class DBDataStyleFormatter  extends AbstractFormatter{
	private String style,targetKey;
	private String[] params;
	@Override
	public void init(InitFormatBean param) {
		this.style = param.getConfig("style");
		this.targetKey = param.getConfig("targetKey");
		
		if(this.style.indexOf("{")>-1&&this.style.indexOf("}")>-1)
			params = this.style.substring(this.style.indexOf("{")+1,this.style.lastIndexOf("}")).split("\\}\\{|\\}.+\\{");
	}
	public DBDataStyleFormatter(){
		
	}

	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts) throws QDevelopException {
		if(params==null)data.put(targetKey,style) ;
		else{
			String tmp = new String(style);
			for(String p:params){
				tmp = tmp.replace(getTarget(p), String.valueOf(data.get(p.toUpperCase())));
			}
			data.put(targetKey,tmp);
		}
	}
	
	private String getTarget(String reg){
		return new StringBuffer().append("{").append(reg).append("}").toString();
	}

	public boolean isQBQuery(){
		return false;
	}	

}
