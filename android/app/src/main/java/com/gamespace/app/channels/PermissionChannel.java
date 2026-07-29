package com.gamespace.app.channels;

import android.content.Context;
import com.gamespace.app.utils.ShizukuExecutor;

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
