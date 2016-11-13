package com.bignerdranch.android.kennel;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by Divya on 11/12/2016.
 */

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, Activity activity)
    {
        Point size =new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path,size.x,size.y);
    }

    public static Bitmap getScaledBitmap(String path,int destWidth,int destHeight)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 4;
        if (srcHeight > destHeight || srcWidth > destWidth)
        {
            if (srcWidth >srcHeight)
            {
                inSampleSize = Math.round(srcHeight/destHeight);
            }else {
                inSampleSize = Math.round(srcWidth / srcWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(path,options);
    }
}
