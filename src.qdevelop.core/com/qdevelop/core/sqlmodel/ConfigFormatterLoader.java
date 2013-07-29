package com.qdevelop.core.sqlmodel;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;

import com.qdevelop.core.formatter.bean.FormatterConfigBean;
import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.utils.files.IQFileLoader;
import com.qdevelop.utils.files.QXMLUtils;

public class ConfigFormatterLoader extends HashMap<String,FormatterConfigBean>  implements IQFileLoader{
	/**
	 * 
	 */
	private HashMap<String,FormatterConfigBean> paramFormatter = new HashMap<String,FormatterConfigBean>();

	private static final long serialVersionUID = -3245392917210984209L;
	private static ConfigFormatterLoader _ConfigFormatterLoader;

	public static ConfigFormatterLoader getInstance(){
		if(_ConfigFormatterLoader==null){
			_ConfigFormatterLoader  = new ConfigFormatterLoader();
			_ConfigFormatterLoader.reload();
		}
		return _ConfigFormatterLoader;
	}




	@SuppressWarnings("unchecked")
	@Override
	public void reload() {
		try {
			File formatter = UtilsFactory.source().getResourceAsFile("data-formatter.xml");
			Element root = new QXMLUtils().getDocument(formatter).getRootElement();
			Element formatter_config;
			Iterator<Element> iter = root.elementIterator("formatter");
			while(iter.hasNext()){
				formatter_config = iter.next();
				FormatterConfigBean fcb = new FormatterConfigBean();
				fcb.setFormatterClass(formatter_config.elementText("class"));
				if(formatter_config.elementText("attribute-name")!=null)
					fcb.setParams(formatter_config.elementText("attribute-name").split("\\|"));
				this.put(formatter_config.attributeValue("name"), fcb);
			}

			iter = root.elementIterator("before-run");
			while(iter.hasNext()){
				formatter_config = iter.next();
				FormatterConfigBean fcb = new FormatterConfigBean();
				fcb.setFormatterClass(formatter_config.elementText("class"));
				if(formatter_config.elementText("attribute-name")!=null)
					fcb.setParams(formatter_config.elementText("attribute-name").split("\\|"));
				paramFormatter.put(formatter_config.attributeValue("name"), fcb);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取结果格式化类
	 * @param formatterConfig
	 * @return
	 */
	public FormatterConfigBean getFormatterConfigBean(String formatterConfig){
		return this.get(formatterConfig);
	}

	/**
	 * 获取参数格式化类
	 * @param formatterConfig
	 * @return
	 */
	public FormatterConfigBean getParamFormatterBean(String formatterConfig){
		return paramFormatter.get(formatterConfig);
	}

	@Override
	public void clear() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConfigFormatterLoader.getInstance().getFormatterConfigBean("style-formatter");
	}
}
