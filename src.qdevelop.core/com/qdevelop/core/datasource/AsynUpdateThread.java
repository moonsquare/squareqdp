package com.qdevelop.core.datasource;

import org.apache.log4j.Logger;

import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;

public class AsynUpdateThread  implements Runnable{
	DBQueryBean qb;
	public static Logger logger = QLog.getInstance().getLog("asynRunning");
	public AsynUpdateThread(DBQueryBean qb){
		this.qb = qb;
	}
	@Override
	public void run() {
		if(qb==null)return;
		try {
			DataBaseFactory.getInstance().update(qb);
			logger.info(new StringBuffer().append("[executed] ").append(qb.explain).toString());
		} catch (QDevelopException e) {
			logger.error(new StringBuffer().append("[executed] ").append(qb.explain).append(" - ").append(e.getMessage()).toString());
		}
	}

}
