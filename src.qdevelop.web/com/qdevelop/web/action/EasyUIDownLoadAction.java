package com.qdevelop.web.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.struts2.ServletActionContext;

import com.qdevelop.bean.QueryBean;
import com.qdevelop.bean.SecurityUserBean;
import com.qdevelop.core.CoreFactory;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QJson;
import com.qdevelop.utils.QLog;
import com.qdevelop.utils.QProperties;
import com.qdevelop.utils.QString;
import com.qdevelop.utils.cache.MapCache;
@SuppressWarnings({ "unchecked","rawtypes" })
public class EasyUIDownLoadAction extends QDevelopAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8060507936855607899L;
	public static File _downLoadRoot;

	String msg;
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String easyDown(){
		msg = toDownLoadFile().getAbsolutePath();
		return SUCCESS;
	}


	public File toDownLoadFile(){
		String maxDownSize = QProperties.getInstance().getProperty("QDevelop_MAX_DOWN_NUMBER")==null?"1000":QProperties.getInstance().getProperty("QDevelop_MAX_DOWN_NUMBER");
		Map<String,String> query = this.getParamMap();
		query.put("page","1");
		query.put("rows",maxDownSize);
		query.put("maxNum",maxDownSize);
		initPagination(query);
		query.put("$v", String.valueOf(QProperties.serialVersionUID));

		String downUrl = query.get("downUrl") == null ? null : new String(query.get("downUrl"));
		String downFields = query.get("downFields") == null ? null : new String(query.get("downFields"));
		query.remove("downUrl");
		query.remove("downFields");

		List rows = (downUrl!=null && downUrl.indexOf("publicJson/query.action")==-1) ? getRemoteReult(downUrl,query):getQueryResult(query);
		return toCSV(rows,parseDownFields(downFields));
	}

	/**
	 * 
	 * @param downFields 例 field1:title1|field2:title2|...
	 * @return
	 */
	private String[][] parseDownFields(String downFields){
		if(downFields == null)return null;
		String[] tmp = downFields.split("\\|");
		String[][] fields = new String[tmp.length][2];
		for(int i=0;i<tmp.length;i++){
			fields[i][0] = tmp[i].substring(0,tmp[i].indexOf(":"));
			fields[i][1] = tmp[i].substring(tmp[i].indexOf(":")+1);
		}
		return fields;
	}

	private List getQueryResult(Map params) throws QDevelopException{
		params.remove("$v");
		QueryBean qb = CoreFactory.getInstance().getQueryBean(params, this.getUserInfo(),this.getSession());
		return CoreFactory.getInstance().getQueryResult(qb).getResultList();
	}
	private List getRemoteReult(String url,Map params) throws QDevelopException{
		String response = null;
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url){
			public String getRequestCharSet(){
				return "UTF-8";
			}
		};
		HttpServletRequest request = ServletActionContext.getRequest();
		if(request!=null){
			Cookie[] cookies = request.getCookies();
			StringBuffer cookieStr = new StringBuffer();
			for(int i=0;i<cookies.length;i++){
				Cookie cookie = cookies[i];
				cookieStr.append(cookie.getName()).append("=").append(cookie.getValue());
				if(i<cookies.length-1){
					cookieStr.append("; ");
				}
			}
			method.setRequestHeader("Cookie", cookieStr.toString());

			method.setRequestHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; BTRS129225; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)");
			method.setRequestHeader("Referer",request.getHeader("Referer"));
			method.setRequestHeader("x-requested-with","XMLHttpRequest");
			method.setRequestHeader("Accept","application/json, text/javascript, */*; q=0.01");
			method.setRequestHeader("Accept-Language","zh-cn");
			method.setRequestHeader("UA-CPU","x86");
			method.setRequestHeader("Accept-Encoding","gzip, deflate");
			method.setRequestHeader("Connection","Keep-Alive");
			method.setRequestHeader("Cache-Control","no-cache");
		}
		if(params!=null){
			NameValuePair[] paris = new NameValuePair[params.size()];
			int i=0;
			for(Object k:params.keySet()){
				paris[i] = new NameValuePair(String.valueOf(k), String.valueOf(params.get(k)));
				i++;
			}
			method.setRequestBody(paris);
		}

		try {
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				response = method.getResponseBodyAsString();
			}
		} catch (IOException e) {
			QLog.getInstance().systemError("执行HTTP Post请求" + url + "时，发生异常！", e);
			throw new QDevelopException(e);
		} finally {
			method.releaseConnection();
		}
		Map<String,Object> resposeMap = QJson.getMapFromJson(response);
		List rows = (List)resposeMap.get("rows");
		if(rows ==null || rows.size()==0)return null;
		return rows;
	}

	/**
	 * 根据数据集，自动生成结果
	 * @param rows
	 * @return
	 */
	private String[][] autoFields(List rows){
		Map<String,Object> values = (Map<String,Object>)rows.get(0);
		String[][] fields = new String[values.size()][2];
		int idx=0;
		for(String key : values.keySet()){
			fields[idx][0] = key;
			fields[idx++][1] = key;
		}
		return fields;
	}

	private String getTmpFileName(){
		SecurityUserBean sub = this.getUserInfo();
		if(sub==null){
			return QString.append(System.currentTimeMillis(),".csv");
		}
		return QString.append(sub.id,"-",QDate.getNow("yyyyMMddHHmmss"),".csv");
	}

	private File toCSV(List rows,String[][] fields)  throws QDevelopException{
		if(fields == null)fields = autoFields(rows);
		if(_downLoadRoot==null){
			_downLoadRoot = new File("download_tmps");
			if(!_downLoadRoot.exists())_downLoadRoot.mkdirs();
		}
		OutputStreamWriter fw=null;
		File fileName = new File(_downLoadRoot,getTmpFileName());
		try {
			Map data;
			fw = new OutputStreamWriter(new  FileOutputStream(fileName),"gbk");
			for(int i=0;i<fields.length;i++){
				fw.write(new StringBuffer().append(i>0?",":"").append(fields[i][1]).toString());
			}
			for(int i=0;i<rows.size();i++){
				data = (Map)rows.get(i);
				fw.write("\r\n");
				for(int j=0;j<fields.length;j++){
					fw.write(new StringBuffer().append(j>0?",":"").append("\"").append(toCsvValue(mergeData(data,fields[j][0]))).append("\"").toString());
				}
			}
		} catch (IOException e) {
			throw new QDevelopException(e);
		}finally{
			try {
				if(fw!=null)
					fw.close();
				fw  = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileName;
	}

	private Object mergeData(Map data,String field){
		if(data.get(field)!=null && data.get(MapCache.getInstance().getFormatterKey(field))!=null){
			String v = String.valueOf(data.get(MapCache.getInstance().getFormatterKey(field)));
			return new StringBuffer().append("[").append(data.get(field)).append("] ").append(v.indexOf("<")>-1?v.replaceAll("\\<.+?\\>", ""):v).toString();
		}
		return data.get(field);
	}

	private String toCsvValue(Object val){
		if(val==null)return "";
		String tmp = String.valueOf(val);
		if(tmp.equals("null"))return "";
		if(tmp.indexOf("\"")==-1)return tmp;
		return tmp.replaceAll("\"", "\"\"");
	}

	/*		public Map getParamMap(){
			Map query = new HashMap();
			query.put("downUrl", "http://oa.fantong.com/publicJson/query.action");
			query.put("downFields", "OID:OID|PID:商品|BID:商家");
			query.put("index", "productOrderView_new");
	//		query.put("$v", "ff_gg@CD4A3D25AF94C556227CEDD27B8FEECF");
			return query;
		}
	 */
	//			public static void main(String[] args) {
	//				String url = "http://192.168.100.2:6000/callcenterAjax/OrderHandlerAction.action?bod__status=7&distribute=false&method=order_list&o__city=%E5%8C%97%E4%BA%AC&o__province=%E5%8C%97%E4%BA%AC&page=1&rows=15&search=true";
	//				String response = null;
	//				HttpClient client = new HttpClient();
	//				PostMethod method = new PostMethod(url){
	//					public String getRequestCharSet(){
	//						return "UTF-8";
	//					}
	//				};
	//				method.setRequestHeader("Cookie", " JSESSIONID=aaaqmvtVLihsifb1N69At; loginName=660f2a0478032c; passWord=34622bb1130370f154741a91042e2c4c ");
	//				method.setRequestHeader("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:11.0) Gecko/20100101 Firefox/11.0 ");
	//				method.setRequestHeader("Referer","http://192.168.100.2:6000/callcenter/order.orderList.action?$r=0.3448899077897995");
	////				if(params!=null){
	////					NameValuePair[] paris = new NameValuePair[params.size()];
	////					int i=0;
	////					for(Object k:params.keySet()){
	////						paris[i] = new NameValuePair(String.valueOf(k), String.valueOf(params.get(k)));
	////						i++;
	////					}
	////					method.setRequestBody(paris);
	////				}
	//				
	//				try {
	//					client.executeMethod(method);
	//					if (method.getStatusCode() == HttpStatus.SC_OK) {
	//						response = method.getResponseBodyAsString();
	//					}
	//				} catch (IOException e) {
	//					QLog.getInstance().systemError("执行HTTP Post请求" + url + "时，发生异常！", e);
	//					throw new QDevelopException(e);
	//				} finally {
	//					method.releaseConnection();
	//				}
	//				System.out.println(response);
	//				Map<String,Object> resposeMap = QJson.getMapFromJson(response);
	////				List rows = (List)resposeMap.get("rows");
	////				if(rows ==null || rows.size()==0)return null;
	////				return rows;
	//			
	//			}

}
