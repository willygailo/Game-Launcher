package com.gamebooster.app.feature.spoofer.brands;

import com.gamebooster.app.feature.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Sony Xperia device spoof profiles.
 * Real-world getprop values for Xperia 1 VI and Xperia 1 V.
 */
public class SonyProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Sony Xperia 1 VI — Snapdragon 8 Gen 3, Adreno 750, 12GB RAM, 120Hz
        list.add(new SpoofProfile(
            "sony_xperia_1_vi",
            "Sony Xperia 1 VI (120Hz 4K, Snapdragon 8 Gen 3, Adreno 750)",
            "Sony",
            "XQ-EC54", "Sony", "Sony",
            "PDX-245", "PDX-245", "XQ-EC54",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "Sony/XQ-EC54/PDX-245:14/69.0.A.2.28/06900A02002800:user/release-keys",
            "69.0.A.2.28",
            34, "Adreno (TM) 750", "adreno", "196610", 12288, 120
        ));

        // Sony Xperia 1 V — Snapdragon 8 Gen 2, Adreno 740, 12GB RAM, 120Hz
        list.add(new SpoofProfile(
            "sony_xperia_1_v",
            "Sony Xperia 1 V (120Hz 4K, Snapdragon 8 Gen 2, Adreno 740)",
            "Sony",
            "XQ-DQ54", "Sony", "Sony",
            "PDX-234", "PDX-234", "XQ-DQ54",
            "qcom", "kalama", "SM8550",
            "qcom", "kalama", "sm8550",
            "arm64-v8a",
            "Sony/XQ-DQ54/PDX-234:13/67.0.A.4.104/06700A04010400:user/release-keys",
            "67.0.A.4.104",
            33, "Adreno (TM) 740", "adreno", "196610", 12288, 120
        ));

        return list;
    }
}
