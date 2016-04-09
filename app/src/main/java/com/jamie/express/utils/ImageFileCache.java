package com.jamie.express.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by jamie on 2016/3/15.
 */
public class ImageFileCache {
    public static final String TAG = "ImageFileCache";

    private static String IMGCACHEDIR = Environment.getExternalStorageDirectory() + "/CacheExpress/ImageCache";
    private static final String CACHETAIL = ".cache";
    private static final int MB = 1024 * 1024;
    private static final int CACHE_SIZE = 5;
    private static final int NEEDED_FREE_SPACE_WHILE_LEFT = 10;

    //private StackTraceElement stackTraceElement;

    public ImageFileCache() {
        manageCache(IMGCACHEDIR);
    }

    /**
     * 从图片缓存文件夹中取出图片文件
     *
     * @param url
     * @return
     */
    public Bitmap getImageFromFile(final String url) {
        final String path = IMGCACHEDIR + "/" + convertURLToFileName(url);
        File file = new File(path);
        if (file != null && file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null) {
                file.delete();
            } else {
                updateFileTime(path);
                return bitmap;
            }
        }
        return null;
    }

    /**
     * 将Bitmap类型的文件压缩存入缓存文件夹
     *
     * @param bitmap
     * @param url
     * @return
     */
    public boolean saveBitmapToFile(Bitmap bitmap, String url) {
        if (bitmap == null) {
            Log.i(TAG, "Bitmap parameter null.");
            return false;
        }
        if (NEEDED_FREE_SPACE_WHILE_LEFT > SDCardFreeSpace()) {
            Log.i(TAG, "Not enough free space in SD Card.");
            return false;
        }
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.i(TAG, "Not allowed to used the SD Card storage.");
            return false;
        }
        String fileName = convertURLToFileName(url);
        File dirFile = new File(IMGCACHEDIR);
        if (!dirFile.exists()) {
            Log.i(TAG, "making dirs.");
            dirFile.mkdirs();
        }
        File file = new File(IMGCACHEDIR + "/" + fileName);
        try {
            file.createNewFile();
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            Log.i(TAG, "IOException");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 管理缓存文件夹，如果缓存大于规定大小或SD卡剩余空间过小则清除部分缓存
     *
     * @param dirPath
     * @return
     */
    private boolean manageCache(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (!android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return false;
        if (files == null)
            return true;
        int usedSize = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(CACHETAIL))
                usedSize += files[i].length();
        }
        if (usedSize > CACHE_SIZE * MB || NEEDED_FREE_SPACE_WHILE_LEFT > SDCardFreeSpace()) {
            int removeFactor = (int) (0.4 * files.length);
            Arrays.sort(files, new FileLastModify());
            for (int i = 0; i < removeFactor; i++) {
                if (files[i].getName().contains(CACHETAIL))
                    files[i].delete();
            }
        }
        if (SDCardFreeSpace() <= CACHE_SIZE)
            return false;
        return true;
    }

    /**
     * 更新最后修改、使用时间
     *
     * @param path
     */
    public void updateFileTime(String path) {
        File file = new File(path);
        long newModifydTime = System.currentTimeMillis();
        file.setLastModified(newModifydTime);
    }

    /**
     * 计算SD卡剩余空间
     *
     * @return
     */
    private int SDCardFreeSpace() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double SDFreeMB = ((double) statFs.getAvailableBlocksLong() * (double) statFs.getBlockSizeLong());
        return (int) SDFreeMB;
    }

    /**
     * 取URL的哈希值，加上缓存后缀
     *
     * @param url
     * @return
     */
    private String convertURLToFileName(String url) {
        return url.hashCode() + CACHETAIL;
    }

    /**
     * 根据文件最后使用时间对文件排序的排序方法
     */
    private class FileLastModify implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.lastModified() > rhs.lastModified()) {
                return 1;
            } else if (lhs.lastModified() == rhs.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

}
