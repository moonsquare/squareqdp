package com.qdevelop.core.sqlmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.cache.bean.TableCacheItem;
import com.qdevelop.cache.clear.TableIndexs;
import com.qdevelop.core.bean.FooterBean;
import com.qdevelop.core.formatter.DBGroupViewPaginationFormatter;
import com.qdevelop.core.formatter.LoopQueryBaseformatter;
import com.qdevelop.core.formatter.bean.FormatterConfigBean;
import com.qdevelop.core.formatter.bean.InitFormatBean;
import com.qdevelop.core.standard.IBeforeRun;
import com.qdevelop.core.standard.IPaginationFormatter;
import com.qdevelop.core.standard.IParamFormatter;
import com.qdevelop.core.standard.IResultFormatter;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QClass;
import com.qdevelop.utils.QFile;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.UtilsFactory;
import com.qdevelop.utils.files.FileFilter;
import com.qdevelop.utils.files.IQFileLoader;
import com.qdevelop.utils.files.QResoureReader;
import com.qdevelop.utils.files.QXMLUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SQLModelLoader  implements IQFileLoader{
	public static final String sql_model_config = "QDevelop_sqlConfig";
	private static SQLModelLoader _SQLModelLoader = new SQLModelLoader();
	private Document sqlModelConfigCache;
	static int size=0;
	public static SQLModelLoader getInstance(){
		return _SQLModelLoader;
	}

	private Lock _lock = new ReentrantLock();
	public Document getAllSQLModel(){
		return sqlModelConfigCache;
	}

	public SQLModelLoader() throws QDevelopException{
		if(sqlModelConfigCache==null)
			reload();
	}
	private ConcurrentHashMap<String,String> tmpCache;
	/**<TableName,Indexs>**/
	private ConcurrentHashMap<String,HashSet<String>> table2Indexs;

	public ConcurrentHashMap<String,HashSet<String>> getTable2Indexs(){
		return table2Indexs;
	}

	private ConcurrentHashMap<String,List<IResultFormatter>> _formatterBeanCache;
	private ConcurrentHashMap<String,FooterBean> _footerBeanCache;
	private ConcurrentHashMap<String,List<IBeforeRun>> _IParamFormatterCache;
	private ConcurrentHashMap<String,IPaginationFormatter> _paginationFormatter;
	private ConcurrentHashMap<String,String> tmpIncludeSqls ;
	private HashSet<String> globeExcludeArgs;
	private ConcurrentHashMap<String,String> tmpFormatterIndexs;
	private ConcurrentHashMap<String,TableCacheItem> cache_table_element;



	private Pattern tablePattern = Pattern.compile("^.+?INTO|^UPDATE| WHERE.+?$| SET.+?$| VALUE.+?$|\\(.+?\\)|^.+?FROM|`");

	@Override
	public void clear() {
	}

	public Element getElementByIndex(String index) throws QDevelopException{
		return (Element)(sqlModelConfigCache.selectSingleNode(new StringBuffer().append("/model-root/property[@index='").append(index).append("']").toString()));
	}

	public TableCacheItem getTableCacheItem(String tableName,String uniKey){
		String tableCacheKey = new StringBuffer().append(tableName).append("-").append(uniKey).toString();
		TableCacheItem tItem = cache_table_element.get(tableCacheKey);
		if(tItem!=null)return tItem;
		Element elem;
		Element tableCaches = sqlModelConfigCache.getRootElement().element("table-caches");
		if(tableCaches == null)return null;
		Iterator<Element> iter = tableCaches.elementIterator();
		while(iter.hasNext()){
			elem = iter.next();
			if(elem.attributeValue("CASETABLENAME").equalsIgnoreCase(tableName) 
					&& elem.attributeValue("CASEUNIKEY").equalsIgnoreCase(uniKey)){
				tItem = new TableCacheItem(tableCacheKey,true);
				tItem.setTableName(elem.attributeValue("tablename"));
				tItem.setUniKey(elem.attributeValue("unikey"));
				break;
			}
		}
		if(tItem == null)
			tItem = new TableCacheItem(tableCacheKey,false);
		cache_table_element.put(tableCacheKey, tItem);
		return tItem;
	}


	/**
	 * 获取执行formatter类组
	 * @param index
	 * @param isDownLoad
	 * @return
	 */
	public IResultFormatter[] getFormatterBeanByIndex(String index){
		if(index==null)return null;
		List<IResultFormatter> tmp  = _formatterBeanCache.get(index);
		if(tmp==null)return null;
		IResultFormatter[] irfs= new IResultFormatter[tmp.size()];
		for(int i=0;i<irfs.length;i++){
			irfs[i] = tmp.get(i).clone();
		}
		return irfs;
	}

	public Iterator<IBeforeRun> getBeforeFormatter(String index){
		if(index==null)return null;
		return _IParamFormatterCache.get(index)==null?null:_IParamFormatterCache.get(index).listIterator();
	}

	public String getConfigByIndex(String index){
		if(index==null)return null;
		Element sqlModel = this.getElementByIndex(index);
		if(sqlModel==null)return null;
		return sqlModel.attributeValue("cacheConfig");
	}

	public FooterBean getFooterBeanByIndex(String index){
		if(index==null)return null;
		return _footerBeanCache.get(index);
	}

	public IPaginationFormatter getPaginationFormatter(String index){
		if(index==null)return null;
		return _paginationFormatter.get(index);
	}
	QXMLUtils xmlUtils;

	@Override
	public synchronized void reload()  {
		long beginer = System.currentTimeMillis();
		_lock.lock();

		if(_footerBeanCache!=null)_footerBeanCache.clear();
		if(_formatterBeanCache!=null)_formatterBeanCache.clear();
		if(_IParamFormatterCache!=null)_IParamFormatterCache.clear();
		if(sqlModelConfigCache!=null)sqlModelConfigCache.clearContent();
		if(tmpCache!=null)tmpCache.clear();
		if(table2Indexs!=null)table2Indexs.clear();
		if(_paginationFormatter!=null)_paginationFormatter.clear();
		if(tmpIncludeSqls!=null)tmpIncludeSqls.clear();
		if(globeExcludeArgs!=null)globeExcludeArgs.clear();
		if(tmpFormatterIndexs!=null)tmpFormatterIndexs.clear();
		if(cache_table_element!=null)cache_table_element.clear();
		_footerBeanCache = new ConcurrentHashMap();
		_formatterBeanCache = new ConcurrentHashMap();
		_IParamFormatterCache = new ConcurrentHashMap();
		_paginationFormatter = new ConcurrentHashMap();
		tmpIncludeSqls = new ConcurrentHashMap();
		globeExcludeArgs = new HashSet<String>();
		tmpFormatterIndexs = new ConcurrentHashMap<String,String>();

		sqlModelConfigCache = DocumentHelper.createDocument();
		tmpCache = new ConcurrentHashMap();
		table2Indexs = new ConcurrentHashMap();
		cache_table_element = new ConcurrentHashMap<String,TableCacheItem>();

		final Element sqlModelRoot = sqlModelConfigCache.addElement("model-root");
		size=0;
		try {
			File sqlConfig = UtilsFactory.source().getResourceAsFile("sqlConfig");
			new QFile(){
				@Override
				public void disposeFileDate(String data) {

				}

				@Override
				public void disposeFile(File f) {
					try {
						//						
						if(QProperties.isDebug)System.out.println(QString.append("loading SQL Config :",f.getAbsolutePath()));
						initSQLConfig(f.getName(),new FileInputStream(f),sqlModelRoot,false);
						++size;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void disposeFileDirectory(File f) {

				}

			}.listFiles(sqlConfig, new FileFilter(".xml"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		/**增加从JAR包中读取sqlConfig文件**/
		new QResoureReader(){
			@Override
			public void desposeFile(final String jarName,final String fileName,final InputStream is) {
				++size;
				initSQLConfig(jarName+"!"+fileName,is,sqlModelRoot,true);
				if(QProperties.isDebug)System.out.println(QString.append("loading SQL Config :",jarName,"!",fileName));
			}

		}.findPath("sqlConfig", ".xml");


		/**收集所有的基于formatter的查询索引**/
		//putCasSelectToTableIndexs();

		/**更新语句级联索引创建**/
		deposeUpdateSQL(sqlModelRoot);

		try {

			xmlUtils.save(sqlModelConfigCache, UtilsFactory.getProjectPath("/QDevelop.sql.debug.xml"),"UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(List<IResultFormatter> itor :_formatterBeanCache.values()){
			for(IResultFormatter formatter : itor){
				if(formatter instanceof LoopQueryBaseformatter){
					LoopQueryBaseformatter bdf = (LoopQueryBaseformatter) formatter;
					if(bdf.getIndex()!=null){
						Element config = this.getElementByIndex(bdf.getIndex());
						if(config!=null){
							String select = config.elementText("sql");
							if(select!=null && select.length()>0){
								String args = getParamKey(select);
								if(args!=null){
									bdf.setArgs(args.split("\\|"));
									if(QProperties.isDebug)
										System.out.println("init LoopQueryBaseformatter index:"+bdf.getIndex()+" args:"+args);
								}
							}
						}
					}
				}
			}
		}

		if(QProperties.isDebug)System.out.println("SQL Config["+size+"] Load : "+(System.currentTimeMillis()-beginer)+"ms");
		tmpCache.clear();
		tmpIncludeSqls.clear();
		_lock.unlock();
		tmpFormatterIndexs.clear();
	}


	private void initSQLConfig(String fileName,InputStream is,Element sqlModelRoot,boolean isFromJar){
		try {
			if(xmlUtils==null)xmlUtils = new QXMLUtils();
			Iterator<Element> iter = xmlUtils.getDocument(is).getRootElement().elementIterator();
			while(iter.hasNext()){

				Element e = iter.next();
				if(e.getName().equals("property")){
					if(tmpCache.get(e.attributeValue("index"))!=null){
						if(isFromJar)
							QLog.getInstance().systemWarn(append("File[",fileName,"] index=",e.attributeValue("index")," Has be Rewrited By File[",tmpCache.get(e.attributeValue("index")),"]!!!!"));
						else
							QLog.getInstance().systemError(append("File[",fileName,"] index=",e.attributeValue("index")," HAS THE SAME INDEX IN File[",tmpCache.get(e.attributeValue("index")),"]!!!!"));
						continue;
					}else tmpCache.put(e.attributeValue("index"), fileName);


					Element ne = initConfigElement(e,fileName);
					if(ne!=null){
						copyBaseAttribute(ne,sqlModelRoot);
						collectParamFormatter(e);
						collectFormatter(e);
						collectFooterClass(e);
						collectPaginationClass(e);
					}
				}else if(e.getName().equals("includes")){
					Element ne =  initConfigElement(e,fileName);
					collectIncludesContent(ne);
				}else if(e.getName().equals("table-caches")){
					initTableCacheContent(e,sqlModelRoot);
				}else if(e.getName().equals("globe-args-exclude")){
					collectGlobeExcludeArgs(e);
				}
			}
		} catch (Exception e) {
			QLog.getInstance().systemError(append("SQL Model Config [",fileName,"] Loader Error!"), e);
			e.printStackTrace();
		}
	}




	private void initTableCacheContent(Element src,Element target){
		//<userinfo tablename="userinfo" unikey="id"  connnect="wjUser" columns="id,name,ctime"></userinfo>
		Element tableCache = target.element("table-caches");
		if(tableCache == null)tableCache = target.addElement("table-caches");
		Element cachePropertie;
		Iterator<Element> itor = src.elementIterator();
		while(itor.hasNext()){
			cachePropertie = itor.next();
			Element tc = tableCache.addElement(cachePropertie.getName());
			List<Attribute> attrs = cachePropertie.attributes();
			for(Attribute attr:attrs){
				tc.addAttribute(attr.getName(), attr.getValue());
			}
			if(tc.attributeValue("tablename")==null)tc.addAttribute("tablename", cachePropertie.getName());
			if(tc.attributeValue("connect")==null)tc.addAttribute("connect", QueryBean.CONNECT_DEFAULT);
			if(tc.attributeValue("unikey")==null)tc.addAttribute("unikey", "id");
			tc.addAttribute("CASETABLENAME", tc.attributeValue("tablename").toUpperCase());
			tc.addAttribute("CASEUNIKEY", tc.attributeValue("unikey").toUpperCase());
			/**增加到一级缓存索引中去**/
			TableIndexs.getInstance().addTableIndex(tc.attributeValue("connect"), tc.attributeValue("tablename"), tc.attributeValue("unikey"));
		}
	}

	public void collectGlobeExcludeArgs(Element sqlModel){
		String _args = sqlModel.getTextTrim();
		if(_args!=null){
			String[] args = _args.replaceAll(" ", "").split(",|\\|");
			for(String arg:args){
				globeExcludeArgs.add(arg);
			}
		}
	}

	/**
	 * 初始化默认配置
	 * @param sqlModel
	 * @return
	 */
	private Element initConfigElement(Element sqlModel,String fileName){
		if(sqlModel==null)return null;
		sqlModel.addAttribute("file", fileName);
		if(sqlModel.attributeValue("connect")==null)sqlModel.addAttribute("connect", QueryBean.CONNECT_DEFAULT);

		if(sqlModel.attributeValue("isLog")==null)sqlModel.addAttribute("isLog", "true");
		if(sqlModel.attributeValue("cacheConfig")==null)sqlModel.addAttribute("cacheConfig",  QDevelopConstant.CACHE_NAME_RESULTBEAN_TACTICS);

		if(sqlModel.attributeValue("sql")!=null){
			Element s = sqlModel.addElement("sql");
			s.addText(cleanSQL(sqlModel.attributeValue("sql")));
			sqlModel.remove(sqlModel.attribute("sql"));
		}

		Iterator<Element> sqls = sqlModel.elementIterator("sql");
		Boolean isSelect = null;
		//		boolean hasSql = false;
		while(sqls.hasNext()){
			Element sql = sqls.next();
			if(sql.attributeValue("include")!=null)continue;
			String sqlStr = cleanSQL(sql.getText());
			sql.setText(sqlStr);
			isSelect = sqlStr.length()>6 && sqlStr.substring(0,6).toLowerCase().equals("select")?true:false;
			if(isSelect){
				if(sqlModel.attributeValue("cacheAble")==null)sqlModel.addAttribute("cacheAble", QProperties.getInstance().getProperty("QDevelop_Config_Cache")==null?"true":QProperties.getInstance().getProperty("QDevelop_Config_Cache"));
				sqlModel.addAttribute("param", getParamKey(sqlStr));
				String[] tableNames = getSelectTableNames(sqlStr);
				if(tableNames.length>0 && sqlModel.attributeValue("authorizedTable")==null && Boolean.parseBoolean(sqlModel.attributeValue("authorized"))){
					String authorizedTable = sqlModel.attributeValue("uniTable") == null?new StringBuffer().append("qd_s_").append(tableNames[0].toLowerCase()).toString():sqlModel.attributeValue("uniTable");
					sqlModel.addAttribute("authorizedTable", authorizedTable);
				}
				/**uniTable的初始化一定要放在下面**/
				String[] mergetableNames = mergeUniTableName(sqlModel.attributeValue("uniTable"),tableNames);
				sqlModel.addAttribute("uniTable", append(mergetableNames,"|"));
				if(Boolean.parseBoolean(sqlModel.attributeValue("cacheAble")))
					pushSelectTable(sqlModel.attributeValue("index"),mergetableNames,sqlModel.attributeValue("cacheConfig"));
			}
			//			hasSql = true;
		}
		//		if(!hasSql)return null;
		sqlModel.addAttribute("isSelect", String.valueOf(isSelect));
		if((isSelect==null || isSelect) && sqlModel.attributeValue("resultClass")==null){
			sqlModel.addAttribute("resultClass", "com.qdevelop.core.bean.DBResultBean");
		}else{
			if(sqlModel.attributeValue("cacheAble")==null)sqlModel.addAttribute("cacheAble", "true");
		}
		return sqlModel;
	}

	/**
	 * 增加最基本元素拷贝
	 * @param sqlModel
	 * @param sqlModelRoot
	 */
	private void copyBaseAttribute(Element sqlModel,Element sqlModelRoot){
		if(sqlModel==null)return;
		List<Attribute> attributes = sqlModel.attributes();
		Element config = sqlModelRoot.addElement("property");
		for(Attribute attr:attributes){
			if(attr.getName().equals("safe-args")){
				config.addAttribute("safeArgs", attr.getValue());
			}else
				config.addAttribute(attr.getName(), attr.getValue());
		}

		if(sqlModel.attributeValue("sql")!=null){
			Element s = config.addElement("sql");
			s.addText(cleanSQL(sqlModel.attributeValue("sql")));
			config.remove(sqlModel.attribute("sql"));
		}

		Iterator<Element> iter = sqlModel.elementIterator("sql");
		while(iter.hasNext()){
			Element sql = iter.next();
			Element s = config.addElement("sql");
			s.addText(sql.getText());
			List<Attribute> attrs = sql.attributes();
			for(Attribute attr:attrs){
				s.addAttribute(attr.getName(), attr.getValue());
			}
		}


		/**收集formatter数据配置**/
		Element _f = sqlModel.element("formatter");

		//		if(_f != null){
		//			collectFormatterCaseIndex(_f,config.attributeValue("index"),config.attributeValue("cacheConfig"));
		//		}
		/**初始化condition模块**/
		_f = sqlModel.element("condition");
		if(_f != null){
			Element condition = config.addElement("condition");
			config.addAttribute("isCondition", "true");
			iter = sqlModel.element("condition").elementIterator();
			while(iter.hasNext()){
				Element con = iter.next();
				Element s = condition.addElement(con.getName());
				if(con.getName().equals("if") && con.attributeValue("exec")==null){
					s.addAttribute("exec", config.attributeValue("index"));
					s.addAttribute("mark", "auto");
				}
				String sqlStr = cleanSQL(con.getText());
				s.addText(sqlStr);
				s.addAttribute("param", getParamKey(sqlStr));
				List<Attribute> attrs = con.attributes();
				for(Attribute attr:attrs){
					s.addAttribute(attr.getName(), attr.getValue());
				}
			}
		}
	}

	//	private void collectFormatterCaseIndex(Element formatter,String index,String config){
	//		Iterator<Element> f = formatter.elementIterator();
	//		while(f.hasNext()){
	//			Element _f = f.next();
	//			if(_f.getName().equals("table-cache")){
	//				String tableName = _f.attributeValue("table").toUpperCase();
	//				Set<String> indexs = table2Indexs.get(tableName);
	//				if(indexs == null){
	//					indexs = new HashSet<String>();
	//				}
	//				indexs.add(index+"@"+config);
	//				table2Indexs.put(tableName, indexs);
	//			}else if(_f.getName().equals("key-value-formatter")){
	//				String casIndex = tmpFormatterIndexs.get(index);
	//				if(casIndex==null) casIndex = _f.attributeValue("cacheIndex");
	//				else casIndex += "|"+_f.attributeValue("cacheIndex");
	//				tmpFormatterIndexs.put(index, casIndex);
	//			}else if(_f.getName().equals("union-multi-formatter")){
	//				String casIndex = tmpFormatterIndexs.get(index);
	//				if(casIndex==null) casIndex = _f.attributeValue("uniIndex");
	//				else casIndex += "|"+_f.attributeValue("uniIndex");
	//				tmpFormatterIndexs.put(index, casIndex);
	//			}
	//		}
	//	}

	/**
	 * 将临时查询出来的formatter增加到clearCache索引中
	 */
	//	private void putCasSelectToTableIndexs(){
	//		//		System.out.println(formatterSelect);
	//		java.util.Iterator<Map.Entry<String, String>> itor = tmpFormatterIndexs.entrySet().iterator();
	//		while(itor.hasNext()){
	//			Map.Entry<String, String> ii = itor.next();
	//			String[] casIndexs = ii.getValue().split("\\|");
	//			for(String cIndex:casIndexs){
	//				Element sqlConfig = getElementByIndex(cIndex);
	//				if(sqlConfig==null)continue;
	//				//				System.out.println(sqlConfig.attributeValue("uniTable"));
	//				String[] tables = sqlConfig.attributeValue("uniTable").split(",");
	//				for(String tableName :tables){
	//					Set<String> indexs = table2Indexs.get(tableName);
	//					if(indexs == null){
	//						indexs = new HashSet<String>();
	//					}
	//					indexs.add(ii.getKey()+"@"+sqlConfig.attributeValue("cacheConfig"));
	//					table2Indexs.put(tableName, indexs);
	//				}
	//			}
	//		}
	//	}




	private void deposeUpdateSQL(Element sqlModelRoot){
		Iterator<Element> iter = sqlModelRoot.elementIterator("property");
		while(iter.hasNext()){
			Element property = iter.next();
			boolean isCacheAble = Boolean.parseBoolean(property.attributeValue("cacheAble"));
			if(!Boolean.parseBoolean(property.attributeValue("isSelect"))){
				boolean isFetch = false;
				Set<String> uniTables = new HashSet();
				Iterator<Element> sqls = property.elementIterator("sql");
				while(sqls.hasNext()){
					Element sql = sqls.next();
					if(sql.attributeValue("include")!=null){
						String includeContent = tmpIncludeSqls.get(sql.attributeValue("include"));
						if(includeContent!=null)
							sql.setText(cleanSQL(includeContent));
						else{
							property.remove(sql);
							QLog.getInstance().systemError("Include ["+sql.attributeValue("include")+"] not found in file ["+property.attributeValue("file")+"]");
							continue;
						}
					}
					/**收集关联的表名**/
					uniTables.add(getUpdateTableName(cleanSQL(sql.getText())));
					/**处理参数**/
					sql.addAttribute("param", getParamKey(cleanSQL(sql.getText())));
					if(Boolean.parseBoolean(sql.attributeValue("fetch")))isFetch=true;
					String repeat = sql.attributeValue("repeat");
					if(repeat!=null){
						String splitName = repeat.replaceAll("[0-9]|[a-z]|[A-Z]| ", "");
						if(splitName.length()>0)
							sql.addAttribute("repeatSlipt", splitName.substring(0,1));
					}
				}
				if(!isFetch){
					List<Element> __sqls = property.elements("sql");
					if(__sqls.size() > 0){
						Element sql = __sqls.get(0);
						sql.addAttribute("fetch", "true");
					}
				}
				property.addAttribute("uniTable", uniTables.toString().replaceAll("\\[|\\]| ", ""));
				if(isCacheAble)property.addAttribute("clearCache", getClearIndexByTableName(uniTables.toArray(new String[]{})));
			}
		}
	}

	/**
	 * 收集引入配置
	 * @param includes
	 */
	private void collectIncludesContent(Element includes){
		if(includes==null)return;
		Element include ;
		Iterator<Element> iter = includes.elementIterator();
		while(iter.hasNext()){
			include = iter.next();
			String includeSql =  cleanSQL(include.getText());
			if(includeSql!=null && !includeSql.equals(""))
				tmpIncludeSqls.put(include.getName(),includeSql);
		}
	}

	/**
	 * 收集配置之前的执行参数格式化的类
	 * @param sqlModel
	 */
	private void collectParamFormatter(Element sqlModel){
		/**增加beforeRun初始化请求参数方法*/
		Element formatQuery = sqlModel.element("param-formatter");
		if(formatQuery!=null){
			List<IBeforeRun> tmpParamFormatter = _IParamFormatterCache.get(sqlModel.attributeValue("index"));  
			if(tmpParamFormatter==null)tmpParamFormatter = new ArrayList();
			Iterator<Element> paramFormat = formatQuery.elementIterator();
			while(paramFormat.hasNext()){
				Element f = paramFormat.next();
				if(f.getName().equals("class"))tmpParamFormatter.add((IBeforeRun)QClass.getInstanceClass(f.getTextTrim()));
				else{
					FormatterConfigBean fcb = ConfigFormatterLoader.getInstance().getParamFormatterBean(f.getName());
					if(fcb!=null){
						String[] params = new String[fcb.getParams().length];
						IParamFormatter rf = (IParamFormatter)QClass.getInstanceClass(fcb.getFormatterClass());
						String key;
						for(int i=0;i<params.length;i++){
							key = fcb.getParams()[i];
							if(key.indexOf("^")==-1)
								params[i] = f.attributeValue(key);
							else{
								params[i] = f.attributeValue(key.substring(0, key.indexOf("^")))==null?
										f.attributeValue(key.substring( key.indexOf("^")+1)):
											f.attributeValue(key.substring(0, key.indexOf("^")));
							}
						}
						rf.init(params);
						tmpParamFormatter.add(rf);
					}
				}

			}
			_IParamFormatterCache.put(sqlModel.attributeValue("index"), tmpParamFormatter);
		}

		if(sqlModel.attributeValue("beforeRun")!=null){
			List<IBeforeRun> tmpParamFormatter = _IParamFormatterCache.get(sqlModel.attributeValue("index"));  
			if(tmpParamFormatter==null)tmpParamFormatter = new ArrayList();
			tmpParamFormatter.add((IBeforeRun)QClass.getInstanceClass(sqlModel.attributeValue("beforeRun")));
			_IParamFormatterCache.put(sqlModel.attributeValue("index"), tmpParamFormatter);
		}
	}


	/**
	 * 收集对结果集格式化的自定类
	 * @param sqlModel
	 */
	private void collectFormatter(Element sqlModel){
		Element formatter = sqlModel.element("formatter");
		if(formatter!=null&&formatter.hasContent()){
			List<IResultFormatter> tmpRF = _formatterBeanCache.get(sqlModel.attributeValue("index"));
			if(tmpRF==null)tmpRF = new ArrayList<IResultFormatter>();
			Iterator<Element> selfFormatter = formatter.elementIterator();
			while(selfFormatter.hasNext()){
				Element ff = selfFormatter.next();
				FormatterConfigBean fcb = ConfigFormatterLoader.getInstance().getFormatterConfigBean(ff.getName());
				if(fcb!=null){
					IResultFormatter rf = (IResultFormatter)QClass.getInstanceClass(fcb.getFormatterClass());
					rf.init(new InitFormatBean(ff,fcb.getParams()));
					tmpRF.add(rf);
				}
			}
			_formatterBeanCache.put(sqlModel.attributeValue("index"), tmpRF);

		}
	}

	/**
	 * 收集针对结果数统计类
	 * @param sqlModel
	 */
	private void collectFooterClass(Element sqlModel){
		Element footer = sqlModel.element("footer");
		if(footer!=null&&footer.hasContent()){
			_footerBeanCache.put(sqlModel.attributeValue("index"), new FooterBean(footer));
		}
	}

	/**
	 * 收集多组分页请求
	 * @param sqlModel
	 */
	private void collectPaginationClass(Element sqlModel){
		Element groupViewPagination = sqlModel.element("group-view-pagination");
		if(groupViewPagination!=null){
			String paginationSql = cleanSQL(groupViewPagination.elementText("pagination-sql"));
			if(paginationSql!=null && paginationSql.trim().length()>0){
				_paginationFormatter.put(sqlModel.attributeValue("index"),
						new DBGroupViewPaginationFormatter(paginationSql,
								groupViewPagination.elementText("order-param"),
								groupViewPagination.elementText("group-field"),
								sqlModel.attributeValue("cacheConfig"),
								sqlModel.attributeValue("connect"),
								getParamKey(paginationSql).split("\\|")
								));
			}
		}
	}

	public String getClearIndexByTableName(String ... tableNames){
		Set tmp = new HashSet();
		Set<String> indexs;
		for(String table:tableNames){
			indexs = table2Indexs.get(table.toUpperCase());
			if(indexs!=null){
				for(String index : indexs){
					tmp.add(index);
				}
			}
		}
		if(tmp.size()==0)return null;
		return append(tmp,"|");
	}

	public HashSet<String> getClearIndexByTableName(String tableName){
		return table2Indexs.get(tableName.toUpperCase());
	}

	public String[] getClearIndexBySQL(String ... sqls){
		Set<String> tmp = new HashSet<String>();
		Set<String> indexs;
		for(String sql:sqls){
			indexs = table2Indexs.get(getUpdateTableName(sql.toUpperCase()));
			if(indexs!=null){
				for(String index : indexs){
					tmp.add(index);
				}
			}
		}
		if(tmp.size()==0)return null;
		return tmp.toArray(new String[]{});
	}

	/**
	 * 根据sql获取表名
	 * @param sqls
	 * @return
	 */
	public String[] getTableNameBySQL(String ... sqls){
		Set<String> tmp = new HashSet<String>();
		for(String sql:sqls){
			tmp.add(getUpdateTableName(sql.toUpperCase()));
		}
		if(tmp.size()==0)return null;
		return tmp.toArray(new String[]{});
	}



	//====================================================================================================================================================================

	//	private final static Pattern tablePattern = Pattern.compile("^.+?INTO|^UPDATE|WHERE.+?$|SET.+?$|VALUE.+?$|\\(.+?\\)|^.+?FROM");

	@SuppressWarnings("unused")
	private String getUpdateTables(Iterator sqls, String uniTable){
		Element e_sql;
		String sql;
		Set<String> tables = new HashSet();
		while(sqls.hasNext()){
			e_sql = (Element)sqls.next();
			sql = e_sql.getText().toUpperCase();
			tables.add(sql.replaceAll("^.+?INTO|^UPDATE|LOW_PRIORITY| WHERE.+?$| SET.+?$| VALUE.+?$|\\(.+?\\)|^.+?FROM|`", "").trim());
		}
		if(uniTable!=null){
			String[] tmp = uniTable.split(";|\\|");
			for(String tab:tmp){
				tables.add(tab.toUpperCase());
			}
		}
		return append(tables,"|").replaceAll("`", "");
	}

	private String pushSelectTable(String index,String[] tableNames,String cacheConfig){
		for(int i=0;i<tableNames.length;i++){
			String tableName = tableNames[i].replaceAll("`", "");
			HashSet<String> tmpIndexs = table2Indexs.get(tableName);
			if(tmpIndexs==null){
				tmpIndexs = new HashSet();
			}
			tmpIndexs.add(append(index,"@",cacheConfig).replaceAll("`", ""));
			table2Indexs.put(tableName, tmpIndexs);
		}
		return append(tableNames,"|");
	} 

	public  static String[] getSelectTableNames(String sql){
		String[] tbs = sql.replaceAll("`", "").toUpperCase().split("FROM | JOIN ");
		Set<String> tables = new HashSet();
		for(String tb:tbs){
			tb = tb.replaceAll(" WHERE .+| ORDER.+| GROUP.+|\\(.+", "").trim().replaceAll(" .+,$|\\$\\[.+?\\]", "");
			if(tb.length()>1&&!tb.startsWith("SELECT")&&!tb.startsWith("(")){
				if(tb.indexOf(",")==-1){
					tables.add(tb.replaceAll(" .+?$|\\)", ""));
				}else{
					for(String ss : tb.split(",")){
						tables.add(ss.trim().replaceAll(" .+?$|\\)|`", ""));
					}
				}
			}
		}
		return tables.toArray(new String[]{});
	}

	/**
	 * 
	 * @param configTableName
	 * @param tableNames
	 * @return
	 */
	private String[] mergeUniTableName(String configTableName,String[] tableNames){
		HashSet<String> tableName = new HashSet<String>();
		String[] tmp = configTableName == null?null:configTableName.split("\\|");
		for(int i=0;i<tableNames.length;i++){
			tableName.add(tableNames[i]);
		}
		if(configTableName!=null){
			for(String tb : tmp){
				tableName.add(tb);
			}
		}

		return tableName.toArray(new String[]{});
	}

	//	Pattern tablePattern = Pattern.compile("^.+?INTO|^UPDATE| WHERE.+?$| SET.+?$| VALUE.+?$|\\(.+?\\)|^.+?FROM|`");
	public String getUpdateTableName(String ... sqls){
		if(sqls.length==1)return tablePattern.matcher(sqls[0].toUpperCase()).replaceAll("").trim();
		Set<String> tables = new HashSet();
		for(String sql:sqls){
			tables.add(tablePattern.matcher(sql.toUpperCase()).replaceAll("").trim());
		}
		return append(tables,"|");
	}

	/**
	 * 清理不需要的参数，清理SQL注入参数等方法
	 * @param param
	 * @param safe_args
	 */
	public void cleanSQLArgs(Map<String,Object> param){
		java.util.Iterator<String> itor = globeExcludeArgs.iterator();
		while(itor.hasNext()){
			param.remove(itor.next());
		}
		//		java.util.Iterator<Map.Entry<String, Object>> pitor = param.entrySet().iterator();
		//		while(pitor.hasNext()){
		//			Map.Entry<String, Object> entry = pitor.next();
		//			if(safe_args!=null &&(safe_args[0].equals("*") || ArrayUtils.contains(safe_args, entry.getKey()))){
		//				continue;
		//			}
		//			String value = String.valueOf(entry.getValue()).toLowerCase();
		//			if(_SQL_ATTACK_REG.matcher(value).find()){
		//				throw new QDevelopException(QString.append("您的请求含有可能造成对系统的攻击[",entry.getKey(),"=",value,"]，被服务器拦截! 如有疑问，请联系系统管理员。"));
		//			}
		//		}
	}



	/**
	 * 整理SQL语句格式；将使用者写的sql格式做一个统一
	 * @param sql
	 * @return
	 * @throws QDevelopException
	 */
	public String cleanSQL(String sql) throws QDevelopException{
		if(sql == null)throw new QDevelopException("sql is null");
		return sql.replaceAll("\n|\t", " ").replace(")values(", ") values (").trim().replaceAll(" +", " ").replace("$[_autoSearch]", QDevelopConstant.AUTO_SEARCH_MARK);
	}
	private Pattern paramClear1 = Pattern.compile("\\@\\[.+?\\]|SEQID\\.NEXT\\[.+?\\]");
	private Pattern paramClear2 = Pattern.compile("\\].+?\\$\\[");
	private Pattern paramClear3 = Pattern.compile("^.+?\\$\\[|\\].+?$|\\]");

	public String getParamKey(String sql){
		if(sql.indexOf("$[")==-1)return null;
		return paramClear3.matcher(paramClear2.matcher(paramClear1.matcher(sql).replaceAll("")).replaceAll("|")).replaceAll("");
		//		return sql.replaceAll("\\@\\[.+?\\]|SEQID\\.NEXT\\[.+?\\]", "").replaceAll("\\].+?\\$\\[", "|").replaceAll("^.+?\\$\\[|\\].+?$|\\]", "");
	}

	private String append(Object ... s){
		StringBuffer sb = new StringBuffer();
		for(Object _s:s)sb.append(_s);
				return sb.toString();
	}
	private String append(String[] s,String split){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<s.length;i++){
			if(i>0)sb.append(split);
			sb.append(s[i]);
		}
		return sb.toString();
	}
	private String append(Set s,String split){
		StringBuffer sb = new StringBuffer();
		Iterator it = s.iterator();
		for(;it.hasNext();){
			sb.append(split).append(it.next());
		}
		return sb.length()>0?sb.toString().substring(1):"";

	}
}
class SQLConfig{
	InputStream is;
	String fileName;
	public SQLConfig(InputStream is,String fileName){
		this.is = is;
		this.fileName = fileName;
	}
}
