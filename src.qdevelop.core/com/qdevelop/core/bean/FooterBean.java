package com.qdevelop.core.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ognl.Ognl;

import org.dom4j.Element;

import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;

public class FooterBean {
	
	String footerSql;int allCount;
	Element footerParser;
	
	public FooterBean(Element footerParser){
		footerSql = footerParser.elementText("footer-sql").replaceAll("\n|\t", " ").trim().replaceAll(" +", " ").replace("$[_autoSearch]", QDevelopConstant.AUTO_SEARCH_MARK);
		this.footerParser = footerParser;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList formatterFooter(Map<String, Object> data) throws QDevelopException {
		ArrayList footerResult = new ArrayList();
		try {
			data.put("ALL", allCount==0?1:allCount);
			Iterator<Element>  rowsItor = footerParser.elements("rows").iterator();
			Iterator<Element>  columnItor;
			Element column;String key;
			while(rowsItor.hasNext()){
				Map footerData = new HashMap();
				columnItor = rowsItor.next().elementIterator("column");
				while(columnItor.hasNext()){
					column = columnItor.next();
					key = column.attributeValue("field").toUpperCase();
					if(column.attributeValue("value").indexOf("$")==-1){
						footerData.put(key, column.attributeValue("value"));
					}else{
						footerData.put(key, new StringBuffer().append(
								Ognl.getValue(Ognl.parseExpression(column.attributeValue("value").toUpperCase().replaceAll("\\$|\\{|\\}", "")),data))
								.append(column.attributeValue("suffix")==null?"":column.attributeValue("suffix")).toString());
					}
				}
				footerResult.add(footerData);
			}
		} catch (Exception e) {
			throw new QDevelopException(e);
		}
		return  footerResult;
	}
	
	public String getFooterKey(String key){
		return key.replaceAll("\\(.+\\)", "(footer)");
	}
	
	public String getFooterSQL(){
		return footerSql;
	}
	
	public void setAll(int allCount){
		this.allCount = allCount;
	}

	

}
