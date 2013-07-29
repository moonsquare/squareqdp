package com.qdevelop.cache.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.rubyeye.xmemcached.GetsResponse;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import com.google.code.yanf4j.core.impl.StandardSocketOption;
import com.qdevelop.cache.impl.memcached.MemCacheConfig;
import com.qdevelop.cache.interfaces.ICacheImpl;
import com.qdevelop.cache.interfaces.ITacties;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

public class MemCachedImpl  implements ICacheImpl{
	private static final MemCachedImpl memcached = new MemCachedImpl();
	private MemcachedClient client;MemcachedClientBuilder builder;
	int reBuildTips = 0;boolean isActive ;long lastRebuild=0;
	private Lock lock = new ReentrantLock();

	public static MemCachedImpl getInstance(){
		return memcached;
	} 
	public MemCachedImpl(){
		init();
	}

	private boolean rebuild(){
		isActive = false;
		if((System.currentTimeMillis() - lastRebuild) < (reBuildTips+1)*(reBuildTips+1)*2000)return false;
		if(builder==null){
			init();
			return false;
		}
		if(reBuildTips < 10 || (System.currentTimeMillis() - lastRebuild > 600000)){//十分钟后，自动重联
			lock.lock();
			reBuildTips++;
			if(reBuildTips>10)reBuildTips=reBuildTips%10;
			try {
				if(client!=null)
					client.shutdown();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try {
				client = builder.build();
				//关闭心跳检查，关闭客户端
				client.setEnableHeartBeat(false);
				builder.getConfiguration().setStatisticsServer(false);
				client.setMergeFactor(300);   //默认是150，修改到300
				client.setOptimizeMergeBuffer(true);  //关闭合并buffer的优化
				QLog.getInstance().systemWarn("rebuild memcache client!");
				lastRebuild = System.currentTimeMillis();
				isActive = true;
				reBuildTips=0;
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}finally{
				lock.unlock();
			}
		}
		return false;
	}

	private synchronized void init(){
		if(!QProperties.globeCache)return;
		lock.lock();
		try {
			shutdown();
			builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(MemCacheConfig.getInstance().getServiceURL()));
			System.out.println("Memcached "+MemCacheConfig.getInstance().getServiceURL());

			//启动网络层优化配置
			builder.setSocketOption(StandardSocketOption.SO_RCVBUF, 512 * 1024); // 设置接收缓存区为512K，默认16K
			builder.setSocketOption(StandardSocketOption.SO_SNDBUF,512 * 1024); // 设置发送缓冲区为512K，默认为8K
			builder.setSocketOption(StandardSocketOption.TCP_NODELAY, false); // 启用nagle算法，提高吞吐量，默认关闭
			//			
			//			builder.setSessionLocator(new KetamaMemcachedSessionLocator());       
			//						builder.setBufferAllocator(new CachedBufferAllocator());
			//使用二进制数据存储
			//			builder.setCommandFactory(new BinaryCommandFactory());

			builder.setConnectionPoolSize(MemCacheConfig.getInstance().maxPool);  //设成25或更高就ok，设小了就出异常
			//			builder.setFailureMode(false);
			//			builder.setTranscoder(new SerializingTranscoder());
			//			builder.getConfiguration().getStatisticsInterval()

			client=builder.build();//client是成功了

			//关闭心跳检查，关闭客户端
			client.setEnableHeartBeat(false);
			builder.getConfiguration().setStatisticsServer(false);

			client.setMergeFactor(300);   //默认是150，修改到300
			client.setOptimizeMergeBuffer(true);  //关闭合并buffer的优化

			isActive = true;
		} catch (IOException e) {
			QLog.getInstance().systemError(e.getMessage(), e);
			e.printStackTrace();
		} 
		lock.unlock();
	}

	@Override
	public boolean add(String key, Serializable value, String config,ITacties tatics) throws QDevelopException {
		return add(tatics==null?key:tatics.toKey(key, config), value , MemCacheConfig.getInstance().getConfig(config));
	}


	public boolean add(String key,Serializable value,int exp) throws QDevelopException{
		if(!QProperties.globeCache || key == null || value == null || client == null )
			return false;
		if(!isActive && !rebuild())return false;
		try {
			return client.set(key, exp, value);
		} catch (TimeoutException e) {
			throw new QDevelopException(e,"Memcache连接超时异常");
		} catch (InterruptedException e) {
			rebuild();
			throw new QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new QDevelopException(e);
		} 
	}

	public boolean add(String key,Serializable value,Date endDate) throws QDevelopException{
		if(!QProperties.globeCache || key == null || value == null || client == null )
			return false;
		if(!isActive && !rebuild())return false;
		try {
			int exp = (int)((endDate.getTime() - System.currentTimeMillis())/1000);
			if(exp<0)return false;
			return client.set(key, exp, value);
		} catch (TimeoutException e) {
			return false;
		} catch (InterruptedException e) {
			rebuild();
			throw new QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new QDevelopException(e);
		} 
	}

