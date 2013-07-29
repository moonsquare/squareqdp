package com.qdevelop.utils.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class TailFile {
	public void read(String filename, String charset,int begin,int end) { 
		if(charset==null)charset="utf-8";
		RandomAccessFile rf = null;  
		try {  
			rf = new RandomAccessFile(filename, "r");  
			long len = rf.length();  
			long start = rf.getFilePointer();  
			long nextend = start + len - 1;  
			int idx = 0;
			String line;  
			rf.seek(nextend);  
			int c = -1;  
			while (nextend > start && (idx <= end||end == -1 )) {  
				c = rf.read();  
				if (c == '\n' || c == '\r') {  
					line = rf.readLine();  
					if (line != null && (idx > begin||begin==-1)) {  
						String t = new String(line.getBytes("ISO-8859-1"), charset);
						if(isBreak(t))break;
						getReadLine(t);  
					}
					idx++; 
//					} else {  
//						getReadLine(line);  
//					}  
					nextend--;  
				}  
				nextend--;  
				rf.seek(nextend);  
				if (nextend == 0 && (idx < begin||begin==-1)) {// 当文件指针退至文件开始处，输出第一行  
					String t = new String(rf.readLine().getBytes("ISO-8859-1"), charset);
					if(isBreak(t))break;
					getReadLine(t); 
					idx++;
				}  
			}  
		} catch (FileNotFoundException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		} finally {  
			try {  
				if (rf != null)  
					rf.close();  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		}  
	} 
	
	public abstract void getReadLine(String lineStr);
	
	public abstract boolean isBreak(String lineStr);
}
