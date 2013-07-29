package com.qdevelop.core.workflow;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;

public class WorkFlowBean extends ArrayList<FlowLeaf>{
	public static final int END_MARKER = 0;
	private static final long serialVersionUID = 7991337647082787216L;
	private int size;
	
	public int getNext(int wfIndex,boolean isAccess){
		if(wfIndex > size)return 0;
		return isAccess?this.get(wfIndex-1).getAgreeTo():this.get(wfIndex-1).getRefuseTo();
	}
	
	public String[] getExcuteSqlIndexs(int wfIndex,boolean isAccess){
		if(wfIndex > size)return null;
		return isAccess?this.get(wfIndex-1).agreeSqlIndexs:this.get(wfIndex-1).refuseSqlIndexs;
	}
	private String index,explain;
	@SuppressWarnings("unchecked")
	public WorkFlowBean(Element workflow){
		index = workflow.attributeValue("index");
		explain = workflow.attributeValue("explain");
		Iterator<Element> iter = workflow.elementIterator("flow");
		int order = 1;
		ArrayList<String> agreeSqls;
		ArrayList<String> refuseSqls;
		while(iter.hasNext()){
			Element flow = iter.next();
			FlowLeaf fl = new FlowLeaf(order++);
			fl.setRoles(flow.attributeValue("roles").split("\\|"));
			fl.setAgreeTo(flow.attributeValue("agreeTo"));
			fl.setRefuseTo(flow.attributeValue("refuseTo"));
			
			agreeSqls = refuseSqls = null;
			Iterator<Element> sql = flow.elementIterator("execute-sql-index");
			while(sql.hasNext()){
				Element e_sql = sql.next();
				if(e_sql.attributeValue("type").equals("agree")){
					if(agreeSqls == null)agreeSqls = new ArrayList<String>();
					agreeSqls.add(e_sql.attributeValue("index"));
				}else if(e_sql.attributeValue("type").equals("refuse")){
					if(refuseSqls == null)refuseSqls = new ArrayList<String>();
					refuseSqls.add(e_sql.attributeValue("index"));
				}
			}
			if(agreeSqls!=null)fl.setAgreeSqlIndexs(agreeSqls.toArray(new String[]{}));
			if(refuseSqls!=null)fl.setRefuseSqlIndexs(refuseSqls.toArray(new String[]{}));
			this.add(fl);
		}
		agreeSqls = refuseSqls = null;
		size = this.size();
		int lastIndex = size-1;
		if(this.get(lastIndex).getAgreeTo() == size+1)this.get(lastIndex).agreeTo = END_MARKER;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getExplain() {
		return explain;
	}
	public void setExplain(String explain) {
		this.explain = explain;
	}

}
class FlowLeaf{
	public FlowLeaf(int order){
		this.order = order;
	}
	int order;
	int agreeTo,refuseTo;
	String[] roles,agreeSqlIndexs,refuseSqlIndexs;
	
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String[] getRoles() {
		return roles;
	}
	public void setRoles(String[] roles) {
		this.roles = roles;
	}
	public String getParamKey(String sql){
		if(sql.indexOf("$[")==-1)return null;
		return sql.replaceAll("\\@\\[.+?\\]", "").replaceAll("\\].+?\\$\\[", "|").replaceAll("^.+?\\$\\[|\\].+?$|\\]", "");
	}
	public int getAgreeTo() {
		return agreeTo;
	}
	public void setAgreeTo(String agreeTo) {
		if(agreeTo!=null){
			if(agreeTo.equals("END"))
				this.agreeTo = WorkFlowBean.END_MARKER;
			else
				this.agreeTo = Integer.parseInt(agreeTo.trim());
		}else 
			this.agreeTo = new Integer(order+1);
	}
	public int getRefuseTo() {
		return refuseTo;
	}
	
	public String[] getAgreeSqlIndexs() {
		return agreeSqlIndexs;
	}
	public void setAgreeSqlIndexs(String[] agreeSqlIndexs) {
		this.agreeSqlIndexs = agreeSqlIndexs;
	}
	public String[] getRefuseSqlIndexs() {
		return refuseSqlIndexs;
	}
	public void setRefuseSqlIndexs(String[] refuseSqlIndexs) {
		this.refuseSqlIndexs = refuseSqlIndexs;
	}
	public void setRefuseTo(String refuseTo) {
		if(refuseTo!=null){
			if(refuseTo.equals("END"))
				this.refuseTo = WorkFlowBean.END_MARKER;
			else
				this.refuseTo = Integer.parseInt(refuseTo.trim());
		}else 
			this.refuseTo =new Integer(order-1);
	}
	
	public String toString(){
		return new StringBuffer().append("cur:").append(order).append("\tagreeTo:").append(agreeTo).append("\trefuseTo:").append(refuseTo).toString();
	}
}

