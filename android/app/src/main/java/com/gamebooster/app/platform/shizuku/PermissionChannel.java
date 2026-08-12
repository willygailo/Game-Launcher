package com.gamebooster.app.platform.shizuku;

import android.content.Context;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

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
