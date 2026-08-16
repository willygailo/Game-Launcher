package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class NubiaProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();
        list.add(new SpoofProfile(
                "redmagic_10_pro",
                "Nubia REDMAGIC 10 Pro+ (165Hz eSports)",
                "Nubia",
                "NX789J",
                "nubia",
                "nubia",
                "NX789J",
                "NX789J",
                "NX789J",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "nubia/NX789J/NX789J:15/UKQ1.231003.002/REDMAGICOS10.0:user/release-keys",
                "REDMAGICOS10.0",
                "Adreno (TM) 830"
        ));
        list.add(new SpoofProfile(
                "redmagic_9_pro",
                "Nubia REDMAGIC 9 Pro (165Hz)",
                "Nubia",
                "NX769J",
                "nubia",
                "nubia",
                "NX769J",
                "NX769J",
                "NX769J",
                "qcom",
                "pineapple",
                "SM8650",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "nubia/NX769J/NX769J:14/UKQ1.230917.001/REDMAGICOS9.0:user/release-keys",
                "REDMAGICOS9.0",
                "Adreno (TM) 750"
        ));
        return list;
    }
}
