package com.gamebooster.app.focus;

import android.graphics.drawable.Drawable;

public class FocusAppModel {
    public final String packageName;
    public final String appLabel;
    public final Drawable appIcon;
    public boolean isWhitelisted; // If true, do not freeze this app

    public FocusAppModel(String packageName, String appLabel, Drawable appIcon, boolean isWhitelisted) {
        this.packageName = packageName;
        this.appLabel = appLabel;
        this.appIcon = appIcon;
        this.isWhitelisted = isWhitelisted;
    }
}