	@Override
	public Serializable get(String key, String config,ITacties tatics) throws QDevelopException {
		if(key==null )return null;
		String keys = tatics==null?key:tatics.toKey(key, config);
		return get(keys,MemCacheConfig.getInstance().maxWaitTimer);
	}

	public Serializable get(String key,int maxWaitTimer) throws QDevelopException {
		if(!QProperties.globeCache || key == null || client == null )return null;
		if(!isActive && !rebuild())return null;
		try {
			return client.get(key,maxWaitTimer);
		}  catch (TimeoutException e) {
			return null;
		} catch (InterruptedException e) {
			rebuild();
			throw new QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new QDevelopException(e);
		} 
	}

	@Override
	public boolean update(String key, Serializable value, String config,ITacties tatics) throws QDevelopException {
		if(!QProperties.globeCache|| key == null || value == null || client == null)return false;
		if(!isActive && !rebuild())return false;
		try {
			client.replace(tatics==null?key:tatics.toKey(key, config), MemCacheConfig.getInstance().getConfig(config), value);
			return true;
		} catch (TimeoutException e) {
			throw new  QDevelopException(e);
		} catch (InterruptedException e) {
			rebuild();
			throw new QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new QDevelopException(e);
		}
	}

	public void set(String key, Serializable value, String config,ITacties tatics) throws QDevelopException {
		if(!QProperties.globeCache|| key == null || value == null || client == null )return;
		if(!isActive && !rebuild())return;
		try {
			client.set(tatics==null?key:tatics.toKey(key, config), MemCacheConfig.getInstance().getConfig(config), value);
		} catch (TimeoutException e) {
			throw new  QDevelopException(e);
		} catch (InterruptedException e) {
			rebuild();
			throw new QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new QDevelopException(e);
		} 
	}
	public boolean casUpdate(String key, Serializable value, String config,ITacties tatics)  {
		return casUpdate(tatics==null?key:tatics.toKey(key, config),value,MemCacheConfig.getInstance().getConfig(config));
	}
	/**
	 * 带版本号的更新
	 * @param key 
	 * @param value
	 * @param timer 超时时长
	 * @return 是否更新成功
	 */
	public boolean casUpdate(String key, Serializable value, int timer)  throws QDevelopException {
		if(!QProperties.globeCache|| key == null || value == null || client == null)return false;
		if(!isActive && !rebuild())return false;
		try {
			GetsResponse<Integer> result = client.gets(key);
			if(result!=null){
				long cas = result.getCas(); 
				return client.cas(key, timer, value, cas);
			}else{
				return client.set(key, timer, value);
			}
		} catch (TimeoutException e) {
			throw new  QDevelopException(e);
		} catch (InterruptedException e) {
			rebuild();
			throw new  QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new  QDevelopException(e);
		}
	}

	public long getCasVersion(String key)  throws QDevelopException {
		if(!QProperties.globeCache|| key == null || client == null )return 0;
		if(!isActive && !rebuild())return 0;
		try {
			return client.gets(key).getCas(); 
		} catch (TimeoutException e) {
			throw new  QDevelopException(e);
		} catch (InterruptedException e) {
			rebuild();
			throw new  QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new  QDevelopException(e);
		}
	}

	/**
	 * 原子计数
	 * @param key
	 * @param delta
	 * @param initValue
	 * @return
	 * @throws QDevelopException
	 */
	public long incr(String key,long delta,long initValue) throws QDevelopException{
		if(!QProperties.globeCache|| key == null  || client == null)return 0;
		if(!isActive && !rebuild())return 0;
		try {
			return client.incr(key, delta,initValue);
		} catch (TimeoutException e) {
			throw new  QDevelopException(e);
		} catch (InterruptedException e) {
			rebuild();
			throw new  QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new  QDevelopException(e);
		}
	}

	/**
	 * 原子减数
	 * @param key
	 * @param delta
	 * @param initValue
	 * @return
	 * @throws QDevelopException
	 */
	public long dccr(String key,long delta,long initValue) throws QDevelopException{
		if(!QProperties.globeCache|| key == null  || client == null )return 0;
		if(!isActive && !rebuild())return 0;
		try {
			return client.decr(key, delta, initValue);
		} catch (TimeoutException e) {
			throw new  QDevelopException(e);
		} catch (InterruptedException e) {
			rebuild();
			throw new  QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new  QDevelopException(e);
		}
	}



	public void shutdown(){
		try {
			//			if(loopThread!=null)loopThread.shutdown();
			if(client!=null && !client.isShutdown())
				client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void remove(String key){
		if(!QProperties.globeCache || key == null || client == null)return;
		if(!isActive && !rebuild())return;
		try {
			client.delete(key);
		} catch (TimeoutException e) {
			throw new  QDevelopException(e);
		} catch (InterruptedException e) {
			rebuild();
			throw new  QDevelopException(e);
		} catch (MemcachedException e) {
			rebuild();
			throw new  QDevelopException(e);
		}
	}
	@Override
	public void remove(String key, String config,ITacties tatics) {
		remove(tatics==null?key:tatics.toKey(key, config));
	}

}
