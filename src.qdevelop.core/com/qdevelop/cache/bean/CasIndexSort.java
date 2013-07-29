package com.qdevelop.cache.bean;

import java.util.Comparator;

public class CasIndexSort implements Comparator<IndexItem>{

	@Override
	public int compare(IndexItem paramT1, IndexItem paramT2) {
		if(!paramT1.isCached() && !paramT2.isCached()){
			return ((int)(paramT2.getLastTime() - paramT1.getLastTime()));
		}else{
			int r1 = paramT1.isCached()?1:-1;
			int r2 = paramT2.isCached()?1:-1;
			return ((int)(paramT2.getLastTime()*r1 - paramT1.getLastTime()*r2));
		}
	}

}
