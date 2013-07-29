package com.qdevelop.utils.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class QSocketServer extends Thread implements IQSocketServer{
	private Socket socket;
	public QSocketServer(Socket socket){
		this.socket = socket;
	}
	
	public void run(){
		try{ 
			BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream())); 
			PrintWriter out=new PrintWriter(socket.getOutputStream()); 
			String str=in.readLine(); 
			disposeSocket(str);
			out.flush();
		}catch(IOException ex){ 
		}finally{ 
			if(socket!=null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				} 
		} 
	}
	public void disposeSocket(String content){
		System.out.println(content);
	}
	
	public static void start(int port){
		try {
			ServerSocket server = new ServerSocket(port);
			Socket socket = null;
			while (true) {
				socket = server.accept();
				new QSocketServer(socket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
