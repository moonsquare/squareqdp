package com.qdevelop.web.handler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;

@SuppressWarnings({"rawtypes"})
public class LogLoader {
	String encode = "utf-8";
	public static LogLoader getInstance(){return new LogLoader();}
	public static LogLoader getInstance(String encode){return new LogLoader(encode);}
	
	public LogLoader(){}
	public LogLoader(String encode){this.encode = encode;}
	
	private String readLine(RandomAccessFile randomAccessFile,long index)throws IOException{
		StringBuffer line = new StringBuffer();  
		char c;  
		while(index > 0){  
			index--; 
			randomAccessFile.seek(index);  
			c = (char)randomAccessFile.read();  
			if (c == '\n' || c == '\r'){  
				if (line.length() < 1){  
					continue;  
				} 
				break;  
			}  
			line.append(c); 
		}
		return line == null ? null : new String(line.reverse().toString().getBytes("ISO8859-1"), encode);  
	}
	
	public ILogParser loadLog(ILogParser parser) throws QDevelopException{
		int start = parser.getStart();
		int maxNum = parser.getMax();
		File _logFile = parser.getLogFile();
		if(QProperties.isDebug)System.out.println(QString.append("Read Log:",_logFile.getAbsolutePath()));
		RandomAccessFile randomAccessFile=null;
		try {
			randomAccessFile = new RandomAccessFile(_logFile, "r");
			long length = randomAccessFile.length();  
			int idx = 0;
			String line;   
			while ((line = readLine(randomAccessFile,length)) != null) {
				if(length < 1 || idx > maxNum)break;
				if(idx >= start &&parser.isTarget(line))
					parser.collect(parser.parser(line));
				length = length - line.length() - 2;
				idx ++;
			}
		} catch (Exception e) {
			throw new QDevelopException(e);
		}finally{
			try {
				if(randomAccessFile!=null)
					randomAccessFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return parser;  
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		LogLoader lah = new LogLoader();
//		lah.loadLog(new File("D:\\WEB-SERVER\\Tomcat 6.0\\log\\operater.log"));
//		String log = "D:\\WEB-SERVER\\Tomcat 6.0\\log\\security.log";
//		
		LogLoginParser parser = (LogLoginParser) LogLoader.getInstance().loadLog(new LogLoginParser(null,null,null,20));
		for(Map data :parser){
			System.out.println(data);
		}
		
	}

}
