package com.gamebooster.app.feature.spoofer.games;

import android.content.Context;
import com.gamebooster.app.feature.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.feature.spoofer.SpoofProfile;

public class FreeFireSpooferStrategy implements GameSpooferInterface {

    private final SpoofProfile profile = new SpoofProfile(
            "oneplus_12",
            "OnePlus 12 (120Hz, Snapdragon 8 Gen 3)",
            "OnePlus",
            "CPH2581", "OnePlus", "OnePlus",
            "OP595DL1", "CPH2581", "CPH2581",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "SM8650",
            "arm64-v8a",
            "OnePlus/CPH2581/OP595DL1:14/UKQ1.230924.001/R.173a1ef-1:user/release-keys",
            "CPH2581_14.0.0.604",
            "Adreno (TM) 750", "adreno", "196610", 16384
    );

    @Override
    public boolean applyGameSpoof(Context context, String packageName) {
        return DeviceSpooferEngine.applyProfile(context, profile, packageName);
    }

    @Override
    public void resetGameSpoof() {
        DeviceSpooferEngine.resetSpoofing();
    }

    @Override
    public SpoofProfile getSpoofProfile() {
        return profile;
    }

    @Override
    public String getStrategyName() {
        return "Free Fire Dedicated Spoofer Strategy (OnePlus 12)";
    }
}
