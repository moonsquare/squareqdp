package com.qdevelop.utils.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

public class HttpClient {
	private String defaultContentEncoding = "utf-8";

	public HttpClient() {
//		this.defaultContentEncoding = Charset.defaultCharset().name();
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public ServerInfo sendGet(String urlString) throws IOException {
		return this.send(urlString, "GET", null, null, 0);
	}

	public ServerInfo sendGet(String urlString, int timeout) throws IOException {
		return this.send(urlString, "GET", null, null, timeout);
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public ServerInfo sendGet(String urlString, Map<String, String> params)
			throws IOException {
		return this.send(urlString, "GET", params, null, 0);
	}

	public ServerInfo sendGet(String urlString, Map<String, String> params,
			int timeout) throws IOException {
		return this.send(urlString, "GET", params, null, timeout);
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @param propertys
	 *            请求属�?
	 * @return 响应对象
	 * @throws IOException
	 */
	public ServerInfo sendGet(String urlString, Map<String, String> params,	Map<String, String> propertys) throws IOException {
		return this.send(urlString, "GET", params, propertys, 0);
	}

	public ServerInfo sendGet(String urlString, Map<String, String> params,
			Map<String, String> propertys, int timeout) throws IOException {
		return this.send(urlString, "GET", params, propertys, timeout);
	}

	/**
	 * 发�?POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public ServerInfo sendPost(String urlString) throws IOException {
		return this.send(urlString, "POST", null, null, 0);
	}

	public ServerInfo sendPost(String urlString, int timeout)
			throws IOException {
		return this.send(urlString, "POST", null, null, timeout);
	}

	/**
	 * 发POST请求 用户验证常用方法
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public ServerInfo sendPost(String urlString, Map<String, String> params)
			throws IOException {
		return this.send(urlString, "POST", params, null, 0);
	}

	public ServerInfo sendPost(String urlString, Map<String, String> params,
			int timeout) throws IOException {
		return this.send(urlString, "POST", params, null, timeout);
	}

	/**
	 * 发POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @param propertys
	 *            请求属
	 * @return 响应对象
	 * @throws IOException
	 */
	public ServerInfo sendPost(String urlString, Map<String, String> params,
			Map<String, String> propertys) throws IOException {
		return this.send(urlString, "POST", params, propertys, 0);
	}

	public ServerInfo sendPost(String urlString, Map<String, String> params,
			Map<String, String> propertys, int timeout) throws IOException {
		return this.send(urlString, "POST", params, propertys, timeout);
	}

	/**
	 * 发HTTP请求
	 * 
	 * @param urlString
	 * @return 响映对象
	 * @throws IOException
	 */
	private ServerInfo send(String urlString, String method,
			Map<String, String> parameters, Map<String, String> propertys,
			int timeout) throws IOException {
		HttpURLConnection urlConnection = null;

		if (method.equalsIgnoreCase("GET") && parameters != null) {
			StringBuffer param = new StringBuffer();
			int i = 0;
			for (String key : parameters.keySet()) {
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(key).append("=").append(parameters.get(key));
				i++;
			}
			urlString += param;
		}
		URL url = new URL(urlString);
		urlConnection = (HttpURLConnection) url.openConnection();

		// String cookies = "session_cookie=value";
		// urlConnection.setRequestProperty("Cookie", cookies);

		urlConnection.setRequestMethod(method);
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);
		// 设置超时时间
		urlConnection.setConnectTimeout(timeout);
		urlConnection.setReadTimeout(timeout);

		if (propertys != null)
			for (String key : propertys.keySet()) {
				urlConnection.addRequestProperty(key, propertys.get(key));
			}

		if (method.equalsIgnoreCase("POST") && parameters != null) {

			urlConnection.addRequestProperty("Content-Type",
					"application/x-www-form-urlencoded; charset=UTF-8");
			StringBuffer param = new StringBuffer();
			for (String key : parameters.keySet()) {
				param.append("&");
				param.append(key).append("=")
						.append(String.valueOf(parameters.get(key)));
			}
			OutputStreamWriter outer = new OutputStreamWriter(
					urlConnection.getOutputStream(), "utf-8");
			outer.write(param.toString());
			outer.flush();
			outer.close();
		}

		return this.makeContent(urlString, urlConnection);
	}

	/**
	 * 得到响应对象
	 * 
	 * @param urlConnection
	 * @return 响应对象
	 * @throws IOException
	 */
	private ServerInfo makeContent(String urlString,
			HttpURLConnection urlConnection) throws IOException {
		ServerInfo httpResponser = new ServerInfo();
		try {
			String ecod = urlConnection.getContentEncoding();
			if (ecod == null) {
				ecod = this.defaultContentEncoding;
			}

			InputStream in = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in, ecod));

			httpResponser.contentCollection = new Vector<String>();
			StringBuffer temp = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null) {
				httpResponser.contentCollection.add(line);
				temp.append(line).append("\r\n");
				line = bufferedReader.readLine();
			}
			bufferedReader.close();

			httpResponser.urlString = urlString;

			httpResponser.defaultPort = urlConnection.getURL().getDefaultPort();
			httpResponser.file = urlConnection.getURL().getFile();
			httpResponser.host = urlConnection.getURL().getHost();
			httpResponser.path = urlConnection.getURL().getPath();
			httpResponser.port = urlConnection.getURL().getPort();
			httpResponser.protocol = urlConnection.getURL().getProtocol();
			httpResponser.query = urlConnection.getURL().getQuery();
			httpResponser.ref = urlConnection.getURL().getRef();
			httpResponser.userInfo = urlConnection.getURL().getUserInfo();

			httpResponser.content = temp.toString();
			httpResponser.contentEncoding = ecod;
			httpResponser.code = urlConnection.getResponseCode();
			httpResponser.message = urlConnection.getResponseMessage();
			httpResponser.contentType = urlConnection.getContentType();
			httpResponser.method = urlConnection.getRequestMethod();
			httpResponser.connectTimeout = urlConnection.getConnectTimeout();
			httpResponser.readTimeout = urlConnection.getReadTimeout();

			return httpResponser;
		} catch (IOException e) {
			throw e;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}

	/**
	 * 默认的响应字符集
	 */
	public String getDefaultContentEncoding() {
		return this.defaultContentEncoding;
	}

	/**
	 * 设置默认的响应字符集
	 */
	public void setDefaultContentEncoding(String defaultContentEncoding) {
		this.defaultContentEncoding = defaultContentEncoding;
	}
}
