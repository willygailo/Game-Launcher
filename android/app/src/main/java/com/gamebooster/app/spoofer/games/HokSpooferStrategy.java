package com.gamebooster.app.spoofer.games;

import android.content.Context;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofProfile;

public class HokSpooferStrategy implements GameSpooferInterface {

    private final SpoofProfile profile = new SpoofProfile(
            "iqoo_15_ultra",
            "iQOO 15 Ultra (165Hz, Snapdragon 8 Elite, 24GB RAM)",
            "Vivo",
            "V2500A", "vivo", "vivo",
            "V2500A", "V2500A", "V2500A",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "vivo/V2500A/V2500A:15/AP1A.240505.005/compileV2500A_15.0.1.1:user/release-keys",
            "V2500A_15.0.1.1",
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
        return "Honor of Kings Dedicated Spoofer Strategy (iQOO 15 Ultra)";
    }
}
