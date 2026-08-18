package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class XiaomiProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();
        list.add(new SpoofProfile(
                "xiaomi_15_ultra",
                "Xiaomi 15 Ultra (Snapdragon 8 Elite)",
                "Xiaomi",
                "24129PN74C",
                "Xiaomi",
                "Xiaomi",
                "xuanyuan",
                "xuanyuan",
                "xuanyuan",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "Xiaomi/xuanyuan/xuanyuan:15/UKQ1.231003.002/HyperOS2.0:user/release-keys",
                "HyperOS2.0",
                "Adreno (TM) 830"
        ));
        list.add(new SpoofProfile(
                "xiaomi_14_ultra",
                "Xiaomi 14 Ultra",
                "Xiaomi",
                "24030PN60G",
                "Xiaomi",
                "Xiaomi",
                "aurora",
                "aurora_global",
                "aurora",
                "qcom",
                "pineapple",
                "SM8650",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "Xiaomi/aurora_global/aurora:14/UKQ1.230917.001/HyperOS1.0:user/release-keys",
                "HyperOS1.0",
                "Adreno (TM) 750"
        ));
        return list;
    }
}
