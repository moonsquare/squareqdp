package com.qdevelop.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.core.bean.DBResultBean;
import com.qdevelop.core.standard.IAfterRun;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ClassUtils {

	/**
	 *  给一个ClassBean赋值
	 * @param Map request 请求参数
	 * @param bean	原对象Bean
	 * @return 反馈一个赋值好的新对象
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public static void setClassValue(Map request,Object bean) throws Exception{
		Object key,value;
		Class<?> c = bean.getClass();
		Method methlist[] = c.getMethods(); 
		for (int i = 0; i < methlist.length; i++) { 
			Method m = methlist[i]; 
			if(m.getName().startsWith("set")){
				key = new StringBuffer().append(m.getName().substring(3,4).toLowerCase()).append(m.getName().substring(4)).toString();
				value = request.get(key) == null ? request.get(m.getName().replace("set", "is")):request.get(key);
				Class[] paramtype = m.getParameterTypes();
				if(value!=null&&paramtype.length==1)
					m.invoke(bean, new Object[]{parseParam(paramtype[0],value)});
			}
		}
	}

	public static String beanToJson(Object bean){
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		try {		
			Class<?> c = bean.getClass();
			Method methlist[] = c.getMethods(); 
			for (int i = 0; i < methlist.length; i++) { 
				Method m = methlist[i]; 
				if(m.getName().indexOf("get")>-1||m.getName().indexOf("is")>-1){
					if(!m.getName().equals("getClass")){
					if(i>0)sb.append(",");
					if(m.getName().indexOf("is")>-1)
						sb.append(m.getName()).append(":").append(m.invoke(bean, new Object[]{}));
					else
						sb.append(m.getName().substring(3,4).toLowerCase()).append(m.getName().substring(4))
						.append(":'").append(m.invoke(bean, new Object[]{})).append("'");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		sb.append("}");
		return sb.toString();
	}

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
	public  static  Object reflectRun(String claSSName,String method,Object[] param) throws Exception{
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
								arglist[j] = parseParam(partypes[j],val);
							}
						}
						return m.invoke(oo, arglist);
					}
					return m.invoke(oo, new Object[]{});
				}
			}
		}catch(Exception e){
			throw e;
		}
		return null;
	}
	
	private static Object parseParam(Class partypes,Object val){
		Object args;
		if(partypes.equals(String.class)){
			args = String.valueOf(val);
		}else if(partypes.equals(Integer.class)||partypes.equals(int.class)){
			args = new Integer(val.toString());
		}else if(partypes.equals(Double.class)||partypes.equals(double.class)){
			args = Double.parseDouble(String.valueOf(val));
		}else if(partypes.equals(Boolean.class)||partypes.equals(boolean.class)){
			args = Boolean.parseBoolean(String.valueOf(val));
		}else if(partypes.equals(Map.class)||partypes.equals(HashMap.class)){
			args = (Map)val;
		}else if(partypes.equals(List.class)||partypes.equals(ArrayList.class)){
			args = (List)val;
		}else if(partypes.equals(QueryBean.class)){
			args = (QueryBean)val;
		}else if(partypes.equals(DBResultBean.class)){
			args = (DBResultBean)val;
		}else args = val;
		return args;
	}

	public  static  boolean isAfterRunClass(String className){
		try {
			Object oo = Class.forName(className).getDeclaredConstructor().newInstance();
			if(oo instanceof IAfterRun)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		try {
//						ClassUtils.reflectRun("com.qdevelop.utils.FileFilterTest::test(Janson)");
//			Map params = new HashMap();
//			params.put("sql", "janson");
//			params.put("uniTable", "aaaa");
//			params.put("isCacheAble", "true");
//			params.put("allCount", "100");
//			QueryBean qb = new DataBaseQueryBean();
//			//			qb.setQuery("aaa");
//			//			qb.setNowPage(1);
//			//			qb.setQueryType("database");
//			//			System.out.println(ClassUtils.beanToJson(qb));
//			ClassUtils.setClassValue(params, qb);
//			System.out.println(ClassUtils.beanToJson(qb));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//		System.out.println(sdf.format(CalendarUtils.parseDate("2010/05/05 23")));
		//		System.out.println(" 2010/05/05 23:23:00 ".replaceAll(" +", " ").replaceAll("^ | $", "").replaceAll("-|/|:| ", "#"));
	}

}
