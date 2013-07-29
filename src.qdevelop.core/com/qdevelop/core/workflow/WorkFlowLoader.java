package com.qdevelop.core.workflow;

import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.utils.files.IQFileLoader;
import com.qdevelop.utils.files.QXMLUtils;

public class WorkFlowLoader extends HashMap<String,WorkFlowBean> implements IQFileLoader{
	/**
	 * 
	 */
	private static final long serialVersionUID = -416686971228909621L;
	private static WorkFlowLoader _WorkFlowLoader = new WorkFlowLoader();
	public static WorkFlowLoader getInstance(){
		return _WorkFlowLoader;
	}
	private boolean isFirst = true;
	public WorkFlowLoader(){
		if(isFirst)this.reload();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void reload() {
		QXMLUtils xmlUtils = new QXMLUtils();
		try {
			Element config =  xmlUtils.getDocument(UtilsFactory.source().getResourceAsFile("workflowConfig.xml")).getRootElement();
			Iterator<Element> iter = config.elementIterator("workflow");
			while(iter.hasNext()){
				Element workflow = iter.next();
				this.put(workflow.attributeValue("index"), new WorkFlowBean(workflow));
			}
			isFirst = false;
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void clear() {
		super.clear();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
}
