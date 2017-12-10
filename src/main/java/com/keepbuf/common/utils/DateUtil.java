package com.keepbuf.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 *
 * @author huacy
 * @since 2017/12/03
 */
public class DateUtil {

    private static final int SECONDS_MS = 1000;

    private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static ThreadLocal<SimpleDateFormat> dateTimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * 获取当前时间 yyyy-MM-dd HH:mm:ss
     * @return 时间字符串
     */
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return dateTimeFormat.get().format(calendar.getTime());
    }

    /**
     * 获取当天日期YYYY-MM-DD
     * @return 时间字符串
     */
    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        return dateFormat.get().format(calendar.getTime());
    }

    /**
     * 秒 转换 标准时间格式(HH:mm:ss.SSS)
     * @param seconds 秒数字
     * @return 时间字符串
     */
    public static String secToHourFormat(double seconds) {
        // 对时区做相减转换
        double millisecond  = seconds * SECONDS_MS - TimeZone.getDefault().getRawOffset();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        return formatter.format(millisecond );
    }

    /**
     * 获取每隔5s的时间点: 11:05 11:10
     * @return 时间字符串
     */
    public static String getToday5SecondsDate() {
        Calendar calendar = Calendar.getInstance();
        int minute = getMinute(calendar.get(Calendar.MINUTE));
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        return dateTimeFormat.get().format(calendar.getTime());
    }

    /**
     * 根据分钟转换5分钟内的整数时间
     * @param minute 分钟
     * @return 0 or 5
     */
    private static int getMinute(int minute) {
        int result = 0;
        if (0 <= minute && minute < 5) {
            result = 0;
        } else if (5 <= minute && minute < 10) {
            result = 5;
        } else if (10 <= minute && minute < 15) {
            result = 10;
        } else if (15 <= minute && minute < 20) {
            result = 15;
        } else if (20 <= minute && minute < 25) {
            result = 20;
        } else if (25 <= minute && minute < 30) {
            result = 25;
        } else if (30 <= minute && minute < 35) {
            result = 30;
        } else if (35 <= minute && minute < 40) {
            result = 35;
        } else if (40 <= minute && minute < 45) {
            result = 40;
        } else if (45 <= minute && minute < 50) {
            result = 45;
        } else if (50 <= minute && minute < 55) {
            result = 50;
        } else if (55 <= minute && minute <= 59) {
            result = 55;
        }
        return result;
    }
}
