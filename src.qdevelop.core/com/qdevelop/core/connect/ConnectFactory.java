package com.qdevelop.core.connect;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.cache.clear.TableIndexs;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QCache;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.utils.cache.MapCache;
import com.qdevelop.utils.files.QResoureReader;
import com.qdevelop.utils.files.QXMLUtils;

public class ConnectFactory {
	private static  String CONNECT_FACTORY_CACHE_KEY = "com.qdevelop.core.connect.ConnectFactory";
	private static ConnectFactory _ConnectFactory = new ConnectFactory();
	private Lock lock = new ReentrantLock();
	private static byte[] locker = new byte[0];

	public static IConnect getInstance() throws QDevelopException{
		return getInstance(QueryBean.CONNECT_DEFAULT);
	}

	private static  Document databaseConfig;

	@SuppressWarnings("unchecked")
	private  void copyConfigNode(Document config,Element root,QXMLUtils xmlUtils){
		Iterator<Element> elem = config.getRootElement().elementIterator();
		while(elem.hasNext()){
			Element e = elem.next();
			if(root.element(e.attributeValue("index"))==null){
				xmlUtils.copyAdd(root, e);
			}else{
				QLog.getInstance().systemWarn(e.asXML());
			}
		}
	}

	public static void initAllConnect(){
		_ConnectFactory.loadConfig();
		@SuppressWarnings("unchecked")
		Iterator<Element> iter = databaseConfig.getRootElement().elementIterator("connect");
		while(iter.hasNext()){
			Element config = iter.next();
			_ConnectFactory.init(config.attributeValue("index"));
		}

	}
	private void loadConfig(){
		if(databaseConfig!=null)return;
		try {
			databaseConfig= DocumentHelper.createDocument();
			final Element root = databaseConfig.addElement("database-config");
			final QXMLUtils xmlUtils = new QXMLUtils();
			File configFile = UtilsFactory.source().getResourceAsFile("databaseConfig.xml");
			System.out.println(configFile.getAbsolutePath());
			copyConfigNode(xmlUtils.getDocument(configFile,"UTF-8"),root,xmlUtils);
			new QResoureReader(){
				@Override
				public void desposeFile(final String jarName,final String fileName,final InputStream is) {
					System.out.println("load databaseConfig from jar "+jarName+"!"+fileName);
					try {
						copyConfigNode(xmlUtils.getDocument(is, "utf-8"),root,xmlUtils);
					} catch (DocumentException e) {
						e.printStackTrace();
					}
				}

			}.findFiles("databaseConfig.xml");
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private synchronized  IConnect init(final String config) throws QDevelopException{
		lock.lock();
		IConnect connection=null;
		try {
			loadConfig();
			connection = null;
			Element configs = (Element) databaseConfig.selectSingleNode(append("/database-config/connect[@index='",config,"']"));
			if(configs==null && config.endsWith("_R"))configs = (Element) databaseConfig.selectSingleNode(append("/database-config/connect[@index='",config.substring(0,config.length()-2),"']"));
			if(configs==null)throw new QDevelopException("数据库配置["+config+"]不存在！请检查src/databaseConfig.xml");
			connection=new DBCPConnect(configs);
			connection.printInfo();
			final String database = connection.getDataBase();
			final Connection conn = connection.getConnection();
			new Thread(){
				public void run(){
					TableIndexs.getInstance().initByConnect(config, database,conn);
					try {
						if(conn!=null&&!conn.isClosed())conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.start();
			QCache.mapCache().setCache(config,connection,CONNECT_FACTORY_CACHE_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		lock.unlock();
		return  connection;
	}

	public static Element getElementByIndex(String config){
		_ConnectFactory.loadConfig();
		return (Element) databaseConfig.selectSingleNode(append("/database-config/connect[@index='",config,"']"));
	}

	public static  IConnect getInstance(String config) throws QDevelopException{
		IConnect connection=null;
		try {
			if(config==null)config = QueryBean.CONNECT_DEFAULT;
			connection = (IConnect)QCache.mapCache().getCache(config,CONNECT_FACTORY_CACHE_KEY);
			if(connection==null && config.endsWith("_R")){//只读配置不存在时，转向主库
				connection = (IConnect)QCache.mapCache().getCache(config.substring(0,config.length()-2),CONNECT_FACTORY_CACHE_KEY);
			}
			if(connection==null){
				synchronized(locker){
					connection = _ConnectFactory.init(config);
				}
			}
			if(connection == null){
				throw new QDevelopException("数据库连接["+config+"]获取异常,也许还没有加入配置吧？？");
			}
			return connection;
		} catch (QDevelopException e) {
			e.printStackTrace();
			QLog.getInstance().systemError(append("数据库连接获取异常\t",config," = [",(connection == null?"":connection.monitor()),"]"));
			throw new QDevelopException("数据库连接["+config+"]获取异常");
		}
	}

	@SuppressWarnings("unchecked")
	public static String[] watchConnect(){
		Map<String,IConnect> allDBCP = (Map<String,IConnect>)MapCache.getInstance().getCacheByConfig(CONNECT_FACTORY_CACHE_KEY);
		if(allDBCP==null||allDBCP.size()==0)return new String[]{};
		Iterator<Map.Entry<String, IConnect>> itor = allDBCP.entrySet().iterator();
		String[] str = new String[allDBCP.size()];
		int i = 0;
		while(itor.hasNext()){
			Entry<String, IConnect> entry = itor.next();
			IConnect value = entry.getValue();
			str[i++] = value.monitor()+"|"+entry.getKey();
		}
		return str;
	}

	@SuppressWarnings("unchecked")
	public static void shutdown(){
		Map<String,IConnect> allDBCP = (Map<String,IConnect>)MapCache.getInstance().getCacheByConfig(CONNECT_FACTORY_CACHE_KEY);
		if(allDBCP==null||allDBCP.size()==0)return;
		Iterator<Map.Entry<String, IConnect>> itor = allDBCP.entrySet().iterator();
		while(itor.hasNext()){
			Entry<String, IConnect> entry = itor.next();
			entry.getValue().shutdown();
		}
		QCache.mapCache().removeAllCache(CONNECT_FACTORY_CACHE_KEY);

	}

	@SuppressWarnings("unchecked")
	public static void clear(){
		Map<String,IConnect> tmp = MapCache.getInstance().getCacheByConfig(CONNECT_FACTORY_CACHE_KEY);
		if(tmp!=null){
			java.util.Iterator<IConnect> itor = tmp.values().iterator();
			while(itor.hasNext()){
				itor.next().shutdown();
			}
		}
		QCache.mapCache().removeAllCache(CONNECT_FACTORY_CACHE_KEY);
		databaseConfig=null;
	}

	public static String getDatabase(String databaseConfig) throws QDevelopException{
		return getInstance(databaseConfig).getDataBase();
	}

	private static String append(Object ... s){
		StringBuffer sb = new StringBuffer();
		for(Object _s:s)sb.append(_s);
				return sb.toString();
	}

}

