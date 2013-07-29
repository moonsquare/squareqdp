package com.qdevelop.core.formatter;

import java.text.SimpleDateFormat;
import java.util.Map;

import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;

public class DBDateValueFormatter  extends AbstractFormatter{
	String[] dateKey,formatterStyle;boolean isFormatter;
	
	ThreadLocal<SimpleDateFormat>[] simpleDateFormats;
	@SuppressWarnings("unchecked")
	@Override
	public void init(InitFormatBean param) {
		this.dateKey = param.getConfig("targetKey", ",",true);
		this.formatterStyle = param.getConfig("style", ",");
		simpleDateFormats = new ThreadLocal[this.dateKey.length];
		for(int i=0;i<this.dateKey.length;i++){
			simpleDateFormats[i] = new ThreadLocal<SimpleDateFormat>();
//			simpleDateFormats[i].set(new SimpleDateFormat((i>formatterStyle.length-1)?formatterStyle[0]:formatterStyle[i]));
		}
	}
	public DBDateValueFormatter(){}
	
	@Override
	public void initFormatter(DBStrutsBean struts) {
		isFormatter = dateKey!=null && formatterStyle!=null;
	}
	
	
	public SimpleDateFormat getSimpleDateFormat(int i){
		SimpleDateFormat sdf = simpleDateFormats[i].get();  
        if (sdf == null) {  
            sdf = new SimpleDateFormat((i>formatterStyle.length-1)?formatterStyle[0]:formatterStyle[i]);  
            simpleDateFormats[i].set(sdf);  
        }  
        return sdf;
	}

	@Override
	public void formatter(Map<String, Object> data,DBStrutsBean struts) throws QDevelopException {
		if(isFormatter){
			for(int i=0;i<dateKey.length;i++){
				if(data.get(dateKey[i])!=null){
					if(data.get(dateKey[i]) instanceof java.sql.Timestamp){
						data.put(dateKey[i], getSimpleDateFormat(i).format((java.sql.Timestamp)data.get(dateKey[i])));
					}else if(data.get(dateKey[i]) instanceof java.sql.Date){
						data.put(dateKey[i], getSimpleDateFormat(i).format((java.sql.Date)data.get(dateKey[i])));
					}
				}
			}
		}
	}

	public boolean isQBQuery(){
		return false;
	}


}
