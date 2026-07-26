package com.gamespace.app.channels;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class GameLibraryChannel implements MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/game_library";
    private final Context context;
    private final MethodChannel channel;

    public GameLibraryChannel(BinaryMessenger messenger, Context context) {
        this.context = context;
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "getInstalledGames":
                result.success(getInstalledGames());
                break;
            case "launchGame":
                String pkg = call.argument("packageName");
                if (pkg != null) {
                    boolean success = launchGame(pkg);
                    result.success(success);
                } else {
                    result.error("INVALID_ARGUMENT", "Package name required", null);
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private List<Map<String, Object>> getInstalledGames() {
        List<Map<String, Object>> games = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            boolean isGame = false;
            if ((app.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                isGame = true;
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (app.category == ApplicationInfo.CATEGORY_GAME) {
                    isGame = true;
                }
            }

            if (isGame) {
                Map<String, Object> map = new HashMap<>();
                map.put("packageName", app.packageName);
                map.put("appName", pm.getApplicationLabel(app).toString());

                try {
                    Drawable icon = pm.getApplicationIcon(app);
                    map.put("iconBase64", drawableToBase64(icon));
                } catch (Exception e) {
                    map.put("iconBase64", "");
                }

                games.add(map);
            }
        }
        return games;
    }

    private boolean launchGame(String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String drawableToBase64(Drawable drawable) {
        try {
            Bitmap bitmap;
            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }
}
