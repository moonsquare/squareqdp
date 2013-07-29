package com.qdevelop.core.sqlmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Element;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.core.bean.DBQueryBean;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.utils.TableSequence;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;

@SuppressWarnings("unchecked")
public class SQLModelParser implements Cloneable{
	private static SQLModelParser _SQLModelFactory = new SQLModelParser();
	public static SQLModelParser getInstance(){
		/***保证线程安全**/
		return _SQLModelFactory.clone();
	}

	public SQLModelParser clone(){
		try{
			return (SQLModelParser)super.clone();
		}catch(Exception e){
			QLog.getInstance().systemError("SQLModelParser Clone Error!", e);
			return new SQLModelParser();
		}
	}

	//	public static void main(String[] args) {
	//		Map queryParam = new HashMap();
	//		queryParam.put("index", "ft_couseware_type!update");
	//		queryParam.put("name", "janson");
	//		queryParam.put("id", "11");
	//		System.out.println(SQLModelParser.getInstance().getQueryBean(queryParam, null, null).getSql());
	//	}

	public static final String[] filterParam = new String[]{"index","page","maxNum","order","rows","param","allCount"}; 
	
	public static final Pattern isComplexValue = Pattern.compile("%|&|>|<|!|\\|");
	
	
	/**
	 * 根据index值 获取配置信息
	 * @param index
	 * @param attr
	 * @return
	 */
	public String getAttrbuteByIndex(String index,String attr){
		Element sqlModel = SQLModelLoader.getInstance().getElementByIndex(index);
		if(sqlModel==null)return null;
		return sqlModel.attributeValue(attr);
	}
	

	/**
	 * 获取数据库请求参数Bean
	 * @param queryParam
	 * @param cacheKey
	 * @param string[] selfRoleId  数组，[0]用户的全部权限ID [1]用户的私有权限ID [2]登录用户名
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public DBQueryBean getQueryBean(Map<String,String> queryParam,String cacheKey,String[] selfRoleId) throws QDevelopException{
		if(isMultiIndexQuery(queryParam))return getQuerySQLByMultiIndex(queryParam,cacheKey,selfRoleId);
		try {
			Element sqlModel = SQLModelLoader.getInstance().getElementByIndex(queryParam.get("index"));
			if(sqlModel==null) {
				QLog.getInstance().systemError(append("SQL Model Config [",queryParam.get("index"),"] Not Found!!"));
				throw new QDevelopException(append("SQL Model Config [",queryParam.get("index"),"] Not Found!!"));
			}
			for(String key : filterParam){
				String value = queryParam.get(key);
				if(value!=null)checkHackValue(key,value,null);
			}
			DBQueryBean queryBean = new DBQueryBean(queryParam);

			boolean isDebug = parseBoolean(sqlModel.attributeValue("isDebug")) || queryBean.isDebug;
			StringBuffer debugInfo = null;
			if(isDebug)debugInfo = new StringBuffer();
			if(isDebug){
				debugInfo.append("===============SQL MODEL DEBUG PARSE INFO=======================\r\n")
				.append("Config XML:").append(sqlModel.asXML())
				.append("\r\n1、Query Param:\t")
				.append(queryBean.getQueryContent())
				.append("\r\n2、CacheKey:\t").append(cacheKey)
				.append("\r\n3、User RID:\t").append((selfRoleId==null?"":append("ALL:",selfRoleId[0],"  ,  SELF:",selfRoleId[1])));
			}

			queryBean.isSelect = parseBoolean(sqlModel.attributeValue("isSelect"));

			/**新增自定义结果请求Bean**/
			if(queryBean.isSelect)queryBean.resultBean = sqlModel.attributeValue("resultClass");
			else queryBean.depends = sqlModel.attributeValue("depends");

			queryBean.setAfterRun(sqlModel.attributeValue("afterRun"));
			queryBean.setBeforeRun(sqlModel.attributeValue("beforeRun"));

