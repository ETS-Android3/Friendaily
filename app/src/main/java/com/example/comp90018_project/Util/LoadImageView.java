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
//                    bitmap = topSquareScale(bitmap);
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
        setImageBitmap(bitmap);
    }
}
