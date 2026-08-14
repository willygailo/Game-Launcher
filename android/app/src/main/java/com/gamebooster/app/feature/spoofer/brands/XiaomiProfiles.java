package com.gamebooster.app.feature.spoofer.brands;

import com.gamebooster.app.feature.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Xiaomi / POCO / Redmi device spoof profiles.
 * Sourced for Android 13, 14, 15, and 16 HyperOS / MIUI — 165Hz flagship gaming capability.
 */
public class XiaomiProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Xiaomi 15 Ultra — Android 16, Snapdragon 8 Elite, 165Hz
        list.add(new SpoofProfile(
            "xiaomi_15_ultra",
            "Xiaomi 15 Ultra (165Hz, Android 16, Snapdragon 8 Elite)",
            "Xiaomi",
            "25010PN60G", "Xiaomi", "Xiaomi",
            "xuanyuan", "xuanyuan_global", "25010PN60G",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "Xiaomi/xuanyuan_global/xuanyuan:16/BP1A.260105.001/OS2.0.1.0.UNCMIXM:user/release-keys",
            "OS2.0.1.0.UNCMIXM",
            35, "Adreno (TM) 830", "adreno", "196610", 24576, 165
        ));

        // Xiaomi 14 Ultra — Android 14/15, Snapdragon 8 Gen 3, 165Hz Extreme
        list.add(new SpoofProfile(
            "xiaomi_14_ultra",
            "Xiaomi 14 Ultra (165Hz, Android 14/15, Snapdragon 8 Gen 3)",
            "Xiaomi",
            "24030PN60G", "Xiaomi", "Xiaomi",
            "aurora", "aurora_global", "24030PN60G",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "Xiaomi/aurora_global/aurora:14/UP1A.231005.007/OS1.0.4.0.UNCMIXM:user/release-keys",
            "OS1.0.4.0.UNCMIXM",
            34, "Adreno (TM) 750", "adreno", "196610", 16384, 165
        ));

        // POCO F6 Pro — Android 14/15, Snapdragon 8 Gen 2, 165Hz Extreme
        list.add(new SpoofProfile(
            "poco_f6_pro",
            "POCO F6 Pro (165Hz, Android 14/15, Snapdragon 8 Gen 2)",
            "Xiaomi",
            "23113RKC6G", "POCO", "Xiaomi",
            "vermeer", "vermeer_global", "23113RKC6G",
            "qcom", "kalama", "SM8550",
            "qcom", "kalama", "sm8550",
            "arm64-v8a",
            "POCO/vermeer_global/vermeer:14/UP1A.231005.007/V816.0.7.0.VNOMIXM:user/release-keys",
            "V816.0.7.0.VNOMIXM",
            34, "Adreno (TM) 740", "adreno", "196610", 16384, 165
        ));

        // POCO X6 Pro — MediaTek Dimensity 8300-Ultra, 165Hz Extreme
        list.add(new SpoofProfile(
            "poco_x6_pro",
            "POCO X6 Pro (165Hz, Dimensity 8300-Ultra, Mali-G615)",
            "Xiaomi",
            "2311DRK48G", "POCO", "Xiaomi",
            "duchamp", "duchamp_global", "2311DRK48G",
            "mediatek", "mt6897", "MT6897",
            "mediatek", "mt6897", "mt6897",
            "arm64-v8a",
            "POCO/duchamp_global/duchamp:14/UP1A.231005.007/V816.0.4.0.UNLMIXM:user/release-keys",
            "V816.0.4.0.UNLMIXM",
            34, "Mali-G615-MC6", "mali", "196610", 12288, 165
        ));

        return list;
    }
}
