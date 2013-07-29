package com.qdevelop.web.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.utils.cache.MapCache;

public class DownResultBean extends ArrayList<Map<String, Object>> implements ResultBean,Cloneable{
	public DownResultBean clone(){
		return (DownResultBean) super.clone();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -997909739138468084L;
	public static final int CSV = 1;
	public static final int TXT = 2;
	public static File _downLoadRoot;

	public DownResultBean(String[] keys,String[] tittles,int type){
		if(tittles!=null){
			for(int i=0;i<tittles.length;i++){
				if(i>0)result.append(",");
				result.append(tittles[i]);
			}
			result.append("\r\n");
		}
		this.keys = keys;
		//		this.type = type;
	}

	//	private int type;
	String[] keys ;
	StringBuffer result = new StringBuffer(); 
	long idx = 0 ;
	private FileWriter  fw = null;
	private File fileName;
	public String _file;

	@Override
	public void addResult(Map<String, Object> data) throws Exception {
		this.add(data);
	}

	private Object getValue(String key,Map<String,Object> data){
		if(data.get(key)==null)return null;
		Object val = data.get(MapCache.getInstance().getFormatterKey(key));
		return val == null?data.get(key):val;
	}
	private String parseVal(Object val){
		if(val==null)return "";
		return new StringBuffer().append("\"").append(String.valueOf(val).indexOf("\"")>-1?String.valueOf(val).replaceAll("\"", "\"\""):val).append("\"").toString();
		//		switch(type){
		//		case TXT : 
		//			return String.valueOf(val);
		//		case CSV :
		//			return new StringBuffer().append("\"").append(String.valueOf(val).indexOf("\"")>-1?String.valueOf(val).replaceAll("\"", "\"\""):val).append("\"").toString();
		//		default:
		//			return String.valueOf(val);
		//		}
	}

	@Override
	public void clear() {
		super.clear();
	}

	public void flush(){

	}
	public void toFile() {
		try {
			if(fw == null){
				if(_downLoadRoot==null){
					_downLoadRoot = new File(UtilsFactory.getProjectPath("/download_temp"));
					if(!_downLoadRoot.exists())_downLoadRoot.mkdirs();
				}
				fileName = new File(_downLoadRoot,QString.append(System.currentTimeMillis(),".tmp"));
				fw=new FileWriter(fileName);
				fw.write(result.toString());
				result  = null;
			}
			for(int idx=0;idx<super.size();idx++){
				Map<String,Object> data = super.get(idx);
				for(int i=0;i<keys.length;i++){
					if(i>0)fw.write(",");
					fw.write(parseVal(getValue(keys[i],data)));
				}
				fw.write("\r\n");
			}
			super.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(fw!=null)
					fw.close();
				fw  = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<Map<String,Object>> getResultList() {
		return null;
	}

	@Override
	public Map<String, Object> getResultMap(Object i) {
		return this.get((Integer)i);
	}

	@Override
	public int size() {
		return super.size();
	}

	//	public InputStream toInputStream() throws Exception{
	//		if(idx<10000 && result!=null)
	//			return new ByteArrayInputStream(result.toString().getBytes());
	//		if(fileName!=null){
	//			return new FileInputStream(fileName);
	//		}
	//		return null;
	//	}




	public File getDownFile(){
		return fileName;
	}

	public File getDownFile(String encode) throws Exception{
		File tmp = new File(fileName.getAbsolutePath()+"."+encode);
		OutputStreamWriter fw=null;
		InputStreamReader read = null;
		try {
			fw=new OutputStreamWriter(new  FileOutputStream(tmp),encode);
			read = new InputStreamReader (new FileInputStream(fileName));   
			BufferedReader reader=new BufferedReader(read);   
			String line;   
			while ((line = reader.readLine()) != null) {   
				fw.write(line);  
				fw.write("\r\n");
			}
			fw.flush();
		} catch (Exception e) {
			throw e;
		}finally{
			if(read!=null)
				read.close();
			if(fw!=null)fw.close();
		}
		return tmp;
	}

	@Override
	public void setResultList(List<Map<String, Object>> result) {
		// TODO Auto-generated method stub

	}

	//	public static void main(String[] args) {
	//		System.out.println("aa\"bb\"ccc".replaceAll("\"", "\"\""));
	//	}
}
