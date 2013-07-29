package web;


public class QD {
	public static String[] loopResource;
	public static int idx=0,loopSize;
	public static String version = "1.0";
	
	public static void init(String uri){
		if(uri!=null){
			QD.loopResource = uri.split("\\|");
			QD.loopSize = QD.loopResource.length;
		}
	}
	
	public static String getRes(){
		if(loopSize-idx>0)return loopResource[idx++];
		idx = 0 ;
		return loopResource[idx++];
	}
	
	public static String getRes(String res){
		return new StringBuffer().append(getRes()).append(res).append("?$v=").append(version).toString();
	}
//	public static void main(String[] args) {
//		URLDecoder.
//		System.out.println(URLDecoder.decode("/EnterpriseCard/QDevelop/themes/%7Cdefault%5Eeasyui.css").replaceAll("\\^","/"));
//	}
}
