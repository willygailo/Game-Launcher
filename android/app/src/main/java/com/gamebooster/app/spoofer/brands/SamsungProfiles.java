package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class SamsungProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Galaxy S26 Ultra — Snapdragon 8 Elite Gen 5 for Galaxy (3nm, Oryon V3), Adreno 840, 24GB LPDDR5X, 165Hz
        list.add(new SpoofProfile(
            "samsung_s26_ultra",
            "Galaxy S26 Ultra (165Hz, Snapdragon 8 Elite Gen 5, Adreno 840, 24GB RAM)",
            "Samsung",
            "SM-S948B", "samsung", "samsung",
            "e5q", "e5qxxx", "SM-S948B",
            "qcom", "sun", "SM8850-AC",
            "qcom", "sun", "SM8850-AC",
            "arm64-v8a",
            "samsung/e5qxxx/e5q:16/BP1A.260105.001/S948BXXU1AXA1:user/release-keys",
            "BP1A.260105.001.S948BXXU1AXA1",
            "Adreno (TM) 840", "adreno", "196610", 24576
        ));

        // Galaxy S25 Ultra — Snapdragon 8 Elite for Galaxy (3nm, Oryon Gen2), Adreno 830, 16GB RAM, 120Hz
        list.add(new SpoofProfile(
            "samsung_s25_ultra",
            "Galaxy S25 Ultra (120Hz, Snapdragon 8 Elite, Adreno 830, 16GB RAM)",
            "Samsung",
            "SM-S938B", "samsung", "samsung",
            "e4q", "e4qxxx", "SM-S938B",
            "qcom", "sun", "SM8750-AC",
            "qcom", "sun", "SM8750-AC",
            "arm64-v8a",
            "samsung/e4qxxx/e4q:15/AP3A.250105.001/S938BXXS1AXL5:user/release-keys",
            "AP3A.250105.001.S938BXXS1AXL5",
            "Adreno (TM) 830", "adreno", "196610", 16384
        ));

        // Galaxy S24 Ultra — Snapdragon 8 Gen 3 for Galaxy (4nm), Adreno 750, 12GB RAM, 120Hz
        list.add(new SpoofProfile(
            "samsung_s24_ultra",
            "Galaxy S24 Ultra (120Hz, Snapdragon 8 Gen 3, Adreno 750, 12GB RAM)",
            "Samsung",
            "SM-S928B", "samsung", "samsung",
            "e3q", "e3qxxx", "SM-S928B",
            "qcom", "kalama", "SM8650-AC",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "samsung/e3qxxx/e3q:14/UP1A.231005.007/S928BXXU3AXK1:user/release-keys",
            "UP1A.231005.007.S928BXXU3AXK1",
            "Adreno (TM) 750", "adreno", "196610", 12288
        ));

        return list;
    }
}
