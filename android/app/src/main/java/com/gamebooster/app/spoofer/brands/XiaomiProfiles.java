package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Xiaomi / POCO / Redmi device spoof profiles.
 * Real-world getprop values for Xiaomi flagship and gaming series.
 */
public class XiaomiProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Xiaomi 14 Ultra — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "xiaomi_14_ultra",
            "Xiaomi 14 Ultra (Snapdragon 8 Gen 3)",
            "Xiaomi",
            "24030PN60G", "Xiaomi", "Xiaomi",
            "aurora", "aurora_global", "24030PN60G",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "Xiaomi/aurora_global/aurora:14/UP1A.231005.007/OS1.0.4.0.UNCMIXM:user/release-keys",
            "OS1.0.4.0.UNCMIXM",
            "Adreno (TM) 750"
        ));

        // Xiaomi 14 Pro — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "xiaomi_14_pro",
            "Xiaomi 14 Pro (Snapdragon 8 Gen 3)",
            "Xiaomi",
            "23116PN5BC", "Xiaomi", "Xiaomi",
            "shennong", "shennong", "23116PN5BC",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "Xiaomi/shennong/shennong:14/UP1A.231005.007/OS1.0.3.0.UNCMIXM:user/release-keys",
            "OS1.0.3.0.UNCMIXM",
            "Adreno (TM) 750"
        ));

        // POCO F6 Pro — Snapdragon 8 Gen 2
        list.add(new SpoofProfile(
            "poco_f6_pro",
            "POCO F6 Pro (Snapdragon 8 Gen 2)",
            "Xiaomi",
            "23113RKC6G", "POCO", "Xiaomi",
            "vermeer", "vermeer_global", "23113RKC6G",
            "qcom", "kalama", "SM8550",
            "kalama", "sm8550",
            "POCO/vermeer_global/vermeer:14/UP1A.231005.007/V816.0.7.0.VNOMIXM:user/release-keys",
            "V816.0.7.0.VNOMIXM",
            "Adreno (TM) 740"
        ));

        // Redmi K70 Pro — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "redmi_k70_pro",
            "Redmi K70 Pro (Snapdragon 8 Gen 3)",
            "Xiaomi",
            "23113RKC6C", "Redmi", "Xiaomi",
            "manet", "manet", "23113RKC6C",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "Redmi/manet/manet:14/UP1A.231005.007/OS1.0.5.0.UNCCNXM:user/release-keys",
            "OS1.0.5.0.UNCCNXM",
            "Adreno (TM) 750"
        ));

        // Redmi Note 13 Pro+ — Dimensity 7200 Ultra
        list.add(new SpoofProfile(
            "redmi_note13_pro_plus",
            "Redmi Note 13 Pro+ (Dimensity 7200 Ultra)",
            "Xiaomi",
            "23090RA98G", "Redmi", "Xiaomi",
            "duchesse", "duchesse_global", "23090RA98G",
            "mt6985", "mt6985", "Dimensity7200Ultra",
            "mt6985", "mt6985",
            "Redmi/duchesse_global/duchesse:14/UP1A.231005.007/V816.0.5.0.UMZMIXM:user/release-keys",
            "V816.0.5.0.UMZMIXM",
            "Mali-G615 MC6"
        ));

        return list;
    }
}
