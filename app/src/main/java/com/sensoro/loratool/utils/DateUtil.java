package com.sensoro.loratool.utils;

import android.content.Context;
import android.text.TextUtils;

import com.sensoro.loratool.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bruceli on 2015/6/16.
 */
public class DateUtil {
    /*
 * 将时间戳转为字符串 ，格式：yyyy-MM-dd HH:mm
 */
    public static String getStrTime_ymd_hm(String cc_time) {
        String re_StrTime = "";
        if (TextUtils.isEmpty(cc_time) || "null".equals(cc_time)) {
            return re_StrTime;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;

    }

    /*
     * 将时间戳转为字符串 ，格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getStrTime_ymd_hms(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;

    }

    /*
     * 将时间戳转为字符串 ，格式：yyyy.MM.dd
     */
    public static String getStrTime_ymd(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    /*
     * 将时间戳转为字符串 ，格式：yyyy
     */
    public static String getStrTime_y(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    /*
     * 将时间戳转为字符串 ，格式：MM-dd
     */
    public static String getStrTime_md(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    /*
     * 将时间戳转为字符串 ，格式：HH:mm
     */
    public static String getStrTime_hm(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    /*
     * 将时间戳转为字符串 ，格式：HH:mm:ss
     */
    public static String getStrTime_hms(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    /*
     * 将时间戳转为字符串 ，格式：MM-dd HH:mm:ss
     */
    public static String getNewsDetailsDate(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    /*
     * 将字符串转为时间戳
     */
    public static String getTime() {
        String re_time = null;
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        Date d;
        d = new Date(currentTime);
        long l = d.getTime();
        String str = String.valueOf(l);
        re_time = str.substring(0, 10);
        return re_time;
    }

    /*
     * 将时间戳转为字符串 ，格式：yyyy.MM.dd  星期几
     */
    public static String getSection(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd  EEEE");
        // 对于创建SimpleDateFormat传入的参数：EEEE代表星期，如“星期四”；MMMM代表中文月份，如“十一月”；MM代表月份，如“11”；
        // yyyy代表年份，如“2010”；dd代表天，如“25”
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    //yyyy-MM-dd
    public static String getTodayDate(String f) {
        return new SimpleDateFormat(f, Locale.ROOT).format(new Date());
    }

    public static Date parseDateTime(String text, String... format) {
        String dateFormat = (format.length == 0 || format[0] == null) ? "yyyy-MM-dd HH:mm:ss" : format[0];
        try {
            if (text == null) return null;
            return new SimpleDateFormat(dateFormat).parse(text);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getDateDiffWithFormat(Context context, long pubTime, String format) {
        if (pubTime == 0) {
            return "-";
        }
        long diff = System.currentTimeMillis() - pubTime;
        long hour = diff / 3600000;
        long day  = diff / 3600000 / 24;
        long year = diff / 3600000 / 24 / 365;
        if (day < 365) {
            if (hour < 1) {
                long min = diff / 60000;
                if (min > 0) {
                    return min + context.getString(R.string.min_ago);
                }
                return context.getString(R.string.now);
            } else if (hour < 24) {
                return hour + context.getString(R.string.h_ago);
            } else {
                return day + context.getString(R.string.day_ago);
            }
        } else {
            return year + context.getString(R.string.year_ago);
        }

    }

    public static String getDate(long time) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date(time));
    }

    public static String getFullDate(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date(time));
    }
}