			if(sqlModel.attributeValue("pagination")!=null){//从配置中初始化分页信息
				queryBean.setNowPage(1);
				if(sqlModel.attributeValue("pagination").indexOf(":")==-1)
					queryBean.setMaxNum(parseInt(sqlModel.attributeValue("pagination")));
				else 
					queryBean.setMaxNum(parseInt(sqlModel.attributeValue("pagination").split(":")[1]));
			}

			if(selfRoleId!=null && selfRoleId.length>2)queryBean.user = selfRoleId[2];


			if(isDebug)debugInfo.append("\r\n5、ResultCache:\t").append(queryBean.isCacheAble()).append("\t").append(queryBean.isCacheAble()?append("CacheConfig:",queryBean.getCacheConfig()):"");
			queryBean.uniTable = sqlModel.attributeValue("uniTable");
			queryBean.targetTable = sqlModel.attributeValue("targetTable")==null?sqlModel.attributeValue("uniTable"):sqlModel.attributeValue("targetTable");

			if(parseBoolean(sqlModel.attributeValue("authorized"))){//权限相关
				queryBean.isAuthorize = true;
				queryBean.authorizedTable = sqlModel.attributeValue("authorizedTable");
				queryBean.uniKey = sqlModel.attributeValue("uniKey");
				if(selfRoleId!=null){
					if(queryBean.isSelect)
						queryBean.userRoleId = selfRoleId[0];
					else
						queryBean.userRoleId = selfRoleId[1];
				}
			}else{
				if(cacheKey!=null)cacheKey = cacheKey.replaceAll("\\^.+\\^", "");
			}
			boolean isCache = parseBoolean(sqlModel.attributeValue("cacheAble"));
			if(isCache){
				queryBean.setCacheConfig(sqlModel.attributeValue("cacheConfig"));
				queryBean.setCacheKey(cacheKey);
			}
			queryBean.setCacheAble(isCache);

			queryBean.isLog = parseBoolean(sqlModel.attributeValue("isLog"));

			if(isDebug)debugInfo.append("\r\n6、isAuthorized:\t").append(queryBean.isAuthorize()).append("\t").append(queryBean.isAuthorize()?append("UniTable:",queryBean.getUniTable()):"").append("\t").append(queryBean.isAuthorize()?append("UniKey:",queryBean.getUniKey()):"");

			queryBean.setClearCache(sqlModel.attributeValue("clearCache"));

			if(!queryBean.isSelect&&isDebug)
				debugInfo.append("\r\n7、Clear Cache:\t").append(queryBean.getClearCache());

			/**配置数据库链接相关信息**/
			queryBean.connect = sqlModel.attributeValue("connect")==null?QueryBean.CONNECT_DEFAULT:sqlModel.attributeValue("connect");
			queryBean.database = ConnectFactory.getInstance(queryBean.connect).getDataBase();

