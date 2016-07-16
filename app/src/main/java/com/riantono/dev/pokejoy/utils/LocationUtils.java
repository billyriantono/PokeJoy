package com.riantono.dev.pokejoy.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

/**
 * Created by billy on 7/16/16.
 */
public class LocationUtils {
    private static ApplicationInfo localApplicationInfo1;

    public static int setMockLocationSettings(Context context) {

        int value = 1;
        try {
            value = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION);
            if (isSystemApps(context)) {
                Settings.Secure.putInt(context.getContentResolver(),
                        Settings.Secure.ALLOW_MOCK_LOCATION, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;

    }

    public static void restoreMockLocationSettings(Context context, int restore_value) {
        try {
            if (isSystemApps(context)) {
                Settings.Secure.putInt(context.getContentResolver(),
                        Settings.Secure.ALLOW_MOCK_LOCATION, restore_value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSystemApps(Context paramContext) {
        String str = paramContext.getPackageName();
        PackageManager localPackageManager = paramContext.getPackageManager();
        try {
            ApplicationInfo localApplicationInfo2 = localPackageManager.getApplicationInfo("com.xbenjii.pokemock", PackageManager.GET_META_DATA);
            localApplicationInfo1 = localApplicationInfo2;
        } catch (Throwable localThrowable) {
            localApplicationInfo1 = null;
        }
        if (localApplicationInfo1 == null) {
            return false;
        }
        while ((!localApplicationInfo1.sourceDir.startsWith("/system/app/")) && (!localApplicationInfo1.sourceDir.startsWith("/system/priv-app/")) && (!isHasInstallLocationProvider(paramContext))) {
            return false;
        }
        return true;
    }

    public static boolean isHasInstallLocationProvider(Context paramContext) {
        return paramContext.checkCallingOrSelfPermission("android.permission.INSTALL_LOCATION_PROVIDER") == PackageManager.PERMISSION_GRANTED;
    }
}
