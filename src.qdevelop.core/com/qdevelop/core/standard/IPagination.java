package com.qdevelop.core.standard;

public interface IPagination {
	
	public void setNowPage(int page);
	
	public int getNowPage();
	
	public void setMaxNum(int maxNum);
	
	public int getMaxNum();
	
	public void setAllCount(int allCount);
	
	public int getAllCount();
	
	public void initPagination(int nowPage,int maxNum,int allCount);

}
