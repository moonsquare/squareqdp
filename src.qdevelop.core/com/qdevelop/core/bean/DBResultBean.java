package com.qdevelop.core.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.core.standard.IPagination;

/**
 * 数据请求查询结果接口
 * @author Janson.Gu
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DBResultBean extends ArrayList<Map> implements Serializable,ResultBean,IPagination,Cloneable{
	
	public DBResultBean clone(){
		return (DBResultBean) super.clone();
	}
	
	private static final long serialVersionUID = 9153701182813448036L;

	/**总的查询结果条数	OR 数据更新操作的执行条数**/
	public int allCount ; 

	/**上一页的页数**/
	public int previousPage ; 

	/**下一页的页数**/
	public int nextPage ; 

	/**当前页数 最小为1**/
	public int nowPage ; //

	/**最后一页的页数**/
	public int lastPage ; //

	/**第一页**/
	public int firstPage ;//

	/**每页显示最大条数**/
	public int maxNum ; //

	/**查询结果列表**/
	public List<Map> footer ; //

	public int getAllCount() {
		return allCount;
	}
	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}
	public int getFirstPage() {
		return firstPage;
	}
	public void setFirstPage(int firstPage) {
		this.firstPage = firstPage;
	}
	public int getLastPage() {
		return lastPage;
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}

	public int getMaxNum() {
		return maxNum;
	}
	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}
	public int getNextPage() {
		return nextPage;
	}
	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}
	public int getNowPage() {
		return nowPage;
	}
	public void setNowPage(int nowPage) {
		this.nowPage = nowPage;
	}
	public int getPreviousPage() {
		return previousPage;
	}
	public void setPreviousPage(int previousPage) {
		this.previousPage = previousPage;
	}
	public List getResultList() {
		return (List)this;
	}
	public void setResultList(List<Map<String,Object>> resultList) {
		for(Map<String,Object> obj:resultList){
			this.add(obj);
		}
	}
	

	public void addResult(Map<String,Object> result){
		this.add(result);
	}


	
	public Map getResultMap(Object i){
		int idx = (Integer)i;
		if(idx>=this.size())return null;
		return (Map)this.get(idx);
	}

	/**
	 *	判定结果集是否有值 
	 * @return
	 */
	public boolean hasValue(){
		return this.size()>0?true:false;
	}

	public void setFooter(List footer) {
		this.footer = footer;
	}
	/**
	 * 初始化分页页码数据
	 * @param page
	 * @param maxNum
	 */
	public void initPagination(int page,int maxNum,int allCount){
		this.setAllCount(allCount);
		this.setNowPage(page);
		this.setMaxNum(maxNum);
		this.setFirstPage(1);
		this.setLastPage(this.getAllCount()%maxNum==0?this.getAllCount()/maxNum:1+this.getAllCount()/maxNum);
		this.setPreviousPage(page>2?page-1:1);
		this.setNextPage(page+1<this.getLastPage()?page+1:this.getLastPage());
	}

	public void clear(){
		if(footer!=null){
			footer.clear();
			footer = null;
		}
		super.clear();
	}
	
	/**
	 * 打印结果信息
	 * @return
	 */
	public String printInfo(){
		StringBuffer sb = new StringBuffer();
		sb.append("{").append("resultList:[\r\n");
		int size = this.size()-1;
		for(int i=0;i<=size;i++){
			if(i<size){
				sb.append(obj2Str(this.get(i))).append(",");
			}else{
				sb.append(obj2Str(this.get(i)));
			}
		}
		if(this.footer!=null){
			sb.append("],footer:[\r\n");
			for(int i=0;i<this.footer.size();i++){
				if(i>0)sb.append(",");
				sb.append(obj2Str(this.footer.get(i)));
			}
		}
		sb.append("],'allCount':'").append(this.getAllCount()).append("'");             
		sb.append(",'previousPage':'").append(this.getPreviousPage()).append("'");     
		sb.append(",'nextPage':'").append(this.getNextPage()).append("'");          
		sb.append(",'nowPage':'").append(this.getNowPage()).append("'");            
		sb.append(",'lastPage':'").append(this.getLastPage()).append("'");             
		sb.append(",'firstPage':'").append(this.getFirstPage()).append("'");           
		sb.append(",'maxNum':'").append(this.getMaxNum()).append("'");        
		sb.append("}");
		return sb.toString();
	}

	public String getPaginationInfo(){
		StringBuffer sb = new StringBuffer();
		sb.append("{'allCount':'").append(this.getAllCount()).append("'");             
		sb.append(",'previousPage':'").append(this.getPreviousPage()).append("'");     
		sb.append(",'nextPage':'").append(this.getNextPage()).append("'");          
		sb.append(",'nowPage':'").append(this.getNowPage()).append("'");            
		sb.append(",'lastPage':'").append(this.getLastPage()).append("'");             
		sb.append(",'firstPage':'").append(this.getFirstPage()).append("'");           
		sb.append(",'maxNum':'").append(this.getMaxNum()).append("'");        
		sb.append("}");
		return sb.toString();
	}

	public List getFooter(){
		return this.footer;
	}

	public void flush(){
		if(this.allCount==0)this.allCount = this.size();
	}


	private String obj2Str(Object obj){
		if(obj == null){
			return "{}";
		}
		if(obj.getClass().getName().indexOf("Object")>-1){
			Object[] tmp = (Object[])obj;
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			for(int i=0;i<tmp.length;i++){
				sb.append(String.valueOf(tmp[i])).append(",");
			}
			sb.append("]");
			return sb.toString().replace(",]", "]");
		}else{
			String str = obj.toString();
			return str.replaceAll(" ", "").replaceAll("=", "':'").replaceAll(",", "','").replace("{", "{'").replace("}", "'}")+"\r\n";

		}
	}
}
