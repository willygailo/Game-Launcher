package com.gamebooster.app.games;
import com.gamebooster.app.config.*;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class GameAppInfo {
    private final String label;
    private final String packageName;
    private final Drawable icon;
    private final Intent launchIntent;

    public GameAppInfo(String label, String packageName, Drawable icon, Intent launchIntent) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
        this.launchIntent = launchIntent;
    }

    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Intent getLaunchIntent() {
        return launchIntent;
    }
}
