package com.qdevelop.web.handler;

import java.io.File;


public interface ILogParser {
	
	public boolean isTarget(String logContent);
	
	public String parser(String logContent);
	
	public void collect(String afterParseLog);
	
	public int getMax();
	
	public int getStart();
	
	public File getLogFile();
	
}
