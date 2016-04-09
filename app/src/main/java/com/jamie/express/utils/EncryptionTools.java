package com.jamie.express.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jamie on 2016/3/16.
 */
public class EncryptionTools {

    private static StringBuffer getSHA1(String string) {
        StringBuffer stringBuffer = new StringBuffer();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(string.getBytes(StandardCharsets.UTF_8));
        byte[] result = messageDigest.digest();
        for (byte b : result) {
            int i = b & 0xff;
            if (i < 0xf) {
                stringBuffer.append(0);
            }
            stringBuffer.append(Integer.toHexString(i));
        }
        return stringBuffer;
    }

    public static String SHA1LowerCase(String string) {
        return getSHA1(string).toString().toLowerCase();
    }

    public static String SHA1UpperCase(String string) {
        return getSHA1(string).toString().toUpperCase();
    }

}
