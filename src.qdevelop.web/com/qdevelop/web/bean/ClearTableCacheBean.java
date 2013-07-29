package com.qdevelop.web.bean;

import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;

public class ClearTableCacheBean implements ResultBean{
	public ClearTableCacheBean clone(){
		try {
			return (ClearTableCacheBean)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public List<Map<String, Object>> getResultList() {
		return null;
	}

	@Override
	public Map<String, Object> getResultMap(Object i) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public void setResultList(List<Map<String, Object>> result) {
		
	}

	@Override
	public void addResult(Map<String, Object> data) throws Exception {
		
	}

	@Override
	public void flush() {
		
	}

	@Override
	public void clear() {
		
	}
	public String toString(){
		return "";
	}

}
