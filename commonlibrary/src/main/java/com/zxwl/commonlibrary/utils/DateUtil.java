package com.zxwl.commonlibrary.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by hcc on 2016/10/11 16:30
 * 100332338@qq.com
 * <p>
 * 时间工具类
 */

@SuppressLint("SimpleDateFormat")
public class DateUtil {
    public final static String FORMAT_YEAR = "yyyy";
    public final static String FORMAT_MONTH_DAY = "MM月dd日";
    public final static String FORMAT_DATE = "yyyy-MM-dd";
    public final static String FORMAT_TIME = "HH:mm";
    public final static String FORMAT_MONTH_DAY_TIME = "MM月dd日  hh:mm";
    public final static String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";
    public final static String CHAT_TIME = "mm:ss";// 显示聊天记录的格式
    public final static String FORMAT_DATE1_TIME = "yyyy/MM/dd HH:mm";
    public final static String FORMAT_DATE_TIME_SECOND = "yyyy/MM/dd HH:mm:ss";
    private static SimpleDateFormat sdf = new SimpleDateFormat();
    private static final int YEAR = 365 * 24 * 60 * 60;// 年
    private static final int MONTH = 30 * 24 * 60 * 60;// 月
    private static final int DAY = 24 * 60 * 60;// 天
    private static final int HOUR = 60 * 60;// 小时
    private static final int MINUTE = 60;// 分钟


