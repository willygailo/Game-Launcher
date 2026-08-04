package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Nubia / REDMAGIC device spoof profiles.
 * Real-world getprop values for REDMAGIC gaming series — designed for 165Hz gaming.
 */
public class NubiaProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // REDMAGIC 9S Pro — Snapdragon 8 Gen 3 Leading
        list.add(new SpoofProfile(
            "redmagic_9s_pro",
            "REDMAGIC 9S Pro (165Hz, Snapdragon 8 Gen 3 Leading)",
            "Nubia / REDMAGIC",
            "NX769S", "nubia", "nubia",
            "NX769S", "NX769S", "NX769S",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "nubia/NX769S/NX769S:14/UP1A.231005.007/V1.39:user/release-keys",
            "NX769S_V1.39",
            "Adreno (TM) 750"
        ));

        // REDMAGIC 9 Pro — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "redmagic_9_pro",
            "REDMAGIC 9 Pro (165Hz, Snapdragon 8 Gen 3)",
            "Nubia / REDMAGIC",
            "NX769J", "nubia", "nubia",
            "NX769J", "NX769J", "NX769J",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "nubia/NX769J/NX769J:14/UP1A.231005.007/V1.35:user/release-keys",
            "NX769J_V1.35",
            "Adreno (TM) 750"
        ));

        // REDMAGIC 8 Pro — Snapdragon 8 Gen 2
        list.add(new SpoofProfile(
            "redmagic_8_pro",
            "REDMAGIC 8 Pro (165Hz, Snapdragon 8 Gen 2)",
            "Nubia / REDMAGIC",
            "NX729J", "nubia", "nubia",
            "NX729J", "NX729J", "NX729J",
            "qcom", "kalama", "SM8550",
            "kalama", "sm8550",
            "nubia/NX729J/NX729J:13/TP1A.220624.014/V1.22:user/release-keys",
            "NX729J_V1.22",
            "Adreno (TM) 740"
        ));

        return list;
    }
}
