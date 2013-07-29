package com.qdevelop.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * @author Janson.Gu
 *
 */
public class Network {
	
	public static final String DEFAULT_HOST_NAME	="localhost";
	public static final String DEFAULT_HOST_ADDRESS	="127.0.0.1";
	
	public static String getHostName(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return DEFAULT_HOST_NAME;
		}
	}
	
	public static String getHostAddress(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return DEFAULT_HOST_ADDRESS;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(Network.getHostName());
	}
}
