package com.qdevelop.utils.files;

import java.io.File;
import java.util.ArrayList;

import com.qdevelop.utils.QFile;

@SuppressWarnings({"unchecked","rawtypes"})
public class FileFormatEncode{
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/**文件源编码，设为NULL时，程序自动判断编码**/
		final String sourceEncode = "GBK";
		
		/**文件目标编码**/
		final String targetEncode = "UTF-8";
		
		/**目标文件OR文件夹**/
		final String sourceFile = "E:\\workspace\\OA\\src\\sqlConfig";
		
		/**文件过滤**/
		final String filter = "*.xml";
		
		new QFile(){
			String tmpEncode;
			private ArrayList<String> tmpFileContent;
			
			@SuppressWarnings("unused")
			public void disposeFile(File f) {
				try {
					tmpFileContent = new ArrayList();
					if(sourceEncode==null){
//						tmpEncode = FileEncodeJudge.getInstance().getFileEncode(f);
					}else
						tmpEncode = sourceEncode;
					/**本身编码相同不处理**/
					if(tmpEncode.equals(targetEncode)) return;
					
					readFile(f.getAbsolutePath(), tmpEncode);
					writeFile(f.getAbsolutePath(), tmpFileContent, targetEncode);
					
					System.out.println("["+tmpEncode+" --> "+targetEncode+"]:"+f.getAbsolutePath());
					
					tmpFileContent.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void disposeFileDate(String data) {
				tmpFileContent.add(new StringBuffer().append(data).append("\r\n").toString());
			}
			
			public void disposeFileDirectory(File f) {
			}
		}.listFiles(sourceFile, filter);
	}
}
