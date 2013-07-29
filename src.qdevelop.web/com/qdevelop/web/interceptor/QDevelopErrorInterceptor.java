package com.qdevelop.web.interceptor;

import java.io.IOException;
import java.sql.SQLException;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.qdevelop.lang.QDevelopException;

public class QDevelopErrorInterceptor extends AbstractInterceptor{

	private static final long serialVersionUID = 6529908129118666895L;

	public String intercept(ActionInvocation invocation) throws Exception {
		try{
			return invocation.invoke();
		}catch(QDevelopException ex){
			throw ex;
		}catch(NumberFormatException ex){
			throw new QDevelopException(ex,"数字格式化错误！");
		}catch(NullPointerException ex){
			throw new QDevelopException(ex,"调用了未经初始化的对象或者是不存在的对象！");
		}catch(IOException ex){
			throw new QDevelopException(ex,"IO异常！");
		}catch(ClassNotFoundException ex){
			throw new QDevelopException(ex,"指定的类不存在！");
		}catch(ArithmeticException ex){
			throw new QDevelopException(ex,"数学运算异常！");
		}catch(ArrayIndexOutOfBoundsException ex){
			throw new QDevelopException(ex,"数组下标越界!");
		}catch(IllegalArgumentException ex){
			throw new QDevelopException(ex,"方法的参数错误！");
		}catch(ClassCastException ex){
			throw new QDevelopException(ex,"类型强制转换错误！");
		}catch(SecurityException ex){
			throw new QDevelopException(ex,"违背安全原则异常！");
		}catch(SQLException ex){
			throw new QDevelopException(ex,"操作数据库异常！");
		}catch(NoSuchMethodError ex){
			throw new QDevelopException(ex,"方法末找到异常！");
		}catch(InternalError ex){
			throw new QDevelopException(ex,"Java虚拟机发生了内部错误");
		}catch(Exception ex){
			throw new QDevelopException(ex,"程序内部错误，操作失败！");
		}
	}
}
