package com.mr47.screenshot_ocr.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public abstract class TimeUtil {
    private static SimpleDateFormat sf;

    @NotNull
    public static String getCurrentDate(boolean isTimeofHour) {
        return getDateFromTimeStamp(null, isTimeofHour);
    }

    public static String getDateFromTimeStamp(Long timeStamp, boolean isTimeofHour){
        Date d;
        if(timeStamp == null) d = new Date();
        else d = new Date(timeStamp);

        if(isTimeofHour){
            //格式为："yyyy-MM-dd HH:mm:ss
            sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }else{
            //格式为："yyyy-MM-dd"
            sf = new SimpleDateFormat("yyyy-MM-dd");
        }
        return sf.format(d);
    }

    public static long getTimeStampFromDate(String date){
        if(date.contains(" ")){
            sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }else {
            sf = new SimpleDateFormat("yyyy-MM-dd");
        }

        long timeStamp = 0;
        try {
            timeStamp = sf.parse(date).getTime();
        } catch (ParseException e) {
            log.error("parse date error", e.getCause());
        }
        return timeStamp;
    }
}
