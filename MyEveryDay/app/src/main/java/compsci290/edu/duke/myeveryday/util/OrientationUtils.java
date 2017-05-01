package compsci290.edu.duke.myeveryday.Util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Created by pallavishankar on 4/30/17.
 */

public class OrientationUtils {

    public static void lockOrientation(Activity mActivity) {
        if(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static void unlockOrientation(Activity mActivity) {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