    /**
     * 根据时间戳获取描述性时间，如3分钟前，1天前
     *
     * @param timestamp 时间戳 单位为毫秒
     * @return 时间字符串
     */
    public static String getDescriptionTimeFromTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        // 与现在时间相差秒数
        long timeGap = (currentTime - timestamp) / 1000;
        System.out.println("timeGap: " + timeGap);
        String timeStr;
        if (timeGap > YEAR) {
            timeStr = timeGap / YEAR + "年前";
        } else if (timeGap > MONTH) {
            timeStr = timeGap / MONTH + "个月前";
        } else if (timeGap > DAY) {// 1天以上
            timeStr = timeGap / DAY + "天前";
        } else if (timeGap > HOUR) {// 1小时-24小时
            timeStr = timeGap / HOUR + "小时前";
        } else if (timeGap > MINUTE) {// 1分钟-59分钟
            timeStr = timeGap / MINUTE + "分钟前";
        } else {// 1秒钟-59秒钟
            timeStr = "刚刚";
        }
        return timeStr;
    }


    /**
     * 获取当前日期的指定格式的字符串
     *
     * @param format 指定的日期时间格式，若为null或""则使用指定的格式"yyyy-MM-dd HH:MM"
     */

    public static String getCurrentTime(String format) {
        if (format == null || format.trim().equals("")) {
            sdf.applyPattern(FORMAT_DATE_TIME);
        } else {
            sdf.applyPattern(format);
        }
        return sdf.format(new Date());
    }

    /**
     * date类型转换为String类型
     * formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
     * data Date类型的时间
     */
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    /**
     * long类型转换为String类型
     * currentTime要转换的long类型的时间
     * formatType要转换的string类型的时间格式
     */

    public static String longToString(long currentTime, String formatType) {
        String strTime;
        // long类型转成Date类型
        Date date = longToDate(currentTime, formatType);
        // date类型转成String
        strTime = dateToString(date, formatType);
        return strTime;
    }

    /**
     * string类型转换为date类型
     * strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
     * HH时mm分ss秒，
     * strTime的时间格式必须要与formatType的时间格式相同
     */

    public static Date stringToDate(String strTime, String formatType) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        try {
            date = formatter.parse(strTime);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    /**
     * long转换为Date类型
     * currentTime要转换的long类型的时间
     * formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
     */

    public static Date longToDate(long currentTime, String formatType) {
        // 根据long类型的毫秒数生命一个date类型的时间
        Date dateOld = new Date(currentTime);
        // 把date类型的时间转换为string
        String sDateTime = dateToString(dateOld, formatType);
        // 把String类型转换为Date类型
        Date date = stringToDate(sDateTime, formatType);
        return date;
    }

    /**
     * string类型转换为long类型
     * strTime要转换的String类型的时间
     * formatType时间格式
     * strTime的时间格式和formatType的时间格式必须相同
     */
    public static long stringToLong(String strTime, String formatType) {
        // String类型转成date类型
        Date date = stringToDate(strTime, formatType);
        if (date == null) {
            return 0;
        } else {
            // date类型转成long类型
            return dateToLong(date);
        }
    }

    /**
     * date类型转换为long类型
     * date要转换的date类型的时间
     */

    public static long dateToLong(Date date) {
        return date.getTime();
    }


    /**
     * * 返回指定pattern样的日期时间字符串。
     * <p>
     * *
     * <p>
     * * @param dt
     * <p>
     * * @param pattern
     * <p>
     * * @return 如果时间转换成功则返回结果，否则返回空字符串""
     * <p>
     * * @author 即时通讯网([url=http://www.52im.net]http://www.52im.net[/url])
     * <p>
     */

    public static String getTimeString(Date dt, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);//"yyyy-MM-dd HH:mm:ss"
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(dt);
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 仿照微信中的消息时间显示逻辑，将时间戳（单位：毫秒）转换为友好的显示格式.
     *
     * <p>
     * <p>
     * 1）7天之内的日期显示逻辑是：今天、昨天(-1d)、前天(-2d)、星期？（只显示总计7天之内的星期数，即<=-4d）；<br>
     * <p>
     * 2）7天之外（即>7天）的逻辑：直接显示完整日期时间。
     *
     * @param srcDate         要处理的源日期时间对象
     * @param mustIncludeTime true表示输出的格式里一定会包含“时间:分钟”，否则不包含（参考微信，不包含时分的情况，用于首页“消息”中显示时）
     * @return 输出格式形如：“10:30”、“昨天 12:04”、“前天 20:51”、“星期二”、“2019/2/21 12:09”等形式
     * @author 即时通讯网([url = http : / / www.52im.net]http : / / www.52im.net[ / url])
     * @since 4.5
     */
    public static String getTimeStringAutoShort2(Date srcDate, boolean mustIncludeTime) {
        String ret = "";

        try {
            GregorianCalendar gcCurrent = new GregorianCalendar();
            gcCurrent.setTime(new Date());
            int currentYear = gcCurrent.get(GregorianCalendar.YEAR);
            int currentMonth = gcCurrent.get(GregorianCalendar.MONTH) + 1;
            int currentDay = gcCurrent.get(GregorianCalendar.DAY_OF_MONTH);

            GregorianCalendar gcSrc = new GregorianCalendar();
            gcSrc.setTime(srcDate);
            int srcYear = gcSrc.get(GregorianCalendar.YEAR);
            int srcMonth = gcSrc.get(GregorianCalendar.MONTH) + 1;
            int srcDay = gcSrc.get(GregorianCalendar.DAY_OF_MONTH);

            // 要额外显示的时间分钟
            String timeExtraStr = (mustIncludeTime ? " " + getTimeString(srcDate, "HH:mm") : "");
            // 当年
            if (currentYear == srcYear) {
                long currentTimestamp = gcCurrent.getTimeInMillis();
                long srcTimestamp = gcSrc.getTimeInMillis();
                // 相差时间（单位：毫秒）
                long delta = (currentTimestamp - srcTimestamp);

                // 当天（月份和日期一致才是）
                if (currentMonth == srcMonth && currentDay == srcDay) {
                    // 时间相差60秒以内
                    if (delta < 60 * 1000)
                        ret = "刚刚";
                        // 否则当天其它时间段的，直接显示“时:分”的形式
                    else
                        ret = getTimeString(srcDate, "HH:mm");
                }
                // 当年 && 当天之外的时间（即昨天及以前的时间）
                else {
                    // 昨天（以“现在”的时候为基准-1天）
                    GregorianCalendar yesterdayDate = new GregorianCalendar();
                    yesterdayDate.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    // 前天（以“现在”的时候为基准-2天）
                    GregorianCalendar beforeYesterdayDate = new GregorianCalendar();
                    beforeYesterdayDate.add(GregorianCalendar.DAY_OF_MONTH, -2);
                    // 用目标日期的“月”和“天”跟上方计算出来的“昨天”进行比较，是最为准确的（如果用时间戳差值

                    // 的形式，是不准确的，比如：现在时刻是2019年02月22日1:00、而srcDate是2019年02月21日23:00，

                    // 这两者间只相差2小时，直接用“delta/(3600 * 1000)” > 24小时来判断是否昨天，就完全是扯蛋的逻辑了）
                    if (srcMonth == (yesterdayDate.get(GregorianCalendar.MONTH) + 1) && srcDay == yesterdayDate.get(GregorianCalendar.DAY_OF_MONTH)) {
                        ret = "昨天" + timeExtraStr;// -1d
                    }
                    // “前天”判断逻辑同上
                    else if (srcMonth == (beforeYesterdayDate.get(GregorianCalendar.MONTH) + 1) && srcDay == beforeYesterdayDate.get(GregorianCalendar.DAY_OF_MONTH)) {
                        ret = "前天" + timeExtraStr;// -2d
                    } else {
                        // 跟当前时间相差的小时数
                        long deltaHour = (delta / (3600 * 1000));
                        // 如果小于 7*24小时就显示星期几
                        if (deltaHour < 7 * 24) {
                            String[] weekday = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
                            // 取出当前是星期几
                            String weedayDesc = weekday[gcSrc.get(GregorianCalendar.DAY_OF_WEEK) - 1];
                            ret = weedayDesc + timeExtraStr;
                        }
                        // 否则直接显示完整日期时间
                        else
                            ret = getTimeString(srcDate, "yyyy/M/d") + timeExtraStr;
                    }
                }

            } else
                ret = getTimeString(srcDate, "yyyy/M/d") + timeExtraStr;
        } catch (Exception e) {
            System.err.println("【DEBUG-getTimeStringAutoShort】计算出错：" + e.getMessage() + " 【NO】");
        }
        return ret;

    }
}
