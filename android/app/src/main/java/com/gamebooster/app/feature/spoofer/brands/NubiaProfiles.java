package com.gamebooster.app.feature.spoofer.brands;

import com.gamebooster.app.feature.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Nubia / REDMAGIC device spoof profiles.
 * Real-world getprop values for REDMAGIC gaming series — 165Hz gaming powerhouse.
 */
public class NubiaProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // REDMAGIC 10 Pro — Snapdragon 8 Elite, 165Hz, 24GB RAM
        list.add(new SpoofProfile(
            "redmagic_10_pro",
            "REDMAGIC 10 Pro (165Hz, Snapdragon 8 Elite, Adreno 830, 24GB RAM)",
            "Nubia / REDMAGIC",
            "NX789J", "nubia", "nubia",
            "NX789J", "NX789J", "NX789J",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "nubia/NX789J/NX789J:15/AP3A.241105.007/V2.10:user/release-keys",
            "NX789J_V2.10",
            35, "Adreno (TM) 830", "adreno", "196610", 24576, 165
        ));

        // REDMAGIC 9S Pro — Snapdragon 8 Gen 3 Leading, 165Hz
        list.add(new SpoofProfile(
            "redmagic_9s_pro",
            "REDMAGIC 9S Pro (165Hz, Snapdragon 8 Gen 3 Leading)",
            "Nubia / REDMAGIC",
            "NX769S", "nubia", "nubia",
            "NX769S", "NX769S", "NX769S",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "nubia/NX769S/NX769S:14/UP1A.231005.007/V1.39:user/release-keys",
            "NX769S_V1.39",
            34, "Adreno (TM) 750", "adreno", "196610", 16384, 165
        ));

        // REDMAGIC 9 Pro — Snapdragon 8 Gen 3, 165Hz
        list.add(new SpoofProfile(
            "redmagic_9_pro",
            "REDMAGIC 9 Pro (165Hz, Snapdragon 8 Gen 3)",
            "Nubia / REDMAGIC",
            "NX769J", "nubia", "nubia",
            "NX769J", "NX769J", "NX769J",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "nubia/NX769J/NX769J:14/UP1A.231005.007/V1.35:user/release-keys",
            "NX769J_V1.35",
            34, "Adreno (TM) 750", "adreno", "196610", 16384, 165
        ));

        // REDMAGIC 8 Pro — Snapdragon 8 Gen 2, 165Hz
        list.add(new SpoofProfile(
            "redmagic_8_pro",
            "REDMAGIC 8 Pro (165Hz, Snapdragon 8 Gen 2)",
            "Nubia / REDMAGIC",
            "NX729J", "nubia", "nubia",
            "NX729J", "NX729J", "NX729J",
            "qcom", "kalama", "SM8550",
            "qcom", "kalama", "sm8550",
            "arm64-v8a",
            "nubia/NX729J/NX729J:13/TP1A.220624.014/V1.22:user/release-keys",
            "NX729J_V1.22",
            33, "Adreno (TM) 740", "adreno", "196610", 12288, 165
        ));

        return list;
    }
}
