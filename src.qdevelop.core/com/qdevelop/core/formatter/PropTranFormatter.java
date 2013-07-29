package com.qdevelop.core.formatter;

import java.util.Map;

import com.qdevelop.core.bean.DBStrutsBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.cache.MapCache;

/**
 * 专门用户翻译状态的
 * @author Janson.Gu
 *
 */
public class PropTranFormatter   extends AbstractFormatter{
	String[] targetKey,propValue;boolean isFormatter;
	
	@Override
	public void init(InitFormatBean param) {
		targetKey = param.getConfig("targetKey", ",",true);
		propValue = param.getConfig("propValue",",");
	}

	@Override
	public void initFormatter(DBStrutsBean struts) {
		isFormatter = targetKey!=null && propValue !=null && targetKey.length == propValue.length;
	}
	private Object tmp;
	@Override
	public void formatter(Map<String, Object> data, DBStrutsBean struts) throws QDevelopException {
		if(isFormatter){
			for(int i=0;i<targetKey.length;i++){
				if(data.get(targetKey[i])!=null){
					tmp = QProperties.getInstance().getJsonValue(new StringBuffer().append(propValue[i]).append(".").append(data.get(targetKey[i])).toString());
//					if(tmp!=null)
						data.put(MapCache.getInstance().getFormatterKey(targetKey[i]), tmp==null?"":tmp);
//					else if(QProperties.getInstance().getProperty(propValue[i])!=null)
//						data.put(MapCache.getInstance().getFormatterKey(targetKey[i]), QProperties.getInstance().getProperty(propValue[i]));
				}
			}
		}
	}

	public boolean isQBQuery(){
		return false;
	}

}
