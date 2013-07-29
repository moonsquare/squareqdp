package com.qdevelop.utils.multithread;

import com.qdevelop.core.schedule.QScheduleFactory;

public class MultiThreadFactory {
	private int max_thread; 
	private MultiThreadQueue[] multiThreadQueues;
	public MultiThreadFactory(int maxThreadBuffer){
		max_thread = maxThreadBuffer;
		multiThreadQueues = new MultiThreadQueue[maxThreadBuffer];
		for(int i=0;i<max_thread;i++){
			multiThreadQueues[i] = new MultiThreadQueue(i);
		}
		QScheduleFactory.getInstance().addSchedule(new Runnable(){
			public void run() {
				for(final MultiThreadQueue queue:multiThreadQueues){
					if(queue.isComplete())continue;
					new Thread(){
						public void run(){
							queue.sync();
						}
					};
				}
			}
		}, 0, 10);
	}

	public void add(Object hashValue,Runnable mission){
		if(hashValue==null||mission==null)return;
		multiThreadQueues[(Math.abs(hashValue.hashCode())%max_thread)].add(mission);
	}

	/**
	 * 判断是不是所有的任务都执行完毕 
	 * @return
	 */
	public boolean isComplete(){
		for(MultiThreadQueue _MultiThreadQueue : multiThreadQueues){
			if(!_MultiThreadQueue.isComplete())return false;
		}
		return true;
	}


	public static void main(String[] args) {
		MultiThreadFactory mtf = new MultiThreadFactory(5);
		for(int i=0;i<1000;i++){
			int mx=(Math.abs(new Integer(i).hashCode())%mtf.max_thread);
			TestRunnable tr = new TestRunnable(i,mx);
			mtf.add(i, tr);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		QScheduleFactory.getInstance().shutdown();
	}
}
