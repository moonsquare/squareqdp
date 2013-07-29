package com.qdevelop.cache.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Element;

import com.qdevelop.cache.CacheFactory;
import com.qdevelop.cache.CacheStatus;
import com.qdevelop.cache.FirstCache;
import com.qdevelop.cache.SecondCache;
import com.qdevelop.cache.bean.CacheStatusBean;
import com.qdevelop.cache.bean.CasIndexArray;
import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.bean.RemoteCasIndex;
import com.qdevelop.cache.clear.CacheIndexsUtils;
import com.qdevelop.cache.clear.ClearCacheQueue;
import com.qdevelop.cache.clear.TableIndexs;
import com.qdevelop.cache.implments.MemCachedImpl;
import com.qdevelop.cache.sync.RemoteSecondCacheSyncIndexs;
import com.qdevelop.cache.utils.CacheUtils;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.AsynUpdateReload;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.core.sqlmodel.SQLModelLoader;
import com.qdevelop.core.utils.CoreUtils;
import com.qdevelop.lang.QDevelopConstant;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QJson;
import com.qdevelop.utils.QProperties;

public class CacheManageServlet  extends HttpServlet{
	public static String ENCODE="UTF-8";
	public static String[] allows = QProperties.getInstance().getProperty("admin_allow_ips")==null? null : QProperties.getInstance().getProperty("admin_allow_ips").split("\\|");
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = 6139342229732330081L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		response.setContentType("text/html;charset="+ENCODE);
		OutputStream out = response.getOutputStream();
		boolean isAllows=false;
		String ip = request.getRemoteAddr();
		if(allows!=null){
			for(String _ip : allows){
				if(ip.startsWith(_ip)){
					isAllows=true;
					break;
				}
			}
		}
		if(allows!=null && !isAllows){
			out.write(("您的IP["+ip+"]不在允许访问范围之内").getBytes(ENCODE));
		}else{
			String url = request.getRequestURI();
			if(url.indexOf("connectMonitor")>-1){
				String[] monitors = ConnectFactory.watchConnect();
				if(url.endsWith("json")){
					StringBuffer sb = new StringBuffer();
					sb.append("[");
					boolean isFirst=true;
					for(String m:monitors){
						String[] tmp = m.split("\\|");
						sb.append(!isFirst?",":"").append("{url:'")
						.append(tmp[0]).append("',usr:'")
						.append(tmp[1]).append("',numActive:")
						.append(tmp[2]).append(",numIdle:")
						.append(tmp[3]).append(",maxActive:")
						.append(tmp[4]).append(",config:'")
						.append(tmp[5]).append("'}");
						isFirst = false;
					}
					sb.append("]");
					out.write(sb.toString().getBytes(ENCODE));
				}else{
					out.write(("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><head><title>\u7CFB\u7EDF\u8FDE\u63A5\u6C60\u76D1\u63A7</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/></head>" +
							"<style>table{border:solid 1px #dbdada;} table th{ font-weight:100; background-color:#f3f3f3; text-align:center;} table td{border:solid 1px #dbdada;} input{width:200px;} .btn{width:50px}</style> <body>").getBytes(ENCODE));
					out.write("<table><tr><th>\u914D\u7F6E\u540D</th><th style='width:700px;'>\u6570\u636E\u5E93\u5730\u5740</th><th>\u7528\u6237\u540D</th><th>\u6D3B\u52A8\u8FDE\u63A5\u6570</th><th>\u5269\u4F59\u8FDE\u63A5\u6570</th><th>\u6700\u5927\u8FDE\u63A5\u6570</th></tr>".getBytes(ENCODE));
					for(String m:monitors){
						String[] tmp = m.split("\\|");
						StringBuffer sb = new StringBuffer().append("<tr><td>")
								.append(tmp[5]).append("</td><td>")
								.append(tmp[0]).append("</td><td>")
								.append(tmp[1]).append("</td><td>")
								.append(tmp[2]).append("</td><td>")
								.append(tmp[3]).append("</td><td>")
								.append(tmp[4]).append("</td></tr>");
						out.write(sb.toString().getBytes(ENCODE));
					}
					out.write("</table></body></html>".getBytes(ENCODE));
				}
				return;
			}

			out.write(("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><head><title>QDevelop\u7F13\u5B58\u7BA1\u7406</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/></head>" +
					"<style>table{border:solid 1px #dbdada;} table th{ font-weight:100; background-color:#f3f3f3; text-align:center;} input{width:150px;} .btn{width:50px}</style> <body>").getBytes(ENCODE));

			if(!url.endsWith("listCache"))
				printInput(request,out);

			if(url.endsWith("listCache")){
				listCache(request,out);
			}else if(url.endsWith("firstCacheManage")){
				firstCacheManage(request,out);
			}else if(url.endsWith("removeSecondCache")){
				removeSecondCache(request);
				listCache(request,out);
			}else if(url.endsWith("removeAllCache")){
				removeAllSecondCache(request,out);
			}else if(url.endsWith("removeCacheTable")){
				removeSecendCacheWithTable(request,out);
			}else if(url.endsWith("removeCacheWithIndex")){
				removeCacheWithIndex(request,out);
			}else if(url.endsWith("printTableIndexs")){
				printTableIndexs(request,out);
			}else if(url.endsWith("clearRemoteIndexs")){
				clearRemoteIndexs(request,out);
			}else if(url.endsWith("printCacheValues")){
				printCacheValues(request,out);
			}else if(url.endsWith("printCacheStatus")){
				printCacheStatus(request,out);
			}else if(url.endsWith("printLocalIndex")){
				printLocalIndex(request,out);
			}else if(url.endsWith("systemReload")){
				CoreUtils.systemReload();
				out.write("\u606D\u559C\u60A8\uFF0C\u6210\u529F\u91CD\u8F7D\u7CFB\u7EDF\u914D\u7F6E\uFF01\uFF01".getBytes(ENCODE));
			}else if(url.endsWith("systemCacheClear")){
				CacheIndexsUtils.getInstance().systemCacheIndexsClean();
				out.write("System cache indexs clear successful !".getBytes(ENCODE));
			}else if(url.endsWith("exeAsyn")){
				AsynUpdateReload aur = new AsynUpdateReload(request.getParameter("date"));
				aur.run();
				out.write(("System AsynUpdateReload ["+aur.getSize()+"] successful !").getBytes(ENCODE));
			}else if(url.endsWith("shutdown")){
				CacheFactory.shutdown();
				ConnectFactory.getInstance().shutdown();
				out.write("\u7CFB\u7EDF\u8D44\u6E90\u91CA\u653E\u6210\u529F".getBytes(ENCODE));
			}else if(url.endsWith("printTablePrimitKey")){
				out.write(TableIndexs.getInstance().toString().getBytes(ENCODE));
			}
			out.write("</body></html>".getBytes(ENCODE));
		}
		out.flush();
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		doGet(request,response);
	}

	public void destroy(){
		super.destroy();
		CoreFactory.shutdown();
	}

	/**
	 * 打印缓存值
	 * @param request
	 * @param out
	 * @throws IOException
	 */
	private void printCacheValues(HttpServletRequest request,OutputStream out) throws IOException{
		String cacheKey = request.getParameter("cacheKey");
		if(cacheKey==null){
			return ;
		}
		Serializable value = MemCachedImpl.getInstance().get(cacheKey, 500);
		if(value == null){
			out.write(("\u5BF9\u4E0D\u8D77\uFF0C[key="+cacheKey+"]\u6CA1\u67E5\u5230\u4EFB\u4F55\u7F13\u5B58\u6570\u636E").getBytes(ENCODE));
			return;
		}

		try {
			String val;
			if(value instanceof Integer ||  value instanceof Double ||  value instanceof Long || value instanceof String){
				val = String.valueOf(value);
			}else
				val = QJson.getJSONString(value);
			out.write("<div style='width:800px'>".getBytes(ENCODE));
			out.write((cacheKey+" = ").getBytes(ENCODE));
			out.write(val.replaceAll("<", "&lt;").replaceAll(">", "&gt;").getBytes(ENCODE));
			out.write("</div>".getBytes(ENCODE));
		} catch (Exception e) {
			e.printStackTrace();
		}


	}


	private void listCache(HttpServletRequest request,OutputStream out) throws IOException{
		StringBuffer sb = new StringBuffer();
		sb.append("<fieldset><legend><B>\u4E8C\u7EA7\u7F13\u5B58\u67E5\u8BE2</B></legend><form type=\"post\" action=\"listCache\">")
		.append("\u914D\u7F6E\uFF1A<input id='config' name='config' value='").append(request.getParameter("config")==null?QDevelopConstant.CACHE_NAME_RESULTBEAN_TACTICS:request.getParameter("config")).append("'/>")
		.append("&nbsp;&nbsp;Index\uFF1A<input id='index' name='index' value='").append(request.getParameter("index")==null?"":request.getParameter("index")).append("'/>")
		.append("&nbsp;&nbsp;<span><input style='width:20px' id='isCacheCheck' name='isCacheCheck' type='checkbox' value='1'/>\u68C0\u67E5\u7F13\u5B58</span>&nbsp;&nbsp;<input type='submit' class='btn' value='\u63D0\u4EA4'/></form><div>\u914D\u7F6E\u4E0D\u77E5\u9053\u65F6\u53EF\u4EE5\u4E0D\u586B;Default is <span style='color:red'>")
		.append(QDevelopConstant.CACHE_NAME_RESULTBEAN_TACTICS).append("</span></div></fieldset><br/>");
		out.write(sb.toString().getBytes(ENCODE));
		String index =  request.getParameter("index");
		if(index==null)return;
		boolean isCacheCheck = request.getParameter("isCacheCheck") == null ? false:true;
		//		SecondCache.getInstance().asynRun();
		CasIndexArray casIndexArray;
		String config = request.getParameter("config");
		if(config==null){
			config = SQLModelLoader.getInstance().getConfigByIndex(index);
		}
		casIndexArray =CacheIndexsUtils.getInstance().getCasIndexArrayFromMemcache(CacheUtils.stackKey(index, config)); 
		if(casIndexArray !=null){
			boolean _isCached;
			out.write(("<fieldset><legend>\u67E5\u8BE2\u4E8C\u7EA7\u7F13\u5B58["+CacheUtils.stackKey(index, config)+"]\u5217\u8868 ["+casIndexArray.size()+"][<a href=\"removeCacheWithIndex?index="+index+"&config="+config+"\">\u5168\u90E8\u5220\u9664</a>]</legend><table ><tr><th>\u64CD\u4F5C</th><th>Config</th><th>\u547D\u4E2D\u6B21\u6570</th><th>\u5E73\u5747\u8BF7\u6C42(ms)</th><th>\u521B\u5EFA\u65F6\u95F4</th><th>\u8BF7\u6C42\u65F6\u95F4</th><th>cached</th><th>\u8BF7\u6C42\u53C2\u6570</th></tr>").getBytes(ENCODE));
			for(IndexItem ii : casIndexArray.values()){
				if(isCacheCheck)
					_isCached = MemCachedImpl.getInstance().get(ii.toKey(),100)!=null;
				else
					_isCached = ii.isCached();

				out.write(new StringBuffer()
				.append("<tr><td>[<a href='removeSecondCache?index=").append(index).append("&rIndex=").append(ii.getKey()).append("&config=").append(ii.getConfig())
				.append("'>\u5220</a>]-[<a href='printCacheValues?cacheKey=").append(ii.getConfig()).append(ii.getKey())
				.append("' target='blank'>\u503C</a>]</td><td title='").append(ii.getKey()).append("'>")
				.append(ii.getConfig()).append("</td><td>")
				.append(ii.getCacheTimes()).append("</td><td>").append(ii.getAvgTime()/1000).append("</td><td>").append(toDate(ii.getCreateTime()))
				.append("</td><td>").append(toDate(ii.getLastTime())).append("</td><td><span title='").append(ii.isCached()).append("'>").append(_isCached).append("</span></td><td>").append(ii.getQuery()).append("</td></tr>")
				.toString().getBytes(ENCODE));
			}
			out.write("</table></fieldset>".getBytes(ENCODE));
		}
	}

	public void clearRemoteIndexs(HttpServletRequest request,OutputStream out) throws IOException{
		String suffix = request.getParameter("suffix");
		String prefix = request.getParameter("prefix");
		if(suffix == null && prefix==null){
			return;
		}
		RemoteCasIndex _remotecasindex = RemoteSecondCacheSyncIndexs.getRemoteIndexs();
		if(_remotecasindex.size() == 0)return;
		RemoteCasIndex _remotecasindexNew = new RemoteCasIndex();
		int size = 0;
		java.util.Iterator<Entry<String, String[]>> indexs = _remotecasindex.entrySet().iterator();
		while(indexs.hasNext()){
			Entry<String, String[]> itor = indexs.next();
			String[] index = itor.getValue();
			ArrayList<String> tmp = new ArrayList<String>();
			for(String idx : index){
				if(suffix!=null && prefix == null){
					if(idx.endsWith(suffix)){
						size++;
					}else{
						tmp.add(idx);
					}
				}else if(suffix==null && prefix != null ){
					if(!idx.startsWith(prefix)){
						size++;
					}else{
						tmp.add(idx);
					}

				}else{
					if(idx.endsWith(suffix) && idx.startsWith(prefix)){
						size++;
					}else{
						tmp.add(idx);
					}
				}
			}
			_remotecasindexNew.addCasIndex(itor.getKey(), tmp.toArray(new String[]{}));
		}
		boolean saveSuccess = RemoteSecondCacheSyncIndexs.saveRemoteIndexs(_remotecasindexNew);
		out.write(new StringBuffer().append("RemoteCasIndex").append(" remove:[").append(size).append("] save:").append(saveSuccess).toString().getBytes(ENCODE));
	}

	private void printTableIndexs(HttpServletRequest request,OutputStream out) throws IOException{
		String table = request.getParameter("table") == null? null : request.getParameter("table").toUpperCase();
		if(table==null){
			out.write("没有获取到参数table".getBytes(ENCODE));
			return;
		}
		boolean isCacheCheck = request.getParameter("isCacheCheck") == null ? false:true;
		out.write(("<fieldset><legend>\u8868["+table+"]\u5173\u8054\u7684\u6240\u6709\u67E5\u8BE2\u7D22\u5F15</legend><ul>").getBytes(ENCODE));
		RemoteCasIndex _RemoteCasIndex = RemoteSecondCacheSyncIndexs.getRemoteIndexs();
		if(_RemoteCasIndex==null)return;
		String[] tbs = table.replaceAll(" ", "").split("\\|");
		for(String tt : tbs){
			String[] indexs = _RemoteCasIndex.get(tt);
			int idx=0;
			if(indexs==null)continue;
			for(String index:indexs){
				int at = index.indexOf("@");
				if(at==-1)continue;
				String key = index.substring(0,at);
				String config = index.substring(at+1);
				out.write(new StringBuffer().append("<li>[<a href=\"removeCacheWithIndex?index=").append(key).append("&config=").append(config).append("\">\u5168\u90E8\u5220\u9664</a>]")
						.append("&nbsp;-&nbsp;[<a href=\"listCache?index=").append(key).append("&config=").append(config).append("\" target='blank'>\u67E5\u770B\u7F13\u5B58</a>]")
						.append(isCacheCheck?checkCacheIndexsView(key,config):"")
						.append("&nbsp;:&nbsp;").append(++idx).append("\u3001&nbsp;").append(index).append("</li>").toString().getBytes(ENCODE));
			}
		}
		out.write("</ul></fieldset>".getBytes(ENCODE));
	}

	private String checkCacheIndexsView(String key,String config){
		StringBuffer sb = new StringBuffer();
		sb.append(" - [");
		CasIndexArray casIndexArray = SecondCache.getInstance().getCacheIndexs(key,config);
		if(casIndexArray == null){
			sb.append("\u6CA1\u7F13\u5B58");
		}else{
			sb.append("\u8BA1").append(casIndexArray.size());
		}
		return sb.append("]").toString();
	}

	private void printInput(HttpServletRequest request,OutputStream out) throws IOException{
		StringBuffer sb = new StringBuffer();

		/**一级缓存管理**/
		sb.append("<fieldset><legend><B>\u4E00\u7EA7\u7F13\u5B58\u7BA1\u7406</B></legend><form type=\"post\" action=\"firstCacheManage\">")
		.append("&nbsp;&nbsp;key\u503C\uFF1A<input id='id' name='id' value=''/>")
		.append("&nbsp;&nbsp;\u8868\u540D\uFF1A<input id='table' name='table' value='").append(request.getParameter("table")==null?"":request.getParameter("table")).append("'/>")
		.append("&nbsp;&nbsp;DBConfig\uFF1A<input id='db' name='db' value=''/>")
		.append("&nbsp;&nbsp;Where\uFF1A<input id='where' name='where' value=''/>&nbsp;&nbsp;<input id='isLoad' style='width:20px' type='checkbox' name='isLoad' value='true'/>\u52A0\u8F7D")
		.append("&nbsp;&nbsp;<input type='submit' class='btn' style='width:70px' value='\u5220\u9664/\u52A0\u8F7D'/>")
		.append("</form></fieldset><br/>");

		/**二级缓存管理**/
		sb.append("<fieldset><legend><B>\u4E8C\u7EA7\u7F13\u5B58\u7BA1\u7406</B></legend><table style='border:solid 0px #ffffff;'><tr><td><form type=\"post\" action=\"removeCacheTable\">")
		.append("\u8868\u540D\u5220\u9664\u4E8C\u7EA7\u7F13\u5B58\uFF1A<input id='table' name='table' value='")
		.append(request.getParameter("table")==null?"":request.getParameter("table"))
		.append("'/>&nbsp;&nbsp;<input type='submit' class='btn' value='\u5220\u9664'/></form>")
		.append("</td><td width='50px'></td><td><form type='post' action='printTableIndexs'>\u8868\u540D\u67E5\u8BE2\u4E8C\u7EA7\u7F13\u5B58\uFF1A<input id='table' name='table'value='")
		.append(request.getParameter("table")==null?"":request.getParameter("table"))
		.append("' />&nbsp;&nbsp;<span><input style='width:20px' id='isCacheCheck' name='isCacheCheck' type='checkbox' value='1'/>\u68C0\u67E5\u7F13\u5B58</span>&nbsp;&nbsp;<input type='submit' class='btn' value='\u67E5\u770B'/></form></td></tr></table></fieldset><hr>");

		out.write(sb.toString().getBytes(ENCODE));
	}

	private void removeSecondCache(HttpServletRequest request){
		String key = request.getParameter("rIndex");
		String config = request.getParameter("config");
		if(key == null || config == null)return;
		SecondCache.getInstance().remove(key, config);
		CacheIndexsUtils.getInstance().sync();
		//		SecondCache.getInstance().asynRun();
	}

	private void removeCacheWithIndex(HttpServletRequest request,OutputStream out) throws IOException{
		String key = request.getParameter("index");
		String config = request.getParameter("config");
		if(key == null || config == null)return;
		removeByindex(key,config,request,out);
	}

	private void removeSecendCacheWithTable(HttpServletRequest request,OutputStream out) throws IOException{
		String tables = request.getParameter("table") == null? null : request.getParameter("table").toUpperCase();
		if(tables==null){
			out.write("没有获取到参数table".getBytes(ENCODE));
			return;
		}
		long timer = System.currentTimeMillis();
		RemoteCasIndex _RemoteCasIndex = RemoteSecondCacheSyncIndexs.getRemoteIndexs();
		if(_RemoteCasIndex==null)return;
		String[] tableArray = tables.split("\\|");
		for(String table:tableArray){
			String[] indexs = _RemoteCasIndex.get(table);
			if(indexs==null)return;
			for(String index:indexs){
				int at = index.indexOf("@");
				if(at==-1)continue;
				String key = index.substring(0,at);
				String config = index.substring(at+1);
				removeByindex(key,config,request,out);
			}
		}
		out.write(new StringBuffer().append("user timer: ").append(System.currentTimeMillis()-timer).append("ms").toString().getBytes(ENCODE));
	}
	private void removeByindex(String index,String config,HttpServletRequest request,OutputStream out) throws IOException{
		int idx = 0;
		CasIndexArray casIndexArray = SecondCache.getInstance().getCacheIndexs(index,config);
		if(casIndexArray!=null){
			for(IndexItem ii : casIndexArray.values()){
				out.write(new StringBuffer().append(idx).append(" remove: ").append(ii.getKey()).append("<br/>").toString().getBytes(ENCODE));
				MemCachedImpl.getInstance().remove(ii.toKey(),null,null);
				idx++;
			}
			MemCachedImpl.getInstance().remove(casIndexArray.getStackKey(),null,null);
			casIndexArray.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private void removeAllSecondCache(HttpServletRequest request,OutputStream out) throws IOException{
		long timer = System.currentTimeMillis();
		Element root = SQLModelLoader.getInstance().getAllSQLModel().getRootElement();
		Iterator<Element> itor = root.elementIterator("property");
		Element property;
		while(itor.hasNext()){
			property = itor.next();
			if(property.attributeValue("index")==null || property.attributeValue("cacheConfig")==null)continue;
			removeByindex(property.attributeValue("index"),property.attributeValue("cacheConfig"),request,out);
		}
		out.write(new StringBuffer().append("user timer: ").append(System.currentTimeMillis()-timer).append("ms").toString().getBytes(ENCODE));
	}

	private void firstCacheManage(HttpServletRequest request,OutputStream out) throws IOException{
		String key = request.getParameter("id") == null ? "" : request.getParameter("id");
		String table = request.getParameter("table") == null ? "":request.getParameter("table") ;
		String dbConfig = request.getParameter("db") == null ? "":request.getParameter("db") ;
		boolean isLoad = request.getParameter("isLoad") == null ? false:request.getParameter("isLoad").equals("true") ;


		if(!key.equals("")){
			FirstCache.getInstance().remove(key, table);
			out.write(("\u4E00\u7EA7\u7F13\u5B58[key="+FirstCache.getInstance().toKey(key, table)+"]\u5220\u9664\u6210\u529F").getBytes(ENCODE));
		}else if(key.equals("") && !table.equals("") && !dbConfig.equals("")){
			Connection _conn=null;
			try {
				String where = request.getParameter("where") == null ? "":request.getParameter("where") ;
				String[] tables = table.split("\\|");
				_conn = ConnectFactory.getInstance(dbConfig).getConnection();

				for(String tableName : tables){
					long timer = System.currentTimeMillis();
					Content.size = 0;
					String uniKey = TableIndexs.getInstance().getTablePrimaryKey(tableName,_conn,dbConfig);
					if(uniKey!=null){
						StringBuffer countSql = new StringBuffer().append("select count(0) as num from ").append(tableName).append(where.equals("")?"":(" where "+where));
						@SuppressWarnings("unchecked")
						Map<String,Object> result = QueryFromDataBaseImp.getInstance().selectSingle(countSql.toString(), _conn, null);
						if(result!=null && result.get("NUM")!=null){
							int all = Integer.parseInt(String.valueOf(result.get("NUM")));
							int idx = 1;
							if(all>0){
								int maxThread = ConnectFactory.getInstance(dbConfig).getCanUseNum() - 2;
								if(maxThread<1)maxThread=1;
								if(maxThread>100)maxThread=100;
								int maxSize = request.getParameter("maxSize") == null ? (all%maxThread==0?all/maxThread:1+(int)(all/maxThread)):Integer.parseInt(request.getParameter("maxSize"));
								if(maxSize<100)maxSize=100;
								System.out.println("running thread: ["+maxThread+"] count:["+all+"] mission:["+maxSize+"]");
								ExecutorService exec = Executors.newFixedThreadPool(maxThread);
								while(all-(idx-1)*maxSize>0){
									StringBuffer sql = new StringBuffer().append("select ").append(isLoad?"*":uniKey).append(" from ").append(tableName)
											.append(where.equals("")?"":(" where "+where)).append(" limit ").append((idx-1)*maxSize).append(",").append(idx*maxSize);
									exec.execute(new LoadDataThread(dbConfig,sql.toString(),uniKey,tableName,isLoad));
									idx++;
								}
								exec.shutdown();
							}
						}
					}
					out.write(("Table:["+tableName+"] Where:["+where+"] "+(isLoad?"load":"clear")+":["+Content.size+"] use_time:["+(System.currentTimeMillis()-timer)+"]").getBytes(ENCODE));
				}
			} catch (QDevelopException e) {
				out.write(e.getMessage().getBytes(ENCODE));
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				try {
					if(_conn!=null)
						_conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void printCacheStatus(HttpServletRequest request,OutputStream out){
		try {
			out.write("<table><tr><th width='500px'>index</th><th width='300px'>config</th><th width='200px'>cached</th><th width='200px'>set</th><th width='200px'>deleted</th></tr>".getBytes(ENCODE));
			for(CacheStatusBean csb : CacheStatus.getInstance().values()){
				out.write(("<tr><td>"+csb.index+"</td><td>"+csb.config+"</td><td>"+csb.cacheTimer+"</td><td>"+csb.addTimer+"</td><td>"+csb.deleteTimer+"</td></tr>").getBytes(ENCODE));
			}
			out.write("</table>".getBytes(ENCODE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printLocalIndex(HttpServletRequest request,OutputStream out){
		try {
			String table=request.getParameter("table");
			if(table == null){
				out.write("please input arguments[cacheManage/printLocalIndex?table=XXX]".getBytes(ENCODE));
				return;
			}
			HashSet<String> indexs =  SQLModelLoader.getInstance().getClearIndexByTableName(table);
			if(indexs == null){
				out.write("no local indexs".getBytes(ENCODE));
				return;
			}
			for(String clearIndex : indexs){
				out.write(clearIndex.getBytes(ENCODE));
				out.write("<br/>".getBytes(ENCODE));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static SimpleDateFormat _defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String toDate(long timer){
		GregorianCalendar c1 = new GregorianCalendar();
		c1.setTimeInMillis(timer);
		return _defaultFormat.format(c1.getTime());
	}

	public void init(){
		ConnectFactory.initAllConnect();
		SQLModelLoader.getInstance();
		ClearCacheQueue.getInstance();
	}
}
