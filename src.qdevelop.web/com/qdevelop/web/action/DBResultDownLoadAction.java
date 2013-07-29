package com.qdevelop.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.DataBaseFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.core.utils.QueryBeanFormatter;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QProperties;
import com.qdevelop.web.bean.DownResultBean;

@SuppressWarnings({ "unchecked","rawtypes" })
public class DBResultDownLoadAction extends DownLoadAction {
	private static final long serialVersionUID = 3614637884008299603L;

	private InputStream resultStream;

	public InputStream getDownloadFile() {
		return resultStream;
	}
	
	public String execute() throws Exception{
		String fileName = this.getFileName();
		File result;
		if(fileName.indexOf(".gbk")>-1)
			result  =  parserDownLoadDownResultBean(this.getParamMap()).getDownFile("gbk");
		else result  =  parserDownLoadDownResultBean(this.getParamMap()).getDownFile();
		if(fileName.endsWith("zip"))
			resultStream = disposeFile(result);
		else resultStream = new FileInputStream(result);
		return SUCCESS;
	}
	
	public DownResultBean parserDownLoadDownResultBean(Map query) throws QDevelopException{
		String[] keys = null;
		String[] tittle = null;
		
		if(query.get("downloadKeys")!=null){
			keys = String.valueOf(query.get("downloadKeys")).toUpperCase().split("\\|");
			query.remove("downloadKeys");
		}
		if(query.get("downloadTittles")!=null){
			tittle = String.valueOf(query.get("downloadTittles")).split("\\|");
			query.remove("downloadTittles");
		}
		query.remove("lazyPagination");
		query.remove("fileName");
		int limitSize = QProperties.getInstance().getProperty("QDevelop_MAX_DOWN_NUMBER")==null?100:QProperties.getInstance().getInt("QDevelop_MAX_DOWN_NUMBER");
		Connection conn = null; 
		try{	
			DBQueryBean qb = CoreFactory.getInstance().getQueryBean(query,this.getUserInfo(),this.getSession());
			
			conn = ConnectFactory.getInstance(qb.getConnect()).getConnection();
			if(keys == null){
				DBResultBean rb = DataBaseFactory.getInstance().getQueryStruts(qb);
				keys = new String[rb.size()];
				tittle = new String[rb.size()];
				for(int i=0;i<rb.size();i++){
					keys[i]		=	String.valueOf(rb.getResultMap(i).get("NAME"));
					tittle[i]	=	keys[i];
				}
			}
			DownResultBean downResultBean = new DownResultBean(keys,tittle,getFileType());
			IResultFormatter[] _formatter =  SQLModelLoader.getInstance().getFormatterBeanByIndex(qb.getSqlIndex());
			String downsql = qb.getSQLByOrder();
			if(downsql.toLowerCase().indexOf(" limit ")==-1)downsql += " limit "+limitSize; 
			QueryFromDataBaseImp.getInstance().select(downsql, conn, downResultBean, _formatter);
			if(qb.getAfterRun()!=null){
				new QueryBeanFormatter().reflectRun(qb.getAfterRun(), "disposeResultBean", new Object[]{downResultBean,qb});
			}
			downResultBean.toFile();
			downResultBean.clear();
			return downResultBean;
		}catch(QDevelopException e){
			throw e;
		}finally{
			try {
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int getFileType(){
		if(this.getFileName() == null) return 0;
		String fName = this.getFileName().toLowerCase();
		if(fName.endsWith("csv")){
			return DownResultBean.CSV;
		}else if(fName.endsWith("txt")){
			return DownResultBean.TXT;
		}
		return 0;
	}
	
	private FileInputStream disposeFile(File f) throws FileNotFoundException {
		if(f.length()==0 || !f.exists())return null;
		File outFile = new File(f.getParent(),this.getFileName());
		ZipOutputStream zos;
		zos = new ZipOutputStream(new FileOutputStream(outFile));
		ZipEntry entry = new ZipEntry("/"+this.getFileName().replaceAll("\\.gbk|\\.zip", ""));
		InputStream is = null;
		try{
			//将条目保存到Zip压缩文件当中
			zos.putNextEntry(entry);
			//从文件输入流当中读取数据，并将数据写到输出流当中.
			is = new FileInputStream(f);            
			int length = 0;
			int bufferSize = (int)f.length();
			byte[] buffer = new byte[bufferSize];
			while((length=is.read(buffer,0,bufferSize))>=0){
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
			return new FileInputStream(outFile);
		}catch(IOException ex){
			ex.printStackTrace();
		} finally {
			try{
				if(is != null)is.close();
				if(zos!=null){
					zos.finish();
					zos.close();
				}
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
		return null;  
	}

}
