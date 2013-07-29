package com.qdevelop.core.standard;

import java.io.InputStream;

public interface IDownLoadResult {
	/**
	 * 自定义输出文件流内容<br>
	 * 
	 * @return
	 */
	public InputStream getDownloadFile();
}
