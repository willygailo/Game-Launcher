package com.gamebooster.app.feature.spoofer.games;

import android.content.Context;
import com.gamebooster.app.feature.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.feature.spoofer.SpoofProfile;

public class PubgSpooferStrategy implements GameSpooferInterface {

    private final SpoofProfile profile = new SpoofProfile(
            "redmagic_10_pro",
            "REDMAGIC 10 Pro (165Hz, Snapdragon 8 Elite, 24GB RAM)",
            "Nubia",
            "NX789J", "nubia", "nubia",
            "NX789J", "NX789J", "NX789J",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "nubia/NX789J/NX789J:15/AP1A.240505.005/v10.0.1:user/release-keys",
            "NX789J_V10.0.1",
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
        return "PUBGM / BGMI Dedicated Spoofer Strategy (REDMAGIC 10 Pro)";
    }
}
