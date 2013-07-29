package com.qdevelop.web.bean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.UtilsFactory;

public class SQLResultBean implements ResultBean,Cloneable{
	public SQLResultBean clone(){
		try {
			return (SQLResultBean) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}return null;
	}
	/**
	 *	insert into {TABLENAME}(cName1,cName2) values({cName1},'{cName2}');
	 **/
	String formatter;
	String[] keys ;
	String dataFileName;
	boolean isDirectToFile;
	public SQLResultBean(String formatter,String sql){
		this.formatter = formatter;
		keys = formatter.replaceAll("\\}.+?\\{", "#").replaceAll("^.+?\\{|\\}.+?$", "").toUpperCase().split("#");
	}


	StringBuffer result = new StringBuffer(); 
	long idx = 0 ;
	private FileWriter  fw = null;
	private File fileName;

	@Override
	public void addResult(Map<String, Object> data) throws Exception {
		if(!isDirectToFile && idx < 10000){
			String tmp = new String(formatter);
			for(int i=0;i<keys.length;i++){
				tmp = tmp.replace(new StringBuffer().append("{").append(keys[i]).append("}").toString()
						, parseVal(data.get(keys[i])));
			}
			result.append(tmp).append("\r\n");
			idx++;
		}else{
			if(fw == null){
				File path = new File(UtilsFactory.getProjectPath("/download_temp"));
				if(path!=null)path.mkdirs();
				fileName = new File(path,(dataFileName==null?QString.append(System.currentTimeMillis(),".tmp"):dataFileName));
				fw=new FileWriter(fileName);
				fw.write(result.toString());
				result  = null;
			}
			String tmp = new String(formatter);
			for(int i=0;i<keys.length;i++){
				tmp = tmp.replace(new StringBuffer().append("{").append(keys[i]).append("}").toString()
						, parseVal(data.get(keys[i])));
			}
			fw.write(tmp);
			fw.write("\r\n");
		}
		data.clear();
		data = null;
	}

	private String parseVal(Object val){
		return String.valueOf(val);
	}

	@Override
	public void clear() {

	}

	@Override
	public void flush() {
		try {
			if(fw!=null)fw.close();
			fw  = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public InputStream toInputStream() throws FileNotFoundException{
		if(idx<10000 && result!=null)
			return new ByteArrayInputStream(result.toString().getBytes());
		if(fileName!=null){
			return new FileInputStream(fileName);
		}
		return null;
	}


	public String toString(){
		return result.toString();
	}

	@Override
	public void setResultList(List<Map<String, Object>> result) {
		// TODO Auto-generated method stub

	}


}
