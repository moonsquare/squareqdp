package com.qdevelop.web.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.UtilsFactory;

public class FileUploadAction extends QDevelopAction{
	private static final long serialVersionUID = 572146812454l ;

	private static final int BUFFER_SIZE = 1024*1024*16 ;//1M

	private File myFile;
	private String allowType,savePath,msg,myFileFileName,isRandomName;
	private int maxSize;


	public File getMyFile() {
		return myFile;
	}

	public void setMyFile(File myFile) {
		this.myFile = myFile;
	}

	public String getAllowType() {
		return allowType;
	}

	public void setAllowType(String allowType) {
		this.allowType = allowType.toLowerCase();
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getMyFileFileName() {
		return myFileFileName;
	}

	public void setMyFileFileName(String myFileFileName) {
		this.myFileFileName = myFileFileName;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	protected  void copy(File src, File dst)  {
		try  {
			InputStream in = null ;
			OutputStream out = null ;
			try  {                
				in = new BufferedInputStream( new FileInputStream(src));
				out = new BufferedOutputStream( new FileOutputStream(dst));
				byte[] b = new byte[BUFFER_SIZE];
				int n = -1;
				while ((n = in.read(b)) != -1) {
					out.write(b, 0, n);
				}
			} finally  {
				if ( null != in)  {
					in.close();
				} 
				if ( null != out)  {
					out.close();
				} 
			} 
		} catch (Exception e)  {
			e.printStackTrace();
		} 
	} 

	public String getIsRandomName() {
		return isRandomName;
	}

	public void setIsRandomName(String isRandomName) {
		this.isRandomName = isRandomName;
	}

	@Override
	public String execute()	throws Exception{    
		msg = getUploadPath();
		return SUCCESS;
	} 
	
	protected String getUploadPath(){
		String _fileName = getValidatorName();
		String sPath =UtilsFactory.getProjectPath("/",getSavePath());
		File _fPath = new File(sPath);
		if(!_fPath.exists())_fPath.mkdirs();
		if(isRandomName!=null&&isRandomName.equals("true"))
			_fileName = QString.append(QDate.getNow("yyyymmdd"),myFileFileName.substring(myFileFileName.indexOf(".")));
		File _saveFile = new File(sPath+"/"+_fileName);
		copy(myFile, _saveFile);
		return _saveFile.getAbsolutePath();
	}
	
	protected String getValidatorName(){
		if(myFile==null) throw new QDevelopException("文件上传错误！");
		String _fileName = this.getMyFileFileName();
		String _fileType = _fileName.substring(_fileName.indexOf(".")).toLowerCase();
		if(this.getAllowType().indexOf(_fileType)==-1) throw new QDevelopException("上传文件类型不对！只允许上传类型为["+this.getAllowType()+"]的文件！");
		if(getMaxSize()>0&&(myFile.length()/1024)>getMaxSize()) throw new QDevelopException("上传文件数据过大！只允许上传文件小于["+this.getMaxSize()+"KB]的文件！");
		return _fileName;
	}
	
	protected InputStream getUploadStream() throws FileNotFoundException{
		getValidatorName();
		return new FileInputStream(myFile);
	}
	

}
