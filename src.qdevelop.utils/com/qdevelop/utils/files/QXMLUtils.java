package com.qdevelop.utils.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.qdevelop.utils.QString;


public class QXMLUtils {
	
	public QXMLUtils(){	
	}
	
	
	/**
	 * 获取XMLDocument
	 * @param filePath
	 * @return
	 * @throws DocumentException
	 */
	public Document getDocument(String filePath) throws DocumentException{
		return getDocument(new File(filePath));
	}
	
	public Document getDocument(String filePath,String encode) throws DocumentException{
		return getDocument(new File(filePath),encode);
	}
	
	public Document getDocument(File xmlFile) throws DocumentException{
		return getDocument(xmlFile,"GBK");
	}
	
	public Document getDocument(File xmlFile,String encode) throws DocumentException{
		if(!xmlFile.exists()) return null;
		SAXReader saxReader = new SAXReader();
//		saxReader.setEncoding(encode);
		return saxReader.read(xmlFile);
	}
	
	public Document getDocument(InputStream xmlFile) throws DocumentException{
		return getDocument(xmlFile,"GBK");
	}
	
	public Document getDocument(InputStream xmlFile,String encode) throws DocumentException{
		SAXReader saxReader = new SAXReader();
//		saxReader.setEncoding(encode);
		return saxReader.read(xmlFile);
	}
	
	@SuppressWarnings("unchecked")
	public void copyAdd(Element targetParent,Element src){
		Element copyElem = targetParent.addElement(src.getName());
		List<Attribute> attributes = src.attributes();
		for(Attribute attr:attributes){
			copyElem.addAttribute(attr.getName(), attr.getValue());
		}
		if(src.getTextTrim().length()>0){
			copyElem.addText(src.getText());
		}
		Iterator<Element> iter = src.elementIterator();
		while(iter.hasNext()){
			copyAdd(copyElem,iter.next());
		}
	}
	
	/**
	 * 保存XML
	 * @param document
	 * @param filePath
	 * @throws IOException
	 */
	public void save(Document document,String filePath) throws IOException{
		save(document,new File(filePath),"GBK");
	}
	public void save(Document document,String filePath,String encode) throws IOException{
		save(document,new File(filePath),encode);
	}
	public void save(Document document,File file) throws IOException{
		save(document,file,"GBK");
	}
	public void save(Document document,File file,String encode) throws IOException{
		OutputFormat outFmt = OutputFormat.createPrettyPrint(); 
	    outFmt.setEncoding(encode); 
	    outFmt.setExpandEmptyElements(true);
	    outFmt.setTrimText(true);
	    outFmt.setIndent(true);
	    outFmt.getNewLineAfterNTags();
		XMLWriter writer = new XMLWriter(new FileOutputStream(file),outFmt);    
		writer.write(document);            
		writer.close();
		System.out.println(QString.append("SAVE XML:\t",file.getAbsolutePath()));
	}
	
	public File[] getXMLFiles(String path){
		File _f = new File(path);
		if(!_f.exists())return null;
		return _f.listFiles(new FileFilter(".xml"));
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
