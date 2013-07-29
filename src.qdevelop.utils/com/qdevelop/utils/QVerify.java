package com.qdevelop.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Pattern;

import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.license.Hex;
import com.qdevelop.utils.license.LicenseManager;
import com.qdevelop.utils.license.SecurityManage;

public class QVerify {
	private static  QVerify _QVerify = new QVerify();
	public static QVerify getInstance(){
		return _QVerify;
	}

	Map<String,String> infoMaps;
	long lastDateTimer;
	int tag = -4;

	public QVerify(){
		if(infoMaps==null)init();
	}

	@SuppressWarnings("unchecked")
	public void init(){
		InputStreamReader read = null;
		try {
			InputStream is = QSource.getInstance().getSourceAsStream("license.dat");
			if(is==null)return;
			read = new InputStreamReader (is,"gbk");
			BufferedReader reader=new BufferedReader(read);   
			String[] signInfo = reader.readLine().split("zz");
			String infos = SecurityManage.decrypt(signInfo[1],"qdevelop");
			if(LicenseManager.getInstance().verify(Hex.decode(signInfo[0]),Hex.decode(signInfo[2]),infos)){
				infoMaps = QJson.getMapFromJson(infos.trim());
				if(!valideSysInfo(infoMaps))
					tag = -3;
				else{
					if(infoMaps.get("stopDate").equals("1900-01-01")) 
						tag = -2;
					else {
						tag = 0;
						lastDateTimer = QDate.parseDate(infoMaps.get("stopDate"),"yyyy-MM-dd").getTime();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(read!=null)read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean isAccept() throws QDevelopException{
		switch(tag){
		case -4:throw new QDevelopException(QProperties.getInstance().getProperty("System.no.cert.msg"));
		case -3:throw new QDevelopException(QProperties.getInstance().getProperty("System.changeServer.msg"));
		case -2:return true;
		default:return System.currentTimeMillis()<lastDateTimer;
		}
	}

	public boolean valideSysInfo(Map<String,String> info){
		if(info.get("mac").equals(".+") && info.get("ip").equals(".+")) return true;
		System.out.println(1);
		boolean isMacAccept=false,isIpAccept = false;
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				if(ni != null ){
					Pattern _mac = Pattern.compile(info.get("mac"));
					byte[] mac = ni.getHardwareAddress();
					if (mac != null) {
						StringBuffer macAddr = new StringBuffer();
						for (int i = 0; i < mac.length; i++) {
							macAddr.append(String.format("%02X%s", mac[i],(i < mac.length - 1) ? "-" : ""));
						}
						System.out.println("macAddr:"+macAddr);
						if(_mac.matcher(macAddr.toString()).find()){
							isMacAccept = true;
						}
					}
				}
				Pattern _ip = Pattern.compile(info.get("ip").replaceAll("\\.", "\\\\."));
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					if(_ip.matcher(ips.nextElement().getHostAddress()).find()){
						isIpAccept = true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isMacAccept && isIpAccept;
	} 

	public String getCertMsg(){
		StringBuffer sb = new StringBuffer();
		java.util.Iterator<String> itor = infoMaps.keySet().iterator();
		String key;
		while(itor.hasNext()){
			key = itor.next();
			sb.append(key).append(":").append(infoMaps.get(key)).append("<br>");
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		try {
			//			System.out.println(".+".equals(".+"));
			System.out.println(QVerify.getInstance().isAccept());
		} catch (QDevelopException e) {
			e.printStackTrace();
		}

	}
}
