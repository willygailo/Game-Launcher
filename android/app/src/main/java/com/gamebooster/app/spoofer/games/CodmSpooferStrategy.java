package com.gamebooster.app.spoofer.games;

import android.content.Context;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofProfile;

public class CodmSpooferStrategy implements GameSpooferInterface {

    private final SpoofProfile profile = new SpoofProfile(
            "black_shark_5_pro",
            "Black Shark 5 Pro (144Hz, Snapdragon 8 Gen 1)",
            "Black Shark",
            "SHARK KTUS-A0", "blackshark", "blackshark",
            "KTUS-A0", "KTUS-A0", "SHARK KTUS-A0",
            "qcom", "taro", "SM8450",
            "qcom", "taro", "SM8450",
            "arm64-v8a",
            "blackshark/KTUS-A0/KTUS-A0:13/TKQ1.221114.001/JOYUI14_23.05.12:user/release-keys",
            "JOYUI14_23.05.12",
            "Adreno (TM) 730", "adreno", "196610", 16384
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
        return "CODM Dedicated Spoofer Strategy (Black Shark 5 Pro)";
    }
}
