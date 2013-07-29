package com.qdevelop.cache;

import java.io.Serializable;
import java.util.Date;

import com.qdevelop.lang.QDevelopException;

public interface QCache {
	
	public void init();

	public void shutdown();
	
	public boolean add(String key,String config,Serializable value,int exp) throws QDevelopException;
	
	public boolean add(String key,String config,Serializable value,Date endDate) throws QDevelopException;
	
	public Serializable get(String key,String config,int maxWaitTimer) throws QDevelopException;
	
	public boolean casUpdate(String key,String config,Serializable value, int timer)  throws QDevelopException;
	
	public long incr(String key,long delta,long initValue) throws QDevelopException;
	
	public long dccr(String key,long delta,long initValue) throws QDevelopException;
	
	public void remove(String key,String config);
}
