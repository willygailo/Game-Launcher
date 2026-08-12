package com.gamebooster.app.feature.spoofer.games;

import android.content.Context;
import com.gamebooster.app.feature.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.feature.spoofer.SpoofProfile;

public class WildRiftSpooferStrategy implements GameSpooferInterface {

    private final SpoofProfile profile = new SpoofProfile(
            "asus_rog8_pro",
            "ASUS ROG Phone 8 Pro (165Hz, Snapdragon 8 Gen 3)",
            "ASUS ROG",
            "ASUS_AI2401", "asus", "asus",
            "ASUS_AI2401", "WW_AI2401", "ASUS_AI2401",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "SM8650",
            "arm64-v8a",
            "asus/WW_AI2401/ASUS_AI2401:14/UP1A.231005.007/36.0210.0210.238-0:user/release-keys",
            "WW_AI2401-36.0210.0210.238",
            "Adreno (TM) 750", "adreno", "196610", 24576
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
        return "Wild Rift Dedicated Spoofer Strategy (ROG Phone 8 Pro)";
    }
}
