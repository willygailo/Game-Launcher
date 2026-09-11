package com.gamebooster.app.focus;

import android.graphics.drawable.Drawable;

public class FocusAppModel {
    public final String packageName;
    public final String appLabel;
    public final Drawable appIcon;
    public boolean isSelectedToFreeze;
    public boolean isCurrentlyFrozen;

    public FocusAppModel(String packageName, String appLabel, Drawable appIcon, boolean isSelectedToFreeze, boolean isCurrentlyFrozen) {
        this.packageName = packageName;
        this.appLabel = appLabel != null ? appLabel : packageName;
        this.appIcon = appIcon;
        this.isSelectedToFreeze = isSelectedToFreeze;
        this.isCurrentlyFrozen = isCurrentlyFrozen;
    }

    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase();
        return (appLabel != null && appLabel.toLowerCase().contains(q))
                || (packageName != null && packageName.toLowerCase().contains(q));
    }
}
