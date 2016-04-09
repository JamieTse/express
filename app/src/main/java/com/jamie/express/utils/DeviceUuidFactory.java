package com.jamie.express.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Created by jamie on 2016/3/16.
 */
public class DeviceUuidFactory {

    protected static final String PREFS_FILE = "device_id";
    protected static final String PREFS_DEVICE_ID = "device_id";
    protected static UUID uuid;

    public DeviceUuidFactory(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuidFactory.class) {
                if (uuid == null) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFS_FILE, 0);
                    final String id = preferences.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        uuid = UUID.fromString(id);
                    } else {
                        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        if (androidId != null && !"9774d56d682e549c".equals(androidId)) {
                            uuid = UUID.nameUUIDFromBytes(androidId.getBytes(StandardCharsets.UTF_8));
                        } else {
                            final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                            if (deviceId == null || deviceId.equals("000000000000000")) {
                                uuid = UUID.randomUUID();
                            } else {
                                uuid = UUID.nameUUIDFromBytes(deviceId.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        preferences.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
                    }
                }
            }
        }
    }

    /**
     * 返回一个在当前安卓设备中无限趋近于唯一的UUID
     * @return
     */
    public UUID getDeviceUuid() {
        return uuid;
    }

}
