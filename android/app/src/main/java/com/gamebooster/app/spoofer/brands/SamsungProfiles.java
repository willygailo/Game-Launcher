package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class SamsungProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();
        list.add(new SpoofProfile(
                "samsung_s26_ultra",
                "Samsung Galaxy S26 Ultra",
                "Samsung",
                "SM-S948B",
                "samsung",
                "samsung",
                "e3q",
                "e3qxxx",
                "e3q",
                "qcom",
                "sun",
                "SM8750-AC",
                "sun",
                "Snapdragon 8 Elite for Galaxy",
                "samsung/e3qxxx/e3q:15/AP3A.240905.015/S948BXXU0AXL3:user/release-keys",
                "AP3A.240905.015.S948BXXU0AXL3",
                "Adreno (TM) 840"
        ));
        list.add(new SpoofProfile(
                "samsung_s25_ultra",
                "Samsung Galaxy S25 Ultra",
                "Samsung",
                "SM-S938B",
                "samsung",
                "samsung",
                "e2q",
                "e2qxxx",
                "e2q",
                "qcom",
                "sun",
                "SM8750",
                "sun",
                "Snapdragon 8 Elite",
                "samsung/e2qxxx/e2q:15/AP3A.240905.015/S938BXXU1AYB1:user/release-keys",
                "AP3A.240905.015.S938BXXU1AYB1",
                "Adreno (TM) 830"
        ));
        list.add(new SpoofProfile(
                "samsung_s24_ultra",
                "Samsung Galaxy S24 Ultra",
                "Samsung",
                "SM-S928B",
                "samsung",
                "samsung",
                "e1q",
                "e1qxxx",
                "e1q",
                "qcom",
                "pineapple",
                "SM8650",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "samsung/e1qxxx/e1q:14/UP1A.231005.007/S928BXXU1AXB5:user/release-keys",
                "UP1A.231005.007.S928BXXU1AXB5",
                "Adreno (TM) 750"
        ));
        return list;
    }
}
