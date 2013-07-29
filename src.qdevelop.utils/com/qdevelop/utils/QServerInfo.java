package com.qdevelop.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class QServerInfo {
	public static String getIp(){
		StringBuffer ipsss = new StringBuffer();
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
//				if(ni != null ){
//					byte[] mac = ni.getHardwareAddress();
//					if (mac != null) {
//						StringBuffer macAddr = new StringBuffer();
//						for (int i = 0; i < mac.length; i++) {
//							macAddr.append(String.format("%02X%s", mac[i],(i < mac.length - 1) ? "-" : ""));
//						}
//						ipsss.append("|").append(macAddr.toString());
//					}
//				}
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					ipsss.append("|").append(ips.nextElement().getHostAddress());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ipsss.length()==0?null:ipsss.substring(1).toString();
	}
	public static void main(String[] args) {
		System.out.println(getIp());
	}
}
