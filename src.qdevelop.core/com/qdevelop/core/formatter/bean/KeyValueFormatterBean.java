package com.qdevelop.core.formatter.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.standard.IPagination;

public class KeyValueFormatterBean extends HashMap<String,String> implements ResultBean,Serializable,IPagination,Cloneable{
	
	public KeyValueFormatterBean clone(){
		return (KeyValueFormatterBean) super.clone();
	}
	private static final long serialVersionUID = 9211752181678734882L;
	String keyName,valueName;
	int allCount=0,nowPage=0,allPage=0;
	
	public KeyValueFormatterBean(String keyName,String valueName){
		this.keyName = keyName;
		this.valueName = valueName;
	}
	
	@Override
	public void addResult(Map<String, Object> data) throws Exception {
		this.put(String.valueOf(data.get(keyName)), String.valueOf(data.get(valueName)));
	}

	@Override
	public void clear() {
		
	}

	@Override
	public void flush() {
		
	}

	@Override
	public int getAllCount() {
		return 0;
	}

	@Override
	public List<Map<String,Object>> getResultList() {
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
	public int getMaxNum() {
		return 0;
	}

	@Override
	public int getNowPage() {
		return 0;
	}

	@Override
	public void initPagination(int nowPage, int maxNum, int allCount) {
		this.allCount = allCount;
		this.allPage = allCount%maxNum>0?((int)(allCount/maxNum)+1):allCount/maxNum;
		this.nowPage = nowPage;
	}
	
	public boolean hasOtherData(){
		if(this.allPage==0 || this.nowPage == 0 || this.nowPage<this.allPage)return true;
		else return false;
	}
	
	public int nextPage(){
		return ++this.nowPage;
	}

	@Override
	public void setMaxNum(int maxNum) {
		
	}

	@Override
	public void setNowPage(int page) {
		
	}

	@Override
	public void setResultList(List<Map<String, Object>> result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAllCount(int allCount) {
		
	}

}
