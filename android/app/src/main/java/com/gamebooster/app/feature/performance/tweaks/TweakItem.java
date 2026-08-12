package com.gamebooster.app.feature.performance.tweaks;

import android.os.Build;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TweakItem — Unit model representing an individual system performance tweak.
 *
 * Supports shell execution commands, Shizuku ADB requirements, Android version
 * compatibility bounds, risk levels, and JSON serialization for JS bridge.
 */
public class TweakItem {

    public enum RiskLevel {
        SAFE,
        RECOMMENDED,
        EXPERIMENTAL
    }

    private final String id;
    private final String title;
    private final String description;
    private final String applyCommand;
    private final String revertCommand;
    private final TweakCategory category;
    private final boolean requiresShizuku;
    private final int minSdk;
    private final int maxSdk;
    private final RiskLevel riskLevel;
    private boolean isApplied;

    public TweakItem(String id, String title, String description, String applyCommand,
                     String revertCommand, TweakCategory category, boolean requiresShizuku) {
        this(id, title, description, applyCommand, revertCommand, category, requiresShizuku, 21, 36, RiskLevel.RECOMMENDED);
    }

    public TweakItem(String id, String title, String description, String applyCommand,
                     String revertCommand, TweakCategory category, boolean requiresShizuku,
                     int minSdk, int maxSdk, RiskLevel riskLevel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.applyCommand = applyCommand;
        this.revertCommand = revertCommand;
        this.category = category;
        this.requiresShizuku = requiresShizuku;
        this.minSdk = minSdk > 0 ? minSdk : 21;
        this.maxSdk = maxSdk > 0 ? maxSdk : 36;
        this.riskLevel = riskLevel != null ? riskLevel : RiskLevel.RECOMMENDED;
        this.isApplied = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getApplyCommand() {
        return applyCommand;
    }

    public String getRevertCommand() {
        return revertCommand;
    }

    public TweakCategory getCategory() {
        return category;
    }

    public boolean isRequiresShizuku() {
        return requiresShizuku;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public int getMaxSdk() {
        return maxSdk;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public boolean isApplied() {
        return isApplied;
    }

    public void setApplied(boolean applied) {
        isApplied = applied;
    }

    public boolean isCompatibleWithCurrentAndroid() {
        int sdk = Build.VERSION.SDK_INT;
        return sdk >= minSdk && sdk <= maxSdk;
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("title", title);
            obj.put("description", description);
            obj.put("category", category != null ? category.name() : "SYSTEM");
            obj.put("requiresShizuku", requiresShizuku);
            obj.put("isApplied", isApplied);
            obj.put("minSdk", minSdk);
            obj.put("maxSdk", maxSdk);
            obj.put("riskLevel", riskLevel.name());
            obj.put("isCompatible", isCompatibleWithCurrentAndroid());
        } catch (JSONException ignored) {}
        return obj;
    }
}
