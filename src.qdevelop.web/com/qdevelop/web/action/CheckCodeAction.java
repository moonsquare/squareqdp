package com.qdevelop.web.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import com.opensymphony.xwork2.ActionContext;
import com.qdevelop.lang.QDevelopConstant;

@SuppressWarnings("serial")
public class CheckCodeAction extends QDevelopAction{
	private int w,h;
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	} 
	private  String multiChars = "3456789abcdefhkmnrstuvwxzABCDEFGHKMNPRSTUVWXYZ";
	private String simpleChars = "0123456789";


	private ByteArrayInputStream inputStream; 
	private int type;
	public String execute() throws Exception{
		int width=getW(), height=getH();   
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);   
		Graphics g = image.getGraphics();   
		Random random = new Random();   
		g.setColor(getRandColor(200,250));   
		g.fillRect(0, 0, width, height);   
		g.setFont(new Font("Times New Roman",Font.PLAIN,18));   
		g.setColor(getRandColor(160,200));   
		for (int i=0;i<155;i++)   
		{   
			int x = random.nextInt(width);   
			int y = random.nextInt(height);   
			int xl = random.nextInt(12);   
			int yl = random.nextInt(12);   
			g.drawLine(x,y,x+xl,y+yl);   
		}   
		StringBuilder sRand = new StringBuilder();
		String[] fontNames = { "Times New Roman", "Arial", "Book antiqua", "" };
		if(type==0||type==1){
			String tmpChar;
			if(type==1)	tmpChar = simpleChars;
			else tmpChar = multiChars;
			for (int i = 0; i < 4; i++) {
				g.setFont(new Font(fontNames[random.nextInt(3)], Font.ITALIC, height));
				char rand = tmpChar.charAt(random.nextInt(tmpChar.length()));
				sRand.append(rand); 
				g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
				g.drawString(String.valueOf(rand), 16 * i + random.nextInt(6) + 3, height - random.nextInt(4));
			} 
		}else{
			int num1 = (int)(Math.random()*10);
			int num2 = (int)(Math.random()*10);
			int[] numOper = new int[]{0,1};
			int _p = numOper[random.nextInt(2)];
			StringBuffer sb = new StringBuffer();
			sb.append(num1).append(_p==1?"+":"*").append(num2).append("=");
			char[] tmp = sb.toString().toCharArray();
			for (int i = 0; i < 4; i++) {
				g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
				g.drawString(String.valueOf(tmp[i]), 16 * i + random.nextInt(6) + 3, height - random.nextInt(4));
			}
			int result=0;
			if(_p==1)result = num1+num2;
			else result = num1*num2;
			sRand.append(result);
		}

		ActionContext.getContext().getSession().put(QDevelopConstant.WEB_CHECK_CODE, sRand.toString().toLowerCase());   
		g.dispose();   
		ByteArrayOutputStream output = new ByteArrayOutputStream();   
		try{   
			ImageOutputStream imageOut = ImageIO.createImageOutputStream(output);   
			ImageIO.write(image, "JPEG", imageOut);   
			imageOut.close();   
//			System.out.println("Code: "+sRand);
			inputStream = new ByteArrayInputStream(output.toByteArray());   
		}catch(Exception e){   
			e.printStackTrace();
		}       

		return SUCCESS;   
	}   
	public void setInputStream(ByteArrayInputStream inputStream) {   
		this.inputStream = inputStream;   
	}   
	public ByteArrayInputStream getInputStream() {   
		return inputStream;   
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getW() {
		return w>1?w:80;
	}
	public void setW(int w) {
		this.w = w;
	}
	public int getH() {
		return h>1?h:20;
	}
	public void setH(int h) {
		this.h = h;
	} 


}