			String[] param = sqlModel.attributeValue("param")==null?new String[]{}:sqlModel.attributeValue("param").split("\\|");
			HashMap parserData = (HashMap)queryBean.getQueryData();
			String[] safe_args = sqlModel.attributeValue("safeArgs")==null?null:sqlModel.attributeValue("safeArgs").split("\\|");
			SQLModelLoader.getInstance().cleanSQLArgs(parserData);
			if(queryBean.isSelect){
				if(sqlModel.attributeValue("includeArgs")!=null){
					String[] args = sqlModel.attributeValue("includeArgs").split(",|\\|");
					java.util.Iterator<String> itor = ((HashMap)parserData.clone()).keySet().iterator();
					while(itor.hasNext()){
						String key = itor.next();
						if(!ArrayUtils.contains(args, key)){
							parserData.remove(key);
						}
					}
				}
				if(sqlModel.attributeValue("excludeArgs")!=null){
					String[] args = sqlModel.attributeValue("excludeArgs").split(",|\\|");
					for(String arg:args){
						parserData.remove(arg);
					}
				}
				String sql = parserQuerySQL(sqlModel.elementText("sql"),param,parserData,queryBean.isSelect,safe_args);	
				if(sql.indexOf("$[")>-1)throw new QDevelopException(append("SQL Model Config [",queryBean.getSqlIndex(),"] Parse Eorror! Param is:",parserData.toString()));
				queryBean.setQuery(parseSeqID(sql,queryBean.connect));
			}else{
				Iterator sqls = sqlModel.elementIterator("sql");
				Element _s;String repeat,repeatSplit;boolean isFullParamOnly;
				ArrayList<Integer> fetchIndex = new ArrayList<Integer>();
				ArrayList<Integer> judgeIndex = null;
				int fIndex = 0;
				while(sqls.hasNext()){
					_s = (Element)sqls.next();
					repeat = _s.attributeValue("repeat");
					String sql = parseSeqID(_s.getText(),queryBean.connect);
					boolean isFetch = _s.attributeValue("fetch")!=null && Boolean.parseBoolean(_s.attributeValue("fetch"));
					boolean isjudge = _s.attributeValue("judge")!=null && Boolean.parseBoolean(_s.attributeValue("judge"));
					isFullParamOnly = Boolean.parseBoolean(_s.attributeValue("fullParamOnly"));
					param = _s.attributeValue("param")==null?param:_s.attributeValue("param").split("\\|");
					String monitor = _s.attributeValue("monitor");

					/**根据参数监控sql是否需要不执行**/
					if(monitor != null && (queryBean.isNullQueryData(monitor) || queryBean.getQueryData(monitor).equals("false") || queryBean.getQueryData(monitor).equals("0"))){
						QLog.getInstance().systemWarn(append("SQL Config[",queryBean.sqlIndex,"] Has no monitor param[",monitor,"]:",queryParam.toString()));
						continue;
					}

					if(repeat == null || queryBean.isNullQueryData(repeat)){
						if(isFullParamOnly&&!isCheckedByFullParam(param,parserData)){
							QLog.getInstance().systemWarn(append("SQL Config[",queryBean.sqlIndex,"] Has no full param:",queryParam.toString()));
							continue;
						}
						queryBean.addSql(parserQuerySQL(sql,param,(parserData),queryBean.isSelect,safe_args));
						if(isFetch)fetchIndex.add(fIndex);
						if(isjudge){
							if(judgeIndex==null)judgeIndex = new ArrayList<Integer>();
							judgeIndex.add(fIndex);
						}
						fIndex++;
					}else{
						//					if(queryBean.isNullQueryData(repeat)){
						//						QLog.getInstance().systemError(append("SQL Config[",queryBean.sqlIndex,"] Has no repeat param:",queryParam.toString()));
						//						continue;
						//					}
						repeatSplit = getSplitStr(_s.attributeValue("repeatSlipt"));
						String[] reParam = repeat.split(repeatSplit);

						String[] reValue = queryBean.getQueryData(repeat).split("\\^");
						Map<String,String> tmpParam = new HashMap((parserData));
						tmpParam.remove(repeat);
						String[] params = queryBean.getComplexQueryParam();
						int paramsIndex = 0;
						for(String val:reValue){
							String[] vvs = val.split(repeatSplit);
							int len = vvs.length;
							for(int i=0;i<reParam.length;i++){
								if(i<len)
									tmpParam.put(reParam[i], vvs[i]);
							}
							if(params!=null && paramsIndex < params.length && params[paramsIndex]!=null)
								tmpParam.put("param", params[paramsIndex++].replaceAll(",", ";"));
							else tmpParam.remove("param");
							if(isFullParamOnly&&!isCheckedByFullParam(param,tmpParam)){
								QLog.getInstance().systemWarn(append("SQL Config[",queryBean.sqlIndex,"] Has no full param:",queryParam.toString()));
								continue;
							}
							queryBean.addSql(parserQuerySQL(sql,param,tmpParam,queryBean.isSelect,safe_args));
							if(isFetch)fetchIndex.add(fIndex);
							if(isjudge){
								if(judgeIndex==null)judgeIndex = new ArrayList<Integer>();
								judgeIndex.add(fIndex);
							}
							fIndex++;
						}
						tmpParam.clear();
						tmpParam = null;
					}
				}
				
				queryBean.fetchIndex = fetchIndex.toArray(new Integer[]{});
				fetchIndex.clear();
				if(judgeIndex!=null){
					queryBean.judgeIndex = judgeIndex.toArray(new Integer[]{});
					judgeIndex.clear();
				}
				
				queryBean.updateHook = sqlModel.attributeValue("updateHook");

				queryBean.isAsync = sqlModel.attributeValue("isAsync") == null ? false : Boolean.parseBoolean(sqlModel.attributeValue("isAsync"));

				queryBean.isCondition = sqlModel.attributeValue("isCondition") == null ? false : Boolean.parseBoolean(sqlModel.attributeValue("isCondition"));
			}
			if(isDebug){
				debugInfo.append("\r\n7、After Run:\t").append(queryBean.afterRun);
				debugInfo.append("\r\n==================SQL MODEL DEBUG END===========================");
				//			System.out.println(debugInfo);
				QLog.getInstance().systemDebugger(debugInfo);
			}

