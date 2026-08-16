package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class AsusRogProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();
        list.add(new SpoofProfile(
                "asus_rog9_pro",
                "ASUS ROG Phone 9 Pro (185Hz Extreme)",
                "ASUS ROG",
                "ASUS_AI2401",
                "asus",
                "asus",
                "ASUS_AI2401",
                "WW_AI2401",
                "AI2401",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "asus/WW_AI2401/ASUS_AI2401:15/UKQ1.231003.002/35.0810.0810.27:user/release-keys",
                "35.0810.0810.27",
                "Adreno (TM) 830"
        ));
        list.add(new SpoofProfile(
                "asus_rog8_pro",
                "ASUS ROG Phone 8 Pro (165Hz)",
                "ASUS ROG",
                "ASUS_AI2401_A",
                "asus",
                "asus",
                "ASUS_AI2401_A",
                "WW_AI2401_A",
                "AI2401_A",
                "qcom",
                "pineapple",
                "SM8650",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "asus/WW_AI2401_A/ASUS_AI2401_A:14/UKQ1.230917.001/34.1420.1420.327:user/release-keys",
                "34.1420.1420.327",
                "Adreno (TM) 750"
        ));
        return list;
    }
}
