package com.qdevelop.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;


public class QSerializSize {
	public static int getSerializObjectSize(Serializable obj){
		DumbOutputStream buf = new DumbOutputStream();
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(buf);
			os.writeObject(obj);
			return buf.count;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				os.close();
			} catch (Exception e) {
			}
		}
		
	}
}
class DumbOutputStream extends OutputStream {
	int count = 0;
	public void write(int b) throws IOException {
		count++; // 只计数，不产生字节转移
	}
}
