package com.gamebooster.app.shizuku;

import android.content.Context;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class PermissionChannel {

    public static boolean hasShizuku() {
        return ShizukuExecutor.hasShizukuPermission();
    }

    public static void grantPermissionsViaShizuku(Context context) {
        if (hasShizuku()) {
            ShizukuExecutor.grantAppPermissionsViaShizuku(context);
        }
    }
}
