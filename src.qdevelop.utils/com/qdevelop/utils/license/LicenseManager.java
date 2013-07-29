package com.qdevelop.utils.license;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class LicenseManager {
	private static LicenseManager _LicenseManager = new LicenseManager();
	public static LicenseManager getInstance(){return _LicenseManager;}

	private byte[] priKey = null;
	public byte[] pubKey = null;

	public LicenseManager(){
		try {
			java.security.KeyPairGenerator keygen = java.security.KeyPairGenerator.getInstance("RSA");
			SecureRandom secrand = new SecureRandom();
			secrand.setSeed("QDevelop3.0 Design By Janson".getBytes()); // 初始化随机产生器
			keygen.initialize(1024, secrand);
			KeyPair keys = keygen.genKeyPair();

			PublicKey pubkey = keys.getPublic();
			PrivateKey prikey = keys.getPrivate();

			pubKey = pubkey.getEncoded();
			priKey =prikey.getEncoded();

		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("生成密钥对失败");
		}
	}

	public byte[] sign(String info) {
		if(priKey==null)return null;
		try {
			PKCS8EncodedKeySpec priPKCS8=new PKCS8EncodedKeySpec(priKey); 
			KeyFactory keyf=KeyFactory.getInstance("RSA");
			PrivateKey myprikey=keyf.generatePrivate(priPKCS8);
			// 用私钥对信息生成数字签名
			java.security.Signature signet = java.security.Signature.getInstance("MD5withRSA");
			signet.initSign(myprikey);
			signet.update(Hex.decode(info));
			return signet.sign(); // 对信息的数字签名
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("签名并生成文件失败");
		}
		return null;
	}
	
	
	public boolean verify(byte[] signCode,String info){
		return verify(signCode,pubKey,info);
	}
	
	public boolean verify(byte[] signCode,byte[] publicKey,String info){
		if(signCode == null || publicKey ==null)return false;
		try {
			X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(publicKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey pubKey = keyFactory.generatePublic(bobPubKeySpec);
			
			java.security.Signature signetcheck = java.security.Signature.getInstance("MD5withRSA");
			signetcheck.initVerify(pubKey);
			signetcheck.update(Hex.decode(info));
			return signetcheck.verify(signCode);
		}
		catch (java.lang.Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
//	public static void main(String[] args) {
//		String info = "{'company':'开拓天际','user':'janson','mac':'12-34-55-55-99-E2','stopDate':'2011-06-08'}";
//		String myinfo = SecurityManage.encrypt(info,"janson_sss");	
//		String singInfo = Hex.encode(LicenseManager.getInstance().sign(info));
//		System.out.println(LicenseManager.getInstance().verify(Hex.decode(singInfo), SecurityManage.decrypt(myinfo, "janson_sss")));
//	}

}
