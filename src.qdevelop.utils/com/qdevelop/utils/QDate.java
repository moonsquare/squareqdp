package com.qdevelop.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked","rawtypes"})
public class QDate {

	private static Map<String,SimpleDateFormat> formatCache = new HashMap();
	private static SimpleDateFormat _defaultFormat = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * 根据格式化的字符串获取日期格式化工具 
	 * @param format
	 * @return
	 */
	public static SimpleDateFormat getSimpleDateFormat(String format){
		SimpleDateFormat tmp = formatCache.get(format);
		if(tmp!=null){
			return tmp;
		}
		tmp = new SimpleDateFormat(format);
		formatCache.put(format, tmp);
		return tmp;
	}

	public static Date parseDate(String date,String format) throws ParseException{
		return getSimpleDateFormat(format).parse(date);
	}
	
	public static String parseStr(Date date,String format){
		return getSimpleDateFormat(format).format(date);
	}
	
	public static String parseStr(java.sql.Date date,String format){
		return getSimpleDateFormat(format).format(date);
	}
	
	public static Date parseDateAuto(String date) throws ParseException{
		if(full_date_time.matcher(date).find()){
			return parseDate(date,"yyyy-MM-dd HH:mm:ss");
		}else if(full_date.matcher(date).find()){
			return parseDate(date,"yyyy-MM-dd");
		}else if(full_date_time1.matcher(date).find()){
			return parseDate(date,"yyyy/MM/dd HH:mm:ss");
		}else if(full_date1.matcher(date).find()){
			return parseDate(date,"yyyy/MM/dd");
		}
		throw new ParseException("No find parse Date style",1);
	}

	public static Date parseDate(String date) throws ParseException{
		return _defaultFormat.parse(date);
	}
	
	public static String getNow(String format){
		return getSimpleDateFormat(format).format(new Date());
	}

	public static int getDisBetweenDate(Date date1,Date date2){
		GregorianCalendar c1 = new GregorianCalendar();
		c1.setTime((date1));
		GregorianCalendar c2 = new GregorianCalendar();
		c2.setTime((date2));
		return (int)((c2.getTimeInMillis() - c1.getTimeInMillis())/(1000*60*60*24));
	}
	
	public static int getDisBetweenDate(String date1,String date2,String format)throws ParseException{
		return getDisBetweenDate(getSimpleDateFormat(format).parse(date1),getSimpleDateFormat(format).parse(date2));
	}
	
	private static Pattern full_date_time = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
	private static Pattern full_date = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	private static Pattern full_date_time1 = Pattern.compile("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}");
	private static Pattern full_date1 = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");
	
	
	
//	public static Date getDate(int dis){
//		GregorianCalendar today = new GregorianCalendar();
//		today.add(field, amount);
//	}
	
	public static List<Date> getDateList(String date1,String date2,String format)throws ParseException{
		List<Date> tmp = new ArrayList();
		GregorianCalendar c1 = new GregorianCalendar();
		c1.setTime(getSimpleDateFormat(format).parse(date1));
		GregorianCalendar c2 = new GregorianCalendar();
		c2.setTime(getSimpleDateFormat(format).parse(date2));
		while(c1.before(c2)){
			c1.add(Calendar.DAY_OF_YEAR, 1);
			tmp.add(c1.getTime());
		}
		return tmp;
	}
	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
//		java.sql.Date
//		SimpleDateFormat _format = QDate.getSimpleDateFormat("yyyy/MM/dd");
		System.out.println((QDate.getDisBetweenDate("2001-02-02","2001-03-01","yyyy-MM-dd")));
	}

}
