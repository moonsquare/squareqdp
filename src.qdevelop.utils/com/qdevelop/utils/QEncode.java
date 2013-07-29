package com.qdevelop.utils;

import java.io.UnsupportedEncodingException;

public class QEncode {

	public static void judgeEncodePrint(String msg){
		try {
			System.out.println(QString.append("1(self) : \t\t",msg));
			System.out.println(QString.append("2(utf-8) : \t\t",new String(msg.getBytes("utf-8"))));
			System.out.println(QString.append("3(iso-8859-1) : \t\t",new String(msg.getBytes("iso-8859-1"))));
			System.out.println(QString.append("4(gbk) : \t\t",new String(msg.getBytes("gbk"))));
			System.out.println(QString.append("5(gb18030) : \t\t",new String(msg.getBytes("gb18030"))));
			System.out.println(QString.append("6(unicode) : \t\t",new String(msg.getBytes("unicode"))));

			System.out.println(QString.append("7(utf-8)(GBK) : \t\t",new String(msg.getBytes("utf-8"),"gbk")));
			System.out.println(QString.append("8(utf-8)(iso-8859-1) : \t\t",new String(msg.getBytes("utf-8"),"iso-8859-1")));
			System.out.println(QString.append("9(utf-8)(gb18030) : \t\t",new String(msg.getBytes("utf-8"),"gb18030")));
			System.out.println(QString.append("10(utf-8)(unicode) : \t\t",new String(msg.getBytes("utf-8"),"unicode")));


			System.out.println(QString.append("11(GBK)(utf-8) : \t\t",new String(msg.getBytes("gbk"),"utf-8")));
			System.out.println(QString.append("12(GBK)(iso-8859-1) : \t\t",new String(msg.getBytes("gbk"),"iso-8859-1")));
			System.out.println(QString.append("13(GBK)(gb18030) : \t\t",new String(msg.getBytes("gbk"),"gb18030")));
			System.out.println(QString.append("14(GBK)(unicode) : \t\t",new String(msg.getBytes("gbk"),"unicode")));

			System.out.println(QString.append("15(iso-8859-1)(utf-8) : \t\t",new String(msg.getBytes("iso-8859-1"),"utf-8")));
			System.out.println(QString.append("16(iso-8859-1)(GBK) : \t\t",new String(msg.getBytes("iso-8859-1"),"gbk")));
			System.out.println(QString.append("17(iso-8859-1)(gb18030) : \t\t",new String(msg.getBytes("iso-8859-1"),"gb18030")));
			System.out.println(QString.append("18(iso-8859-1)(unicode) : \t\t",new String(msg.getBytes("iso-8859-1"),"unicode")));


			System.out.println(QString.append("19(gb18030)(utf-8) : \t\t",new String(msg.getBytes("gb18030"),"utf-8")));
			System.out.println(QString.append("20(gb18030)(GBK) : \t\t",new String(msg.getBytes("gb18030"),"gbk")));
			System.out.println(QString.append("21(gb18030)(iso-8859-1) : \t\t",new String(msg.getBytes("gb18030"),"iso-8859-1")));
			System.out.println(QString.append("22(gb18030)(unicode) : \t\t",new String(msg.getBytes("gb18030"),"unicode")));

			System.out.println(QString.append("23(unicode)(utf-8) : \t\t",new String(msg.getBytes("unicode"),"utf-8")));
			System.out.println(QString.append("24(unicode)(GBK) : \t\t",new String(msg.getBytes("unicode"),"gbk")));
			System.out.println(QString.append("25(unicode)(iso-8859-1) : \t\t",new String(msg.getBytes("unicode"),"iso-8859-1")));
			System.out.println(QString.append("26(unicode)(gb18030) : \t\t",new String(msg.getBytes("unicode"),"gb18030")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static boolean isValidUtf8(byte[] b,int aMaxCount){
		int lLen=b.length,lCharCount=0;
		for(int i=0;i<lLen && lCharCount<aMaxCount;++lCharCount){
			byte lByte=b[i++];//to fast operation, ++ now, ready for the following for(;;)
			if(lByte>=0) continue;//>=0 is normal ascii
			if(lByte<(byte)0xc0 || lByte>(byte)0xfd) return false;
			int lCount=lByte>(byte)0xfc?5:lByte>(byte)0xf8?4
					:lByte>(byte)0xf0?3:lByte>(byte)0xe0?2:1;
					if(i+lCount>lLen) return false;
					for(int j=0;j<lCount;++j,++i) if(b[i]>=(byte)0xc0) return false;
		}
		return true;
	}

}
