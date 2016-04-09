package com.jamie.express.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by jamie on 2016/3/18.
 */
public class ImageLoader {

    public static final String TAG = "ImageLoader";
    public static final int BUFFER_SIZE = 1024;

    public static void loadBitmap(final String imgPath, final BitmapCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                callback.onGotBitmap((Bitmap) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageFileCache imageFileCache = new ImageFileCache();
                Bitmap bitmap = imageFileCache.getImageFromFile(imgPath);
                if (bitmap == null) {
                    InputStream inputStream = null;
                    BufferedOutputStream outputStream = null;
                    try {
                        inputStream = new BufferedInputStream(new URL(imgPath).openStream(), BUFFER_SIZE);
                        final ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
                        outputStream = new BufferedOutputStream(imgOutput, BUFFER_SIZE);

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                        buffer = null;
                        outputStream.flush();

                        byte[] imgBytes = imgOutput.toByteArray();
                        bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                        imageFileCache.saveBitmapToFile(bitmap, imgPath);
                        imgBytes = null;
                        if (imgOutput != null) {
                            imgOutput.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);
            }
        }).start();
    }

    public static void loadDrawable(final String imgPath, final DrawableCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                callback.onGotDrawable((Drawable) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable drawable = Drawable.createFromStream(new URL(imgPath).openStream(), "picHead");
                    Message message = Message.obtain();
                    message.obj = drawable;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface BitmapCallback {
        public void onGotBitmap(Bitmap bitmap);
    }

    public interface DrawableCallback {
        public void onGotDrawable(Drawable drawable);
    }

}
