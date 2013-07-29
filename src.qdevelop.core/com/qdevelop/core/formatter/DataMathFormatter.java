package com.qdevelop.core.formatter;

import java.util.Map;

import ognl.Ognl;

import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;

public class DataMathFormatter extends AbstractFormatter{
	String[] targetValue,math,surfix;
	private boolean isFormatter;
	@Override
	public void init(InitFormatBean param) {
		targetValue = param.getConfig("targetValue", ",", true);
		math = param.getConfig("math", ",");
		surfix = param.getConfig("surfix", ",");
	}

	@Override
	public void initFormatter(DBStrutsBean struts) {
		isFormatter = targetValue!=null && math!=null && targetValue.length == math.length ;
	}

	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts)
			throws QDevelopException {
		if(isFormatter){
			for(int i=0;i<targetValue.length;i++){
				try {
					data.put(targetValue[i], Ognl.getValue(Ognl.parseExpression(math[i]),data));
					if(surfix!=null&&surfix.length>i&&surfix[i]!=null)data.put(targetValue[i], new StringBuffer().append(data.get(targetValue[i])).append(surfix[i]).toString());
				} catch (Exception e) {
					data.put(targetValue[i],"0");
					e.printStackTrace();
				}
			}
		}
	}
	public boolean isQBQuery(){
		return false;
	}

//	public static void main(String[] args) {
//		HashMap data = new HashMap();
//		data.put("a", 10.0);
//		data.put("b",3);
//		try {
//			System.out.println(Ognl.getValue(Ognl.parseExpression("(a*b-b)/c"),data));
//		} catch (OgnlException e) {
//			e.printStackTrace();
//		}
//	}

}
