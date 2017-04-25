package compsci290.edu.duke.myeveryday.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import compsci290.edu.duke.myeveryday.R;

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
                Constants.MIME_TYPE_IMAGE_EXT,
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
