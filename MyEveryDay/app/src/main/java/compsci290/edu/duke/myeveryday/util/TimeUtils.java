package compsci290.edu.duke.myeveryday.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by FD on 4/18/2017.
 */

public class TimeUtils {

    public static String getReadableModifiedDate(long date){

        String displayDate = new SimpleDateFormat("MMM dd, yyyy, h:mm a").format(new Date(date));
        return displayDate;
    }

    public static String getReadableModifiedShortDate(long date){

        String displayDate = new SimpleDateFormat("MMM dd").format(new Date(date));
        return displayDate;
    }

    public static String getReadableModifiedTime(long date){
        String displayTime = new SimpleDateFormat("h:mm a").format(new Date(date));
        return displayTime;
    }

    public static String getDatetimeSuffix(long date){
        String timeStamp = new SimpleDateFormat("yyyy_MMM_dd_HH_mm").format(new Date(date));
        return timeStamp;
    }

    public static String getTime(int milliseconds) {
        int seconds = milliseconds/1000;
        String minutes = Integer.toString(seconds/60);
        String remainderSeconds = Integer.toString(seconds % 60);
        if (remainderSeconds.length() < 2) {
            remainderSeconds = "0" + remainderSeconds;
        }
        return minutes + ":" + remainderSeconds;
    }
}
