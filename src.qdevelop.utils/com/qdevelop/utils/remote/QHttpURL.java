package com.qdevelop.utils.remote;

import java.io.IOException;

public abstract class QHttpURL {
	protected abstract void requestData(String data);

	public void getURL(String url) {
		String sCurrentLine=null; 
		java.io.InputStream l_urlStream=null; 
		java.io.BufferedReader l_reader=null;
		java.net.HttpURLConnection l_connection=null;
		try {
			java.net.URL l_url = new java.net.URL(url); 
			l_connection = (java.net.HttpURLConnection) l_url.openConnection();
			l_connection.connect(); 
			l_urlStream = l_connection.getInputStream(); 
			l_reader = new java.io.BufferedReader(new java.io.InputStreamReader(l_urlStream)); 
			while ((sCurrentLine = l_reader.readLine()) != null){ 
				requestData(sCurrentLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(l_reader!=null)
					l_reader.close();
				if(l_urlStream!=null)
					l_urlStream.close();
				l_connection=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	public void getUrl(String url) throws Exception{
		String sCurrentLine=null; 
		java.io.InputStream l_urlStream=null; 
		java.io.BufferedReader l_reader=null;
		java.net.HttpURLConnection l_connection=null;
		try {
			java.net.URL l_url = new java.net.URL(url); 
			l_connection = (java.net.HttpURLConnection) l_url.openConnection();
			l_connection.connect(); 
			l_urlStream = l_connection.getInputStream(); 
			l_reader = new java.io.BufferedReader(new java.io.InputStreamReader(l_urlStream)); 
			while ((sCurrentLine = l_reader.readLine()) != null){ 
				requestData(sCurrentLine);
			}
		} catch (Exception e) {
			throw e;
		}finally{
			try {
				if(l_reader!=null)
					l_reader.close();
				if(l_urlStream!=null)
					l_urlStream.close();
				l_connection=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
}
