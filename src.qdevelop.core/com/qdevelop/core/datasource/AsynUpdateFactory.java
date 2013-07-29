package com.qdevelop.core.datasource;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.schedule.ISchedule;
import com.qdevelop.core.schedule.QScheduleFactory;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QString;

public class AsynUpdateFactory  extends ConcurrentLinkedQueue<DBQueryBean>{

	private static final long serialVersionUID = -8479021796441510343L;
	private static AsynUpdateFactory _AyscUpdateFactory = new AsynUpdateFactory();
	private static boolean isRunning = false;
	private byte[] _lock = new byte[0];
	private Logger ayscSql = QLog.getInstance().getLog("asynSql");
	private Logger ayscRunning = QLog.getInstance().getLog("asynRunning");
	
	public AsynUpdateFactory(){
		QScheduleFactory.getInstance().addSchedule(new ISchedule(){
			public void run() {
				if(AsynUpdateFactory.getInstance().sync()){
					System.out.println("[AsynUpdateFactory] back up running...");
				}
			}
		}, 60, 30);
	}

	public static AsynUpdateFactory getInstance(){
		return _AyscUpdateFactory;
	}

	public int addPool(DBQueryBean qb){
		if(qb==null)return 0;
		synchronized(_lock){
			String mark = QString.get32MD5(new StringBuffer().append(qb.sqlIndex).append(System.currentTimeMillis()).toString());
			qb.explain = mark;
			qb.isAsync=true;
			for(String sql:qb.getSqls()){
				ayscSql.info(new StringBuffer().append("[").append(mark).append("] ").append(sql).append(" [").append(qb.getConnect()).append("]").toString());
			}
			this.offer(qb);
			if(!isRunning){
				new Thread(){
					public void run() {
						AsynUpdateFactory.getInstance().sync();
					}
				}.start();
			}
			return 1;
		}

	}

	public boolean sync(){
		if(isRunning || this.isEmpty())return false;
		isRunning = true;
		ExecutorService exec=null;
		try {
			long s = System.currentTimeMillis();
			int idx=0;
			exec = Executors.newFixedThreadPool(5);
			while(!this.isEmpty()){
				DBQueryBean qb = this.poll();
				if(qb!=null){
					++idx;
					exec.execute(new AsynUpdateThread(qb));
				}
			}
			exec.shutdown();
			// 等待子线程结束，再继续执行下面的代码
			exec.awaitTermination(120, TimeUnit.SECONDS);
			isRunning = false;
			ayscRunning.info(new StringBuffer().append("[sync Running] size:").append(idx).append(" use:").append(System.currentTimeMillis()-s).append("ms").toString());
		} catch (InterruptedException e) {
			isRunning = false;
			if(exec!=null)exec.shutdown();
			e.printStackTrace();
		}
		return true;
	}

}
