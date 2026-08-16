package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class OnePlusProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();
        list.add(new SpoofProfile(
                "oneplus_13",
                "OnePlus 13 (Snapdragon 8 Elite)",
                "OnePlus",
                "PJZ110",
                "OnePlus",
                "OnePlus",
                "OP5D1BL1",
                "PJZ110",
                "PJZ110",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "OnePlus/PJZ110/OP5D1BL1:15/UKQ1.231003.002/OxygenOS15.0:user/release-keys",
                "OxygenOS15.0",
                "Adreno (TM) 830"
        ));
        list.add(new SpoofProfile(
                "oneplus_12",
                "OnePlus 12",
                "OnePlus",
                "CPH2583",
                "OnePlus",
                "OnePlus",
                "OP595DL1",
                "CPH2583",
                "CPH2583",
                "qcom",
                "pineapple",
                "SM8650",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "OnePlus/CPH2583/OP595DL1:14/UKQ1.230917.001/OxygenOS14.0:user/release-keys",
                "OxygenOS14.0",
                "Adreno (TM) 750"
        ));
        return list;
    }
}
