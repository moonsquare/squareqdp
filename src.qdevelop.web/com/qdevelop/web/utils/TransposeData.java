package com.qdevelop.web.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransposeData {
	/**
	 * 转置核心方法 放到util中
	 * @param fieldShow
	 * @param data
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public  List toTransposeData(Map<String,String> fieldShow,List data){
		List tmpResult = new ArrayList(fieldShow.keySet().size());
		int dataSize = data.size()+1;
		for(String key : fieldShow.keySet()){
			Map dd = new HashMap(dataSize);
			dd.put("COLUMN0", fieldShow.get(key));
			for(int j=1;j<dataSize;j++){
				dd.put("COLUMN"+j, ((Map)data.get(j-1)).get(key));
			}
			tmpResult.add(dd);
		}
		return tmpResult;
	}
}
