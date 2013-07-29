package com.qdevelop.cache.bean;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 
 * TODO 处理cache操作的队列
 * 
 * @author Janson
 * 2012-5-25
 *
 */
public class AsynCacheQueue extends ConcurrentLinkedQueue<QueueItem>{

	/**1 增加操作**/
	public static int	ADD = 1;
	/**2 更新操作**/
	public static int UPDATE = 2;
	/**3 删除操作**/
	public static int DELETE = 3;
	/**4 命中cache**/
	public static int ADDRATE=4;

	public static int DELRATE=5;
	public int size = 0;
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 1881567109557189824L;

	public boolean offer(String key,String config,String query,int oper){
		++size;
		this.offer(new QueueItem(key,config,query,oper));
		return true;
	}

	public QueueItem poll(){
		--size;
		return super.poll();
	}

	public int size(){
		return size;
	}
	
	public boolean hasData(){
		return size > 0;
	}

	//	public QueueItem peek(){
	//		QueueItem item = this.peek();
	//		return item;
	//	}

}
