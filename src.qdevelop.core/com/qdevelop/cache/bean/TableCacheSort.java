package com.qdevelop.cache.bean;

import java.util.Comparator;

public class TableCacheSort implements Comparator<TableCacheItem>{

	@Override
	public int compare(TableCacheItem paramT1, TableCacheItem paramT2) {
		return paramT1.getRate() - paramT2.getRate() ;
	}

}
