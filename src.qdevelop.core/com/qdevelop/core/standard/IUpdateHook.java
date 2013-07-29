package com.qdevelop.core.standard;

import java.sql.Statement;

import com.qdevelop.lang.QDevelopException;

public interface IUpdateHook {
	
	public void collect(String sql,int fetchNum) throws QDevelopException;
	
	public void flush(Statement stmt) throws QDevelopException;
	
}
