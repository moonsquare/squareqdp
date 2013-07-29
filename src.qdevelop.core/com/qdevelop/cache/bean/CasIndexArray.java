package com.qdevelop.cache.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.qdevelop.cache.interfaces.ITacties;

/**
 * 
 * TODO 存放index下面所有的相关请求
 * 
 * @author Janson 2012-5-24
 * 
 */
public class CasIndexArray extends HashMap<String, IndexItem> implements
		Serializable {
	String stackKey;
	public String nextStackKey;
	public long modifyTime;

	public CasIndexArray(String stackKey) {
		this.stackKey = stackKey;
		modifyTime = System.currentTimeMillis();
	}

	public String getStackKey() {
		return this.stackKey;
	}

	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 351902603797606757L;

	public void mergeIndexItem(IndexItem ii) {
		if (ii == null)
			return;
		IndexItem arrayItem = this.get(ii.toKey());
		if (arrayItem != null) {
			if (ii.getQuery() == null) {
				ii.query = arrayItem.getQuery();
			}
			if (Math.abs(arrayItem.getLastTime() - ii.getLastTime()) < 60000) {// 一分钟之内的缓存
				ii.setCached(ii.isCached() || arrayItem.isCached());
			}
			if (!ii.isCached())
				ii.setLastclicktime(arrayItem.getLastTime());
		}
		this.put(ii.toKey(), ii);
	}

	public void addTacties(QueueItem queueItem) {
		IndexItem ii = findKeyItem(queueItem);
		if (ii == null) {
			ii = new IndexItem(queueItem);
		} else {
			ii.addRate(1);
		}
		this.put(ii.toKey(), ii);
	}

	public IndexItem addItemRate(String key, String config, int diff) {
		IndexItem ii = findKeyItem(key, config);
		if (ii != null) {
			ii.addRate(diff);
		}
		return ii;
	}

	public IndexItem addItemRate(QueueItem queueItem, int diff) {
		IndexItem ii = findKeyItem(queueItem.getKey(), queueItem.getConfig());
		if (ii != null) {
			if (queueItem.getQuery() != null
					&& queueItem.getQuery().length() > 0) {
				ii.query = queueItem.getQuery();
			}
			ii.addRate(diff);
		} else {
			ii = new IndexItem(queueItem);
			this.put(ii.toKey(), ii);
		}
		return ii;
	}

	public IndexItem findKeyItem(String key, String config) {
		return this.get(itemKey(key, config));
	}

	public IndexItem findKeyItem(QueueItem queueItem) {
		return this.get(itemKey(queueItem.key, queueItem.config));
	}

	public void removeItem(String key, String config) {
		this.remove(itemKey(key, config));
	}

	/**
	 * 
	 * TODO 根据一个key获取其他关联的keys
	 * 
	 * @param key
	 * @param config
	 * @return
	 */
	public Collection<String> getCasKeys(String key, String config) {
		Collection<String> collect = new HashSet<String>();
		Iterator<IndexItem> itor = this.values().iterator();
		while (itor.hasNext()) {
			IndexItem t = itor.next();
			if (t.isCasKey(key, config)) {
				collect.add(t.fullKey);
			}
		}
		return collect;
	}

	/**
	 * 
	 * TODO 打印索引信息
	 * 
	 */
	public void print() {
		Iterator<IndexItem> itor = this.values().iterator();
		while (itor.hasNext()) {
			IndexItem t = itor.next();
			System.out.println(t.toString());
		}
	}

	/**
	 * 
	 * TODO 获取最近使用缓存
	 * 
	 * @param tacties
	 * @param size
	 * @return
	 */
	public String[] getLastCacheKeys(ITacties tacties, int size) {
		IndexItem[] arrayOfObject = this.values().toArray(new IndexItem[] {});
		Arrays.sort(arrayOfObject, new CasIndexSort());
		if (arrayOfObject.length < size)
			size = arrayOfObject.length;
		String[] keys = new String[size];
		for (int i = 0; i < size; i++) {
			if (arrayOfObject[i].isCached())
				keys[i] = tacties.toKey(arrayOfObject[i].getKey(),
						arrayOfObject[i].getConfig());
		}
		return keys;
	}

	private String itemKey(String key, String config) {
		return new StringBuffer().append(config).append(key).toString();
	}

}
