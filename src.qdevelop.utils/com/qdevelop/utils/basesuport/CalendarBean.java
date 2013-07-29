package com.qdevelop.utils.basesuport;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
/**
 * 
 * 功能：对日期的操作的一个Bean，<br>
 *      主要是方便字符串转日期的一个操作，通过此Bean的转换后获取其各项属性值
 * 
 * @author 顾杰勇
 * @version 1.0
 * @serialData 2008-6-12 11:20:50
 * 
 * 修改人：
 * 修改时间：
 * 修改内容：
 * 修改原因：
 *
 */
public class CalendarBean {

	private int year = -1;
	private int month = -1;
	private int day = -1;
	private int hour = -1;
	private int minite = -1;
	private int second = -1;


	public CalendarBean(){}

	/**
	 * 构造日期Bean，以字符串形式传如，分隔成年、月、日<br>
	 * 传如参数示例：2008-08-08 08:08:08 即2008年8月8日上午8点8分8秒<br>
	 * 不传时间就直接传入 2008-08-08
	 * @param date
	 */
	public CalendarBean(String date){
		if(date!=null){
			String[] tmp = date.replaceAll(" +", " ").replaceAll("^ | $", "").replaceAll("-|/|:| ", "#").split("#");
			year = formatTime(tmp,0);
			month = formatTime(tmp,1);
			day = formatTime(tmp,2);
			hour = formatTime(tmp,3);
			minite = formatTime(tmp,4);
			second = formatTime(tmp,5);
		}
	}
	
	private int formatTime(String[] p,int idx){
		if(p==null || idx >= p.length) return 0;
		return Integer.parseInt(p[idx]);
	}

	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getWeek() {
		return getGregorianCalendar().get(Calendar.DAY_OF_WEEK);
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinite() {
		return minite;
	}
	public void setMinite(int minite) {
		this.minite = minite;
	}
	public int getSecond() {
		return second;
	}
	public void setSecond(int second) {
		this.second = second;
	}
	private GregorianCalendar getGregorianCalendar(){
		if(year>-1){
			return new GregorianCalendar(year,month-1,day,hour,minite,second);
		}else{
			return new GregorianCalendar();
		}

	}

	/**
	 * 获取日期
	 * @return Date
	 */
	public Date toDate(){
		return getGregorianCalendar().getTime();
	}



	/**
	 * 获取相差的年数的日期<br>
	 * 正数则为此日期的年数向后推yearDis年<br>
	 * 负数则为次日起的年数向前退yearDis年<br>
	 * @param yearDis 相差数
	 * @return Date
	 */
	public Date getDateByYearDis(int yearDis){
		GregorianCalendar gorianCalendar = getGregorianCalendar();
		gorianCalendar.add(Calendar.YEAR,yearDis);
		return gorianCalendar.getTime();
	}
	/**
	 * 获取相差的月份数的日期<br>
	 * 正数则为此日期的月份数向后推monthDis月<br>
	 * 负数则为次日起的月份数向前退monthDis月<br>
	 * @param monthDis 相差的月份数
	 * @return Date
	 */
	public Date getDateByMonthDis(int monthDis){
		GregorianCalendar gorianCalendar = getGregorianCalendar();
		gorianCalendar.add(Calendar.MONTH,monthDis);
		return gorianCalendar.getTime();
	}
	/**
	 * 获取相差的天数的日期<br>
	 * 正数则为此日期的天数向后推monthDis天<br>
	 * 负数则为次日起的天数向前退monthDis天<br>
	 * @param dayDis 相差的天数
	 * @return Date
	 */
	public Date getDateByDayDis(int dayDis){
		GregorianCalendar gorianCalendar = getGregorianCalendar();
		gorianCalendar.add(Calendar.DAY_OF_YEAR,dayDis);
		return gorianCalendar.getTime();
	}
}
