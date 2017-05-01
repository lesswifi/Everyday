package compsci290.edu.duke.myeveryday.util;

import android.content.Context;
import android.os.Environment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

/**
 * Created by pallavishankar on 4/20/17.
 */

public class CameraHelper {

    public static File createImageFile(Context mContext) throws IOException {
        String timeStamp = TimeUtils.getDatetimeSuffix(System.currentTimeMillis());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    public static void displayImageInView(Context context, String imagePath, ImageView image) {
        Glide.with(context)
                .load(imagePath)
                .fitCenter()
                .into(image);
    }

}