			//		queryParam.clear();
			//		queryParam = null;
			return queryBean;
		} catch (QDevelopException e) {
			QLog.getInstance().systemError(e.getMessage());
			throw e;
		}
	}



	private boolean isCheckedByFullParam(String[] param,Map<String, String> tmpParam){
		for(String p:param){
			if(tmpParam.get(p)==null)return false;
		}
		return true;
	}

	private String getSplitStr(String _split){
		if(_split == null) return "\\^";
		if(_split.equals("*")||_split.equals("!")||_split.equals("+")||_split.equals("|")||_split.equals("-")){
			return append("\\",_split);
		}
		return _split;
	}
	/**
	 * 兼容原先Multi SQL Query 模式
	 * @param queryParam
	 * @return
	 */
	private boolean isMultiIndexQuery(Map<String,String> queryParam){
		if(queryParam.get("index").indexOf(";")>-1||parseBoolean(queryParam.get("isMultiQuery"))){
			queryParam.remove("isMultiQuery");
			return true;
		} 
		return false;
	}
	private DBQueryBean getQuerySQLByMultiIndex(Map<String,String> queryParam,String cacheKey,String[] selfRoleId) throws QDevelopException{
		DBQueryBean queryBean = new DBQueryBean();
		String index = queryParam.get("index");
		String[] params = queryParam.get("param")==null?new String[]{""}:queryParam.get("param").replaceAll("\\[|\\]", "").split(";");
		Element sqlModel = null;
		queryBean.setConnect((sqlModel==null||sqlModel.attributeValue("connect")==null)?QueryBean.CONNECT_DEFAULT:sqlModel.attributeValue("connect"));
		queryBean.setDatabase(ConnectFactory.getInstance(queryBean.connect).getDataBase());
		@SuppressWarnings("null")
		String[] safe_args = sqlModel.attributeValue("safe-args")==null ? null : sqlModel.attributeValue("safe-args").split("\\|");

		if(index.indexOf(";")>-1){
			String[] idxs = index.split(";");
			for(int i=0;i<idxs.length;i++){
				queryParam.put("param", i<params.length?params[i].replaceAll(",", ";"):null);
				sqlModel = SQLModelLoader.getInstance().getElementByIndex(idxs[i]);
				if(sqlModel==null) {
					QLog.getInstance().systemError(append("SQL Model Config [",idxs[i],"] Not Found!!"));
					throw new QDevelopException(append("SQL Model Config [",idxs[i],"] Not Found!!"));
				}
				String[] param = sqlModel.attributeValue("param")==null?new String[]{}:sqlModel.attributeValue("param").split("\\|");
				String sql = parserQuerySQL(sqlModel.elementText("sql"),
						param,
						queryParam,queryBean.isSelect,safe_args);	
				if(sql.indexOf("$[")>-1)throw new QDevelopException(append("SQL Model Config [",idxs[i],"] Parse Eorror! Param is:",queryParam.toString()));
				queryBean.addSql(parseSeqID(sql,queryBean.connect));
			}
		}else{
			sqlModel = SQLModelLoader.getInstance().getElementByIndex(index);
			if(sqlModel==null) {
				QLog.getInstance().systemError(append("SQL Model Config [",index,"] Not Found!!"));
				throw new QDevelopException(append("SQL Model Config [",index,"] Not Found!!"));
			}
			String sqlModelStr = parseSeqID(sqlModel.elementText("sql"),queryBean.connect);
			String[] param = sqlModel.attributeValue("param")==null?new String[]{}:sqlModel.attributeValue("param").split("\\|");
			for(String p:params){
				queryParam.put("param", p.replaceAll(",", ";"));
				String sql = parserQuerySQL(sqlModelStr,
						param,
						queryParam,queryBean.isSelect,safe_args);	
				if(sql.indexOf("$[")>-1)throw new QDevelopException(append("SQL Model Config [",index,"] Parse Eorror! Param is:",queryParam.toString()));
				queryBean.addSql(sql);
			}
		}
		StringBuffer sb = new StringBuffer();
		String[] indexs = SQLModelLoader.getInstance().getClearIndexBySQL(queryBean.getSqls()==null?new String[]{queryBean.getSql()}:queryBean.getSqls().toArray(new String[]{}));
		for(int i=0;i<indexs.length;i++){
			if(i>0)sb.append("|");
			sb.append(indexs[i]);
		}
		queryBean.setClearCache(sb.toString());

		queryParam.clear();
		queryParam = null;
		return queryBean;
	}





	/**
	 * 格式化SQL数据<br>
	 * 在SQL模版中，只有AUTOSEARCH模式才能够使用ComplexValue模式格式数据
	 * @param sql sql模版
	 * @param args	sql模版参数名称
	 * @param data	格式化SQL的数据
	 * @return
	 */
	public String  parserQuerySQL(String sql,String[] args,Map<String,String> data,boolean isSelect,String[] safe_args)  throws QDevelopException{
		String[] paramArray = data.get("param") == null?null:data.get("param").split(";");

		/** "1>0" 是AutoSearch的特殊标识！！！**/
		boolean isAutoSearch =  sql.indexOf(QDevelopConstant.AUTO_SEARCH_MARK)>-1;

		int arrayIndex = 0;
		for(String arg:args){
			if(data.get(arg)!=null){
				if(isSelect && (isAutoSearch&&isComplexValue.matcher(data.get(arg)).find()||data.get(arg).indexOf("NULL")>-1)){
					checkHackValue(arg,data.get(arg),safe_args);
					sql = sql.replaceAll(append(" [a-z|A-Z|\\_|\\.]+ ?= ?'?\\$\\[",arg,"\\]'?"), toComplexValue(arg,data.get(arg),isSelect));
				}else{ 
					if(data.get(arg).equals("") && sql.indexOf(append("'$[",arg,"]'"))==-1){//兼容value=''的数值型数据
						sql = sql.replace(append("$[",arg,"]"),"0");
					}else{
						String value = parseSpecilCharater(arg,data.get(arg),safe_args);
						checkHackValue(arg,value,safe_args);
						sql = sql.replace(append("$[",arg,"]"), value);
					}
				}
			}else if(paramArray!=null&&arrayIndex<paramArray.length){
				if(isAutoSearch&&isComplexValue.matcher(paramArray[arrayIndex]).find() || paramArray[arrayIndex].indexOf("NULL")>-1){
					checkHackValue(arg,paramArray[arrayIndex],safe_args);
					sql = sql.replaceAll(append(" [a-z|A-Z|\\_|\\.]+ ?= ?'?\\$\\[",arg,"\\]'?"), toComplexValue(arg,paramArray[arrayIndex],isSelect));
				}else{
					String value = parseSpecilCharater(arg,paramArray[arrayIndex],safe_args);
					checkHackValue(arg,value,safe_args);
					sql = sql.replace(append("$[",arg,"]"), value);
				}
				arrayIndex++;
			}
		}
		if(isSelect && isAutoSearch){
			Iterator<String> iter = data.keySet().iterator();
			String key;String[] keys;
			StringBuffer sb = new StringBuffer();
			while(iter.hasNext()){
				key = iter.next();
				if(!key.equals("param")&&!ArrayUtils.contains(args, key)){
					/**增加自定义复合条件SQL查询；条件：key中含有转义符"*"**/
					/**例：key1*key2=('a'&'b')|('c'&'d') ==> (key1='a' and key2='b') or (key1='c' and key2='d') **/
					if(key.indexOf("*")>-1){
						String value=data.get(key);
						checkHackValue(key,value,safe_args);
						if(sb.length()>0)sb.append(" and");
						sb.append(" (");
						if(value.indexOf("(")>-1 && value.indexOf(")")>-1){
							String[] firstValues = value.replaceAll("^\\(|\\)$", "").split("\\)[&|\\|]\\(");
							char[] tmp = value.replaceAll("(\\()(.*?)(\\))", "").toCharArray();
							for(int i=0;i<firstValues.length;i++){
								if(i>0)sb.append(" ").append((tmp[i-1]=='|')?"or":"and").append(" ");
								sb.append("(");
								sb.append(createComplexSelect(key,firstValues[i]));
								sb.append(")");
							}
						}else{
							sb.append(createComplexSelect(key,value));
						}
						sb.append(")");
					}else{
						keys = key.split("&|\\|");
						if(sb.length()>0)sb.append(" and ");
						if(key.indexOf("|")>-1)sb.append("(");
						for(int i=0;i<keys.length;i++){
							if(i>0)sb.append(key.indexOf("&")>-1?" and ":" or ");
							checkHackValue(keys[i],data.get(key),safe_args);
							sb.append(toComplexValue(keys[i],data.get(key),isSelect));
						}
						if(key.indexOf("|")>-1)sb.append(")");
					}
				}
			}
			if(sb.length()>0){

				sql = sql.replace(QDevelopConstant.AUTO_SEARCH_MARK, sb.toString());
			}
		}else if(sql.substring(0,6).toUpperCase().equals("UPDATE") && sql.indexOf("$[") > -1){//去除更新语句中不关心的参数
			sql = removeUpdateNoArgsParams.matcher(sql).replaceAll("");
			sql = removeUpdateClean3.matcher(sql).replaceAll(",");
			sql = removeUpdateClean1.matcher(sql).replaceAll("set ");
			sql = removeUpdateClean2.matcher(sql).replaceAll(" where");
		}
		return clearParam(sql);
	}

	private static Pattern removeUpdateNoArgsParams = Pattern.compile("`?[a-z|A-Z|\\_]+`? ?= ?'?\\$\\[[a-z|A-Z|\\_]+\\]'?");
	private static Pattern removeUpdateClean1 = Pattern.compile("set +,+|SET +,+");
	private static Pattern removeUpdateClean2 = Pattern.compile(",+ +where|,+ +WHERE");
	private static Pattern removeUpdateClean3 = Pattern.compile(",[ |,]+,|,,");
	private static Pattern removeHackSQL = Pattern.compile("\\(|\\)|select | from |SELECT | FROM | where | WHERE ");
	private static Pattern findTableNameReg = Pattern.compile("^.+?INTO|^UPDATE| WHERE.+?$| SET.+?$| VALUE.+?$|\\(.+?\\)|^.+?FROM|`| ");
	private static Pattern _SQL_ATTACK_REG =  Pattern.compile("(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|update|and|or|delete|insert|trancate|char|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)",Pattern.CASE_INSENSITIVE);
	//Pattern.compile(" +?or +?'?[0-9a-z]+?'? *[>|<|=]+? *'?[0-9a-z]+?'?|show +?table|drop +?table|drop +?database|delete +?table|alter +?table|exec ");

	/**
	 * key1*key2*key3 = a&b|c
	 * @param key
	 * @param value
	 * @return
	 */
	private String  createComplexSelect(String key,String value){
		StringBuffer sb = new StringBuffer();
		String[] keys = key.split("\\*"); 
		String[] values = value.split("&|\\|");
		char[] tmp= value.replaceAll("[^&|^\\|]", "").toCharArray();
		if(keys.length!=values.length||values.length-1!=tmp.length)throw new QDevelopException(QProperties.getInstance().getProperty("sql_parser_complex_query_error_msg", key,value));
		for(int i=0;i<keys.length;i++){
			if(i>0){
				sb.append(" ").append((tmp[i-1]=='|')?"or":"and");
			}
			sb.append(toComplexValue(keys[i],values[i],true));
		}
		return sb.toString();
	}
	/**
	 * 检查是否是SQL 异常注入信息
	 * @param key
	 * @param value
	 * @param safe_args
	 * @throws QDevelopException
	 */
	public static void checkHackValue(String key,String value,String[] safe_args) throws QDevelopException{
		if(safe_args!=null && (safe_args[0].equals("*") || ArrayUtils.contains(safe_args, key))){
			return;
		}
		if(_SQL_ATTACK_REG.matcher(value.toLowerCase()).find() && value.indexOf(" ")>-1){
			throw new QDevelopException(append("您的请求含有可能造成对系统的攻击[",key,"=",value,"]，被服务器拦截! 如有疑问，请联系系统管理员。"));
		}
	}
	private boolean globeNoSpecilSet = QProperties.getInstance().getBoolean("SQLModelParser.globeNoSpecilSet");
	
	private String parseSpecilCharater(String key,String value,String[] safe_args){
		if(value==null)return null;
//		if(globeNoSpecilSet)return value;
		if(globeNoSpecilSet || (safe_args!=null && ArrayUtils.contains(safe_args, key))){
			return value;
		}
		return value.indexOf("'")==-1?value:value.replaceAll("'", "\\\\'");
	}

	private String parseSeqID(String sql,String connect){
		if(sql.indexOf("SEQID.NEXT")==-1 && sql.indexOf("SEQID.CURRENT")==-1)return sql;
		String tableName,key;
		if(sql.indexOf("[") == -1){
			tableName = findTableNameReg.matcher(sql.toUpperCase()).replaceAll("");
			key = "ID";
		}else{
			String[] tmp = sql.substring(sql.indexOf("SEQID")+5).replaceAll("^.+?\\[|\\].+?$|\\]$", "").split(",");
			if(tmp.length == 2){
				tableName = tmp[0];
				key = tmp[1];
			}else{
				tableName = tmp[0];
				key="ID";
			}
		}

		if(sql.indexOf("SEQID.NEXT")>-1){
			sql = sql.replace(append("SEQID.NEXT[",tableName,",",key,"]"), String.valueOf(TableSequence.getNextIdByTable(key, tableName,connect)));
		}else if(sql.indexOf("SEQID.CURRENT")>-1){
			sql = sql.replace(append("SEQID.CURRENT[",tableName,",",key,"]"), String.valueOf(TableSequence.getSeqIdByTable(key, tableName,connect)));
		}
		return sql;
	}

	private String toComplexValue(String key,String value,boolean isSelect) throws QDevelopException{
		String[] tmp ;
		if(isSelect){
			tmp = removeHackSQL.matcher(value).replaceAll("").split("&|\\|");
		}else{
			tmp = new String[]{value.replaceAll("'", "\\\\'")};
		}
		boolean isAnd = value.indexOf("|")==-1;
		StringBuffer sb = new StringBuffer();
		sb.append(" ");
		if(value.indexOf("ISNULL")>-1 || value.indexOf("ISNOTNULL")>-1){//翻译is null
			sb.append(key).append(" is ").append(value.indexOf("ISNOTNULL")>-1?"not ":"").append("null ");
		}else if(value.indexOf(">")>-1 ||value.indexOf("<")>-1 || value.indexOf("%")>-1){
			if(!isAnd)sb.append("(");
			for(int i=0;i<tmp.length;i++){
				if(i>0)sb.append(" ").append(isAnd?"and":"or");
				sb.append(" ");
				if(tmp[i].indexOf(">")>-1 ||value.indexOf("<")>-1)sb.append(key).append(tmp[i]);
				else if(tmp[i].indexOf("%")>-1) sb.append(key).append(tmp[i].startsWith("!")?" not like ":" like ").append("'").append(tmp[i].replace("!", "")).append("'");
				else sb.append(key).append(tmp[i].startsWith("!")?"<>":"=").append("'").append(tmp[i].replace("!", "")).append("'");
			}
			if(!isAnd)sb.append(")");
		}else{
			if(isAnd){
				for(int i=0;i<tmp.length;i++){
					if(i>0)sb.append(" ").append("and ");
					sb.append(key).append(tmp[i].startsWith("!")?"<>":"=").append("'").append(tmp[i].replace("!", "")).append("'");
				}
			}else{
				sb.append(key).append(value.startsWith("!")?" not":"").append(" in (");
				for(int i=0;i<tmp.length;i++){
					if(i>0)sb.append(",");
					sb.append("'").append(tmp[i].replace("!", "")).append("'");
				}
				sb.append(")");
			}

		}
		return sb.toString();
	}

	//	@SuppressWarnings("unused")
	//	private String toComplexValues(String key,String value){
	//		String[] tmp = value.split("&|\\|");
	//		boolean isAnd = value.indexOf("|")==-1;
	//		StringBuffer sb = new StringBuffer();
	//		sb.append(" ");
	//		if(!isAnd)sb.append("(");
	//		if(value.startsWith(">")||value.startsWith("<")){
	//			for(int i=0;i<tmp.length;i++){
	//				if(i>0)sb.append(isAnd?" AND ":" OR ");
	//				sb.append(key).append(tmp[i]);
	//			}
	//		}else if(value.indexOf("%")>-1){
	//			for(int i=0;i<tmp.length;i++){
	//				if(i>0)sb.append(isAnd?" AND ":" OR ");
	//				sb.append(key).append(tmp[i].startsWith("!")?" NOT LIKE":" LIKE").append(" '").append(tmp[i].replace("!", "")).append("'");
	//			}
	//		}else{
	//			
	//			for(int i=0;i<tmp.length;i++){
	//				if(i>0)sb.append(isAnd?" AND ":" OR ");
	//				if(tmp[i].toUpperCase().endsWith("NULL")){
	//					sb.append(key).append(tmp[i].startsWith("!")?" IS NOT NULL":" IS NULL");
	//				}else{
	//					sb.append(key).append("='").append(tmp[i]).append("'");
	//				}
	//			}
	//		}
	//		if(!isAnd)sb.append(")");
	//		return sb.toString();
	//	}



	/**
	 * 清理无效参数
	 * @param sql
	 * @return
	 */
	private String clearParam(String sql){
		if(sql.indexOf("$[")==-1)return sql;
		if(sql.substring(0,6).toUpperCase().equals("SELECT")){
			String t =  sql.replaceAll(" [A-Z|a-z|\\.|0-9|\\_]+ *?[<|>|=]+ *?'?\\$\\[.+?\\]'?", " 1=1");
			if(t.indexOf("$[")==-1)return t;
			return t.replaceAll("\\$\\[.+\\]", "");
		}else {
			return sql.replaceAll("'\\$\\[.+?\\]'", "''").replaceAll("\\$\\[.+?\\]", "0");
		}
	}

	private static String append(Object ... s){
		StringBuffer sb = new StringBuffer();
		for(Object _s:s)sb.append(_s);
				return sb.toString();
	}
	private boolean parseBoolean(String _boolean){
		if(_boolean==null)return false;
		return Boolean.parseBoolean(_boolean.trim());
	}
	private int parseInt(String _boolean){
		if(_boolean==null)return -1;
		return Integer.parseInt(_boolean.trim());
	}
	//	public static void main(String[] args) {
	//		try {
	//			checkHackValue("","加强型葡萄酒---波特酒",null);
	//		} catch (QDevelopException e) {
	//			e.printStackTrace();
	//		}
	////		System.out.println(SQLModelParser.getInstance().getAttrbuteByIndex("compactfee_leftsettlemoney_select", "cacheConfig"));
	//		//	System.out.println(SQLModelParser.getInstance().toComplexValues("key", "123&123&123"));
	//		//	System.out.println(SQLModelParser.getInstance().toComplexValues("key", "123|123|123"));
	//	}
}
