package com.qdevelop.utils.multithread;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.qdevelop.utils.QLog;

public class MultiThreadQueue extends ConcurrentLinkedQueue<Runnable>{
	private static final long serialVersionUID = -5578417746089072721L;
	public static Logger ayscRunning = QLog.getInstance().getLog("asynRunning");
	private boolean isRunning = false;
	private int idx;
	public MultiThreadQueue(int idx){
		this.idx = idx;
	}
	
	public boolean add(Runnable mission){
		boolean o = this.offer(mission);
		if(!isRunning){
			new Thread(){
				public void run(){
					sync();
				}
			}.start();
		}
		return o;
	}
	
	public boolean sync(){
		if(isRunning || this.isEmpty())return false;
		isRunning = true;
		int i=0;
		long cur = System.currentTimeMillis();
		while(!this.isEmpty()){
			Runnable mission = this.poll();
			mission.run();
			++i;
		}
		ayscRunning.info("[MultiThreadQueue] ("+idx+") running mission:["+i+"] use:["+(System.currentTimeMillis()-cur)+"]");
		isRunning=false;
		return true;
	}
	
	public boolean isComplete(){
		return !isRunning&&this.isEmpty();
	}
}
