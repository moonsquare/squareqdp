package com.qdevelop.utils.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class QSocketClient {
	public static String run(String cmd,String ip,int port){
		Socket socket=null;
		try {
			socket = new Socket(ip, port);
			BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream())); 
			PrintWriter out=new PrintWriter(socket.getOutputStream()); 
			out.println(cmd); 
			out.flush(); 
			return (in.readLine());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(socket!=null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		} 
		return "";
	}
}
