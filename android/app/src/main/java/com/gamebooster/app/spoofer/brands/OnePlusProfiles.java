package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * OnePlus device spoof profiles.
 * Sourced for Android 13, 14, 15, and 16 OxygenOS.
 */
public class OnePlusProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // OnePlus 13 — Android 15/16, Snapdragon 8 Elite
        list.add(new SpoofProfile(
            "oneplus_13",
            "OnePlus 13 (165Hz, Android 15/16, Snapdragon 8 Elite)",
            "OnePlus",
            "CPH2681", "OnePlus", "OnePlus",
            "OP611DL1", "CPH2681", "CPH2681",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "OnePlus/CPH2681/OP611DL1:15/AP1A.240505.005/R.183a1ef-1:user/release-keys",
            "CPH2681_15.0.0.100",
            "Adreno (TM) 830", "adreno", "196610", 24576
        ));

        // OnePlus 12 — Android 14, Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "oneplus_12",
            "OnePlus 12 (120Hz, Android 14, Snapdragon 8 Gen 3)",
            "OnePlus",
            "CPH2583", "OnePlus", "OnePlus",
            "aston", "CPH2583", "CPH2583",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "OnePlus/CPH2583/aston:14/UP1A.231005.007/202407050038:user/release-keys",
            "CPH2583_14.0.0.811(EX01)",
            "Adreno (TM) 750"
        ));

        // OnePlus 11 — Android 13/14, Snapdragon 8 Gen 2
        list.add(new SpoofProfile(
            "oneplus_11",
            "OnePlus 11 (120Hz, Android 13/14, Snapdragon 8 Gen 2)",
            "OnePlus",
            "CPH2449", "OnePlus", "OnePlus",
            "salami", "CPH2449", "CPH2449",
            "qcom", "kalama", "SM8550",
            "kalama", "sm8550",
            "OnePlus/CPH2449/salami:14/UP1A.231005.007/202406170038:user/release-keys",
            "CPH2449_14.0.0.623(EX01)",
            "Adreno (TM) 740"
        ));

        return list;
    }
}
