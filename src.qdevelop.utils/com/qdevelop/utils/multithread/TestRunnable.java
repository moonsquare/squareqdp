package com.qdevelop.utils.multithread;

public class TestRunnable implements Runnable{
	private int iii,idx;
	TestRunnable(int i,int idx){
		iii=i;
		this.idx = idx;
	}
	@Override
	public void run() {
		System.out.println(idx+" ==>"+iii+" "+System.currentTimeMillis());
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
	}

}
