package com.qdevelop.core.utils;

import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.utils.QCache;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

public class CoreUtils {
	
	/**
	 * 系统所有配置重载
	 */
	public static void systemReload(){
		QProperties.getInstance().reload();
		QLog.getInstance().reload();
		/**清理系统缓存**/
		QCache.indexCache().initCache();
		QCache.ehCache().initCache();
		QCache.mapCache().initCache();
		QCache.memCache().initCache();

		/**SQL Config**/
		SQLModelLoader.getInstance().reload();
//		WorkFlowLoader.getInstance().reload();
		TableSequence.clear();
		ConnectFactory.clear();
	}
	

}
