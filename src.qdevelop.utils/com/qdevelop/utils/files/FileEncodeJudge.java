package com.qdevelop.utils.files;


/**
 * 查看文件编码
 * @author Janson.Gu
 *
 */
public class FileEncodeJudge {
//	private static FileEncodeJudge _FileEncodeJudge = new FileEncodeJudge();
//	private CodepageDetectorProxy detector;
//
//	public FileEncodeJudge() {
//		if (detector == null) {
//			detector = CodepageDetectorProxy.getInstance();
//			/*------------------------------------------------------------------------- 
//			 * 	detector是探测器，它把探测任务交给具体的探测实现类的实例完成。 
//			 * 	cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法加进来，如ParsingDetector、 JChardetFacade、ASCIIDetector、UnicodeDetector。   
//			 * 	detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的字符集编码。
//			 */
//			detector.add(new ParsingDetector(false));
//			/*-------------------------------------------------------------------------- 
//			  	ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于 
//				指示是否显示探测过程的详细信息，为false不显示。
//				JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码 
//				  测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以 
//				  再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。  
//			 ---------------------------------------------------------------------------*/
//			detector.add(JChardetFacade.getInstance());
//			detector.add(UnicodeDetector.getInstance());
//			detector.add(ASCIIDetector.getInstance());
//			
//		}
//	}
//
//	public static FileEncodeJudge getInstance() {
//		return _FileEncodeJudge;
//	}
//
//	@SuppressWarnings("deprecation")
//	public final String getFileEncode(File f) {
//		Charset charset = null;
//		try {
//			charset = detector.detectCodepage(f.toURL());
//			if (charset != null)
//				return charset.name();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return null;
//	}
//
//	/**
//	 * 
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		new QFile(){
//			String encode;
//			public void disposeFile(File f) {
//				try {
//					encode = FileEncodeJudge.getInstance().getFileEncode(f);
//					if(!encode.equals("UTF-8"))
//						System.out.println(f.getAbsolutePath()+":\t"+encode);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			public void disposeFileDate(String data) {
//			}
//			public void disposeFileDirectory(File f) {
//			}
//			//!*.svn|!*.jar|*.class|!*.swf|!*.png|!*.gif
//		}.listFiles("E:/workspace/trafficAnalysis/WebRoot/QDevelop", "*.js");
//		//		System.out.println(Charset.defaultCharset());
//	}
}
