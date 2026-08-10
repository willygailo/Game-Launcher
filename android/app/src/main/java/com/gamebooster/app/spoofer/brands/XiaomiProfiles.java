package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Xiaomi / POCO / Redmi device spoof profiles.
 * Sourced for Android 13, 14, 15, and 16 HyperOS / MIUI.
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
            "Adreno (TM) 830", "adreno", "196610", 24576
        ));

        // Xiaomi 14 Ultra — Android 14/15, Snapdragon 8 Gen 3, 120Hz
        list.add(new SpoofProfile(
            "xiaomi_14_ultra",
            "Xiaomi 14 Ultra (120Hz, Android 14/15, Snapdragon 8 Gen 3)",
            "Xiaomi",
            "24030PN60G", "Xiaomi", "Xiaomi",
            "aurora", "aurora_global", "24030PN60G",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "Xiaomi/aurora_global/aurora:14/UP1A.231005.007/OS1.0.4.0.UNCMIXM:user/release-keys",
            "OS1.0.4.0.UNCMIXM",
            34, "Adreno (TM) 750", "adreno", "196610", 16384, 120
        ));

        // POCO F6 Pro — Android 14/15, Snapdragon 8 Gen 2, 120Hz
        list.add(new SpoofProfile(
            "poco_f6_pro",
            "POCO F6 Pro (120Hz, Android 14/15, Snapdragon 8 Gen 2)",
            "Xiaomi",
            "23113RKC6G", "POCO", "Xiaomi",
            "vermeer", "vermeer_global", "23113RKC6G",
            "qcom", "kalama", "SM8550",
            "qcom", "kalama", "sm8550",
            "arm64-v8a",
            "POCO/vermeer_global/vermeer:14/UP1A.231005.007/V816.0.7.0.VNOMIXM:user/release-keys",
            "V816.0.7.0.VNOMIXM",
            34, "Adreno (TM) 740", "adreno", "196610", 16384, 120
        ));

        // Redmi K70 Pro — Android 14, Snapdragon 8 Gen 3, 120Hz
        list.add(new SpoofProfile(
            "redmi_k70_pro",
            "Redmi K70 Pro (120Hz, Android 14, Snapdragon 8 Gen 3)",
            "Xiaomi",
            "23113RKC6C", "Redmi", "Xiaomi",
            "manet", "manet", "23113RKC6C",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "Redmi/manet/manet:14/UP1A.231005.007/OS1.0.5.0.UNCCNXM:user/release-keys",
            "OS1.0.5.0.UNCCNXM",
            34, "Adreno (TM) 750", "adreno", "196610", 16384, 120
        ));

        return list;
    }
}
