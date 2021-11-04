package com.example.comp90018_project.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapTransfer {

    public static String convertBitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);
    }

    public static Bitmap convertStringToBitmap(String st) {

            Bitmap bitmap = null;
            try {
                // out = new FileOutputStream("/sdcard/aa.jpg");
                byte[] bitmapArray;
                bitmapArray = Base64.decode(st, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                bitmapArray.length);
                // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                return bitmap;
            }
            catch (Exception e){
                return null;
            }
        }
}