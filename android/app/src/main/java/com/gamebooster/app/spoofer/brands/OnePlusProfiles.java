package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * OnePlus device spoof profiles.
 * Real-world getprop values for OnePlus flagship and Ace gaming series.
 */
public class OnePlusProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // OnePlus 12 — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "oneplus_12",
            "OnePlus 12 (Snapdragon 8 Gen 3)",
            "OnePlus",
            "CPH2583", "OnePlus", "OnePlus",
            "aston", "CPH2583", "CPH2583",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "OnePlus/CPH2583/aston:14/UP1A.231005.007/202407050038:user/release-keys",
            "CPH2583_14.0.0.811(EX01)",
            "Adreno (TM) 750"
        ));

        // OnePlus 11 — Snapdragon 8 Gen 2
        list.add(new SpoofProfile(
            "oneplus_11",
            "OnePlus 11 (Snapdragon 8 Gen 2)",
            "OnePlus",
            "CPH2449", "OnePlus", "OnePlus",
            "salami", "CPH2449", "CPH2449",
            "qcom", "kalama", "SM8550",
            "kalama", "sm8550",
            "OnePlus/CPH2449/salami:14/UP1A.231005.007/202406170038:user/release-keys",
            "CPH2449_14.0.0.623(EX01)",
            "Adreno (TM) 740"
        ));

        // OnePlus Ace 3 Pro — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "oneplus_ace3_pro",
            "OnePlus Ace 3 Pro (Snapdragon 8 Gen 3)",
            "OnePlus",
            "PJZ110", "OnePlus", "OnePlus",
            "aston_chn", "PJZ110", "PJZ110",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "OnePlus/PJZ110/aston_chn:14/UP1A.231005.007/202405010038:user/release-keys",
            "PJZ110_14.0.0.500(CN01)",
            "Adreno (TM) 750"
        ));

        // OnePlus Nord 4 — Snapdragon 7+ Gen 3
        list.add(new SpoofProfile(
            "oneplus_nord4",
            "OnePlus Nord 4 (Snapdragon 7+ Gen 3)",
            "OnePlus",
            "CPH2625", "OnePlus", "OnePlus",
            "larry", "CPH2625", "CPH2625",
            "qcom", "kalama", "SM7675",
            "kalama", "sm7675",
            "OnePlus/CPH2625/larry:14/UP1A.231005.007/202406280038:user/release-keys",
            "CPH2625_14.0.0.402(EX01)",
            "Adreno (TM) 732"
        ));

        return list;
    }
}
