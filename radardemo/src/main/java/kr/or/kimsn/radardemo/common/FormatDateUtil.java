package kr.or.kimsn.radardemo.common;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FormatDateUtil {
  
  public static String formatTimestamp(String format, Timestamp time) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(time);
  }
  
  public static String formatTimestamp(Timestamp time) {
    return formatTimestamp("yyyy년 MM월 dd일 HH시 mm분 ss초", time);
  }
  
  public static String formatDate(String format, Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    try {
      return sdf.format(date);
    }
    catch (NullPointerException e) {
      return sdf.format(stringToDate("yyyy/MM/dd HH:mm:ss", "0001/01/01 00:00:00"));
    } 
  }
  
  public static String formatDate(Date date) {
    return formatDate("yyyy년 MM월 dd일", date);
  }
  
  public static Calendar getCalendar(int year, int month, int day, int hour, int minute, int second) {
    Calendar cal = Calendar.getInstance();
    cal.set(year, month - 1, day, hour, minute, second);
    return cal;
  }
  
  public static int getLastDayOfMonth(String datestr) {
    if (datestr.length() > 6) {
      datestr = datestr.substring(0, 6);
    }
    if (!isNumberFormatString(datestr, "yyyymm")) {
      return -1;
    }
    int year = Integer.parseInt(datestr.substring(0, 4));
    int month = Integer.parseInt(datestr.substring(4));
    Calendar cal = getCalendar(year, month, 1, 1, 1, 1);
    return cal.getActualMaximum(5);
  }
  
  public static Date stringToDate(String dateStr) {
    if (dateStr == null || dateStr.length() != 8) return null; 
    int date = Integer.parseInt(dateStr);
    int year = date / 10000;
    int month = date % 10000 / 100;
    int day = date % 100;
    return new Date(getCalendar(year, month, day, 0, 0, 0).getTimeInMillis());
  }
  
  public static java.util.Date stringToDate(String format, String dateStr) {
    if (dateStr == null) {
      return null;
    }
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    try {
      return sdf.parse(dateStr);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static java.util.Date changeKstToUtc(Date kst) {
    try {
      Date date = new Date(kst.getTime() - 32400000L);
      return date;
    }
    catch (NullPointerException e) {
      return stringToDate("yyyy/MM/dd HH:mm:ss", "0001/01/01 00:00:00");
    } 
  }
  
  public static java.util.Date changeUtcToKst(Date utc) {
    try {
      Date date = new Date(utc.getTime() + 32400000L);
      return date;
    }
    catch (NullPointerException e) {
      return stringToDate("yyyy/MM/dd HH:mm:ss", "0001/01/01 09:00:00");
    } 
  }
  
  public static boolean isNumberFormatString(String numstr, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    try {
      sdf.parse(numstr);
    } catch (ParseException e) {
      return false;
    } 
    return true;
  }
  
  public static Date sumMinutes(Date date, int m) {
    Date d = new Date(date.getTime() + (m * 60 * 1000));
    return d;
  }
  
  public static Date sumDay(Date date, int day) {
    Date d = new Date(date.getTime() + (day * 24 * 60 * 60 * 1000));
    return d;
  }
  
  public static java.util.Date sumMonth(Date date, int month) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(2, month);
    return cal.getTime();
  }
}

