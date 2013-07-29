package com.qdevelop.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QClass {
	/**
	 * 字符串形式执行JAVA类<br>
	 * 用"::"或":"区分类和所执行的方法<br>
	 * 优先使用"::"执行区分,用"::"时，":"不作为特殊字符处理<br>
	 * 参数直接写到后面，以括号形式写在后面，字符串和数字都直接写,字符串上引号可不写<br>
	 * 例：com.test.TestClass::testMethod(123,'c:/测试参数/dd.txt')
	 * @param params
	 * @return
	 * @throws QDevelopException
	 */
	public static Object reflectRun(String params) throws Exception{
		if(params==null) throw new Exception("JAVA反射机制运行参数为空");
		String[] tmp ;
		if(params.indexOf("::")>-1)
			tmp = params.split("::");
		else
			tmp = params.split(":");
		
		if(tmp.length<2) throw new Exception("JAVA反射机制运行参数值不足！例：com.util.Test|runMethod|test,aa");
		Object[] param = null;
		String method;
		if(tmp[1].indexOf("(")>-1){
			method = tmp[1].substring(0,tmp[1].indexOf("("));
			param = tmp[1].replaceAll(method+"|\\(|\\)|\"|'", "").split(",");
		}else{
			param = new Object[]{};
			method = tmp[1];
		}
		return reflectRun(tmp[0], method, param);
	}

	/**
	 * 运用Java反射机制执行某个方法
	 * @param claSSName	类名
	 * @param method	方法名
	 * @param param		参数
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object reflectRun(String claSSName,String method,Object[] param) throws Exception{
		try{
			Class c =Class.forName(claSSName);
			Constructor con = c.getDeclaredConstructor();
			con.setAccessible(true);
			Object oo = con.newInstance();
			Method methlist[] = c.getDeclaredMethods(); 
			for (int i = 0; i < methlist.length; i++) { 
				Method m = methlist[i]; 
				if(m.getName().equals(method)){
					Class[] partypes = m.getParameterTypes();
					if(partypes.length>0&&param!=null&&param.length>0){
						Object[] arglist = new Object[param.length];
						for(int j=0;j<partypes.length;j++){
							Object val = j>param.length?null:param[j];
							if(val!=null){
								if(partypes[j].equals(String.class)){
									arglist[j] = String.valueOf(val);
								}else if(partypes[j].equals(Integer.class)||partypes[j].equals(int.class)){
									arglist[j] = new Integer(val.toString());
								}else if(partypes[j].equals(Double.class)||partypes[j].equals(double.class)){
									arglist[j] = Double.parseDouble(String.valueOf(val));
								}else if(partypes[j].equals(Boolean.class)||partypes[j].equals(boolean.class)){
									arglist[j] = Boolean.parseBoolean(String.valueOf(val));
								}else if(partypes[j].equals(Map.class)||partypes[j].equals(HashMap.class)){
									arglist[j] = (Map)val;
								}else if(partypes[j].equals(List.class)||partypes[j].equals(ArrayList.class)){
									arglist[j] = (List)val;
								}else arglist[j] = val;
							}
						}
						return m.invoke(oo, arglist);
					}
					return m.invoke(oo, new Object[]{});
				}
			}
		}catch(Exception e){
			throw new Exception(e);
		}
		return null;
	}
	
	public static Object getInstanceClass(String className){
		if(className==null||className.trim().length()==0)return null;
		try {
			return  Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		QClass.reflectRun(null);
	}

}
