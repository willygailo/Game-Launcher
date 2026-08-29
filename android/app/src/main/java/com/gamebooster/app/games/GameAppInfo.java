package com.gamebooster.app.games;
import com.gamebooster.app.config.*;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class GameAppInfo {
    private final String label;
    private final String packageName;
    private final Drawable icon;
    private final Intent launchIntent;
    private final String gameType;
    private final int cardBgRes;
    private final int badgeColor;

    public GameAppInfo(String label, String packageName, Drawable icon, Intent launchIntent) {
        this(label, packageName, icon, launchIntent, "GAME", 0, 0000000);
    }

    public GameAppInfo(String label, String packageName, Drawable icon, Intent launchIntent, String gameType, int cardBgRes, int badgeColor) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
        this.launchIntent = launchIntent;
        this.gameType = gameType;
        this.cardBgRes = cardBgRes;
        this.badgeColor = badgeColor;
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

    public String getGameType() {
        return gameType;
    }

    public int getCardBgRes() {
        return cardBgRes;
    }

    public int getBadgeColor() {
        return badgeColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameAppInfo that = (GameAppInfo) o;
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;
        return label != null ? label.equals(that.label) : that.label == null;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        return result;
    }
}
