package com.example.comp90018_project.Util;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class LoadImageView extends AppCompatImageView {
    public static final int GET_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;

    private Bitmap bitmap;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_SUCCESS:
                    bitmap = (Bitmap) message.obj;
                    // onz what will this get called ?
                    bitmap = topSquareScale(bitmap);
                    setImageBitmap(bitmap);
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getContext(), "Network failure", Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(getContext(), "Server failure", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private Bitmap topSquareScale(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        Bitmap finalBitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        int length = 200;

        if (widthOrg > length || heightOrg > length) {
//            if (widthOrg > heightOrg) {
//                length = heightOrg;
//            }
//            else {
//                length = widthOrg;
//            }

            int xTopLeft = (widthOrg - length) / 2;
            int yTopLeft = (heightOrg - length) / 2;

            try{
                finalBitmap = Bitmap.createBitmap(bitmap, xTopLeft, yTopLeft, length, length);
            }
            catch(Exception e){
                return bitmap;
            }
            return finalBitmap;
        }
        return bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    // provide one constructor, should be enough ?
    public LoadImageView(Context context) {
        super(context);
    }

    public LoadImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public LoadImageView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    // use a new thread to load image from Internet
    public void loadImageFromURL(String path) {
        Bitmap bitmap = BitmapTransfer.convertStringToBitmap(path);
        bitmap = topSquareScale(bitmap);
        setImageBitmap(bitmap);
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    URL url = new URL(path);
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("GET");
//                    connection.setConnectTimeout(10000);
//                    int code = connection.getResponseCode();
//                    if (code == 200) {
//                        InputStream inputStream = connection.getInputStream();
//                        // decode the input stream to be the bitmap
//                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                        Message message = Message.obtain();
//                        message.obj = bitmap;
//                        message.what = GET_SUCCESS;
//                        handler.sendMessage(message);
//                        inputStream.close();
//                    } else {
//                        handler.sendEmptyMessage(SERVER_ERROR);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    handler.sendEmptyMessage(NETWORK_ERROR);
//                }
//            }
//        }.start();
    }
}
