package com.gamebooster.app.feature.spoofer.games;

import android.content.Context;
import com.gamebooster.app.feature.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.feature.spoofer.SpoofProfile;

public class GenericGameSpooferStrategy implements GameSpooferInterface {

    private final SpoofProfile profile = new SpoofProfile(
            "asus_rog9_pro",
            "ASUS ROG Phone 9 Pro (165Hz, Snapdragon 8 Elite, 24GB RAM)",
            "ASUS ROG",
            "ASUS_AI2501", "asus", "asus",
            "ASUS_AI2501", "WW_AI2501", "ASUS_AI2501",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "asus/WW_AI2501/ASUS_AI2501:15/AP1A.240505.005/37.0210.0210.100-0:user/release-keys",
            "WW_AI2501-37.0210.0210.100",
            "Adreno (TM) 830", "adreno", "196610", 24576
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
        return "Generic Game Fallback Spoofer Strategy (ROG Phone 9 Pro)";
    }
}
