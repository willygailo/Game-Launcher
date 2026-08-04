package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Realme device spoof profiles.
 * Real-world getprop values for Realme GT and Narzo gaming series.
 */
public class RealmeProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Realme GT 5 Pro — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "realme_gt5_pro",
            "Realme GT 5 Pro (Snapdragon 8 Gen 3)",
            "Realme",
            "RMX3888", "realme", "realme",
            "RE5C4FL1", "RMX3888", "RMX3888",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "realme/RMX3888/RE5C4FL1:14/UP1A.231005.007/T.202408101530:user/release-keys",
            "RMX3888_14.0.0.801",
            "Adreno (TM) 750"
        ));

        // Realme GT Neo 5 — Snapdragon 8+ Gen 1
        list.add(new SpoofProfile(
            "realme_gt_neo5",
            "Realme GT Neo 5 (Snapdragon 8+ Gen 1)",
            "Realme",
            "RMX3706", "realme", "realme",
            "RE58B2L1", "RMX3706", "RMX3706",
            "qcom", "taro", "SM8475",
            "taro", "sm8475",
            "realme/RMX3706/RE58B2L1:13/TP1A.220624.014/T.202310201355:user/release-keys",
            "RMX3706_13.0.0.520",
            "Adreno (TM) 730"
        ));

        // Realme GT 3 — Snapdragon 8+ Gen 1
        list.add(new SpoofProfile(
            "realme_gt3",
            "Realme GT 3 (Snapdragon 8+ Gen 1)",
            "Realme",
            "RMX3709", "realme", "realme",
            "RE58B5L1", "RMX3709", "RMX3709",
            "qcom", "taro", "SM8475",
            "taro", "sm8475",
            "realme/RMX3709/RE58B5L1:13/TP1A.220624.014/T.202309111600:user/release-keys",
            "RMX3709_13.0.0.420",
            "Adreno (TM) 730"
        ));

        // Realme Narzo 60 Pro — Dimensity 7050
        list.add(new SpoofProfile(
            "realme_narzo60_pro",
            "Realme Narzo 60 Pro (Dimensity 7050)",
            "Realme",
            "RMX3771", "realme", "realme",
            "RE58C1L1", "RMX3771", "RMX3771",
            "mt6893", "mt6893", "Dimensity7050",
            "mt6893", "mt6893",
            "realme/RMX3771/RE58C1L1:13/TP1A.220624.014/T.202311151100:user/release-keys",
            "RMX3771_13.0.0.300",
            "Mali-G77 MC9"
        ));

        return list;
    }
}
