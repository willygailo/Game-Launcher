package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Vivo / iQOO device spoof profiles.
 * Real-world getprop values for Vivo X-series and iQOO gaming series.
 */
public class VivoProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // iQOO 15 Ultra — Snapdragon 8 Elite, 165Hz, 24GB RAM
        list.add(new SpoofProfile(
            "iqoo_15_ultra",
            "iQOO 15 Ultra (165Hz, Snapdragon 8 Elite, 24GB RAM)",
            "Vivo",
            "V2500A", "vivo", "vivo",
            "V2500A", "V2500A", "V2500A",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "vivo/V2500A/V2500A:15/AP1A.240505.005/compileV2500A_15.0.1.1:user/release-keys",
            "V2500A_15.0.1.1",
            "Adreno (TM) 830", "adreno", "196610", 24576
        ));

        // Vivo X100 Ultra — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "vivo_x100_ultra",
            "Vivo X100 Ultra (Snapdragon 8 Gen 3)",
            "Vivo",
            "V2366A", "vivo", "vivo",
            "V2366A", "V2366A", "V2366A",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "vivo/V2366A/V2366A:14/UP1A.231005.007/compileV2366A_14.0.3.5:user/release-keys",
            "V2366A_14.0.3.5",
            "Adreno (TM) 750"
        ));

        // iQOO 12 — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "iqoo_12",
            "iQOO 12 (Snapdragon 8 Gen 3)",
            "Vivo",
            "V2307A", "vivo", "vivo",
            "V2307A", "V2307A", "V2307A",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "vivo/V2307A/V2307A:14/UP1A.231005.007/compileV2307A_14.0.3.1:user/release-keys",
            "V2307A_14.0.3.1",
            "Adreno (TM) 750"
        ));

        // iQOO Neo 9 Pro — Snapdragon 8 Gen 2
        list.add(new SpoofProfile(
            "iqoo_neo9_pro",
            "iQOO Neo 9 Pro (Snapdragon 8 Gen 2)",
            "Vivo",
            "V2348A", "vivo", "vivo",
            "V2348A", "V2348A", "V2348A",
            "qcom", "kalama", "SM8550",
            "kalama", "sm8550",
            "vivo/V2348A/V2348A:14/UP1A.231005.007/compileV2348A_14.0.2.8:user/release-keys",
            "V2348A_14.0.2.8",
            "Adreno (TM) 740"
        ));

        return list;
    }
}
