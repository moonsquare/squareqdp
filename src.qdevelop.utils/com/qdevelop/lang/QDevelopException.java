package com.qdevelop.lang;

import java.io.IOException;
import java.sql.SQLException;

import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

import ognl.OgnlException;


/**
 * 快速开发系统，友好错误提示<br>
 * 每一种错误均自动记录到相应的日志文件中去
 * @author Janson Gu
 *
 */
public class QDevelopException extends RuntimeException{
	
	private int status;
	private static final long serialVersionUID = 0xc1a865c45ffdc5f9L;
	
	/**
	 * 一般错误信息，友好提示使用，例如空指针异常，自定义的一些异常操作使用
	 * @param frdMessage 友好信息
	 */
	public QDevelopException(String frdMessage)	{
		super(frdMessage);
	}
	
	public QDevelopException(int status,String frdMessage)	{
		super(frdMessage);
		this.status = status;
	}
	
	/**
	 * 集成原来的错误信息，友好展示
	 * @param throwable
	 */
	public QDevelopException(Throwable throwable){
		super(createFriendlyErrMsg(throwable),throwable);
		QLog.getInstance().systemError(createFriendlyErrMsg(throwable),throwable);
		if(QProperties.isDebug){
			System.out.print(QDate.getNow("yyyy-MM-dd HH:mm:ss"));
			throwable.printStackTrace();
		}
	}
	
	/***
	 * 自定义友好信息、异常信息，友好化展示
	 * @param throwable
	 * @param frdMessage
	 */
	public QDevelopException(Throwable throwable, String frdMessage){
		super(frdMessage,throwable);
		QLog.getInstance().systemError(frdMessage,throwable);
		if(QProperties.isDebug){
			System.out.print(QDate.getNow("yyyy-MM-dd HH:mm:ss"));
			throwable.printStackTrace();
		}
	}
	
	protected static String createFriendlyErrMsg(Throwable throwable){
		StringBuffer sb = new StringBuffer();
		if(throwable instanceof NumberFormatException){
			return sb.append("数字格式化错误:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof NullPointerException){
			return  sb.append("调用了未经初始化的对象或者是不存在的对象:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString();
		}else if(throwable instanceof IOException){
			return  sb.append("IO异常:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof ClassNotFoundException){
			return  sb.append("指定的类不存在:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof ArithmeticException){
			return  sb.append("数学运算异常:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof ArrayIndexOutOfBoundsException){
			return  sb.append("数组下标越界:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof IllegalArgumentException){
			return  sb.append("方法的参数错误:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString();
		}else if(throwable instanceof ClassCastException){
			return  sb.append("类型强制转换错误:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof SecurityException){
			return  sb.append("违背安全原则异常:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof SQLException){
			return  sb.append("操作数据库异常:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof NoSuchMethodError){
			return  sb.append("方法末找到异常:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof InternalError){
			return  sb.append("Java虚拟机发生了内部错误:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof Exception){
			return  sb.append("程序内部错误，操作失败:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}else if(throwable instanceof OgnlException){
			return  sb.append("自定义反向执行JAVA类操作失败:").append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString(); 
		}
		return sb.append(throwable.getCause()==null?throwable.getMessage():throwable.getCause()).toString();
	}
	
	/**
	 * 错误状态码
	 * 
	 * 500 	数据库更新没有影响记录数
	 *
	 * 600	未能找到数据库配置文件
	 * 
	 */
	public int getStatus(){
		return this.status;
	}
}
