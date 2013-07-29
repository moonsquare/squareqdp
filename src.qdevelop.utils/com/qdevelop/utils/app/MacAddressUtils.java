package com.qdevelop.utils.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MacAddressUtils {
	/**
	 * 执行系统命令
	 * @param commond	命令
	 * @param newLine	换行符
	 * @return 执行结果
	 */
	public static String getMacAddr(String commond,String newLine){
		if(commond == null) return "Bad Commond!";
		String os = getOSName();   
//		System.out.println(os);   
		if(os.startsWith("windows")){   
			//本地是windows   
			return Window(commond,newLine);   
		}else{   
			//本地是非windows系统 一般就是unix   
			return Linux(commond,newLine);   
		} 
	}
	private static  String getOSName() {   
		return System.getProperty("os.name").toLowerCase();   
	}   
	/**  
	 * 获取unix网卡的mac地址.  
	 * 非windows的系统默认调用本方法获取.如果有特殊系统请继续扩充新的取mac地址方法.  
	 * @return mac地址  
	 */  
	private static String Linux(String commond,String newLine) {   
		BufferedReader bufferedReader = null;   
		Process process = null;   
		try {   
			process = Runtime.getRuntime().exec(commond);// linux下的命令，一般取eth0作为本地主网卡 显示信息中包含有mac地址信息   
			bufferedReader = new BufferedReader(new InputStreamReader(process   
					.getInputStream()));   
			String line = null;   
			StringBuffer tmp = new StringBuffer();
			while ((line = bufferedReader.readLine()) != null) {   
				tmp.append(line).append(newLine);
			}   
			return tmp.toString();
		} catch (IOException e) {   
			e.printStackTrace();   
		} finally {   
			try {   
				if (bufferedReader != null) {   
					bufferedReader.close();   
				}   
			} catch (IOException e1) {   
				e1.printStackTrace();   
			}   
			bufferedReader = null;   
			process = null;   
		}   

		return "Bad Commond!";   
	}   

	/**  
	 * 获取widnows网卡的mac地址.  
	 * @return mac地址  
	 */  
	private static String Window(String commond,String newLine) {   
		BufferedReader bufferedReader = null;   
		Process process = null;   
		try {   
			process = Runtime.getRuntime().exec(commond);// windows下的命令，显示信息中包含有mac地址信息   
			bufferedReader = new BufferedReader(new InputStreamReader(process   
					.getInputStream()));   
			String line = null;   
			StringBuffer tmp = new StringBuffer();
			while ((line = bufferedReader.readLine()) != null) {   
				tmp.append(line).append(newLine); 
			}   
			return tmp.toString();
		} catch (IOException e) {   
			e.printStackTrace();   
		} finally {   
			try {   
				if (bufferedReader != null) {   
					bufferedReader.close();   
				}   
			} catch (IOException e1) {   
				e1.printStackTrace();   
			}   
			bufferedReader = null;   
			process = null;   
		}   
		return "Bad Commond!";   
	}   


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(MacAddressUtils.Window("dir c:", "\n"));
	}
}
