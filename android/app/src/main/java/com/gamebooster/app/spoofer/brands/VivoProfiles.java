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

        // iQOO 15 Ultra — Snapdragon 8 Elite Gen 5 (3nm, Active Fan), Adreno 840 (1300MHz), 24GB RAM, 165Hz 2K
        list.add(new SpoofProfile(
            "iqoo_15_ultra",
            "iQOO 15 Ultra (165Hz 2K, Snapdragon 8 Elite Gen 5, Adreno 840 @1300MHz, Active Fan, 24GB RAM)",
            "Vivo",
            "V2500A", "vivo", "vivo",
            "V2500A", "V2500A", "V2500A",
            "qcom", "sun", "SM8850",
            "qcom", "sun", "SM8850",
            "arm64-v8a",
            "vivo/V2500A/V2500A:16/BP1A.260105.005/compileV2500A_16.0.1.1:user/release-keys",
            "V2500A_16.0.1.1",
            "Adreno (TM) 840", "adreno", "196610", 24576
        ));

        // iQOO 15 (Vanilla) — Snapdragon 8 Elite Gen 5 (3nm) + Q3 Gaming Chip, Adreno 840, 16GB RAM, 144Hz 2K
        list.add(new SpoofProfile(
            "iqoo_15_vanilla",
            "iQOO 15 (144Hz 2K, Snapdragon 8 Elite Gen 5, Q3 Gaming Chip, Adreno 840, 16GB RAM)",
            "Vivo",
            "V2430A", "vivo", "vivo",
            "V2430A", "V2430A", "V2430A",
            "qcom", "sun", "SM8850",
            "qcom", "sun", "SM8850",
            "arm64-v8a",
            "vivo/V2430A/V2430A:16/BP1A.260105.005/compileV2430A_16.0.1.1:user/release-keys",
            "V2430A_16.0.1.1",
            "Adreno (TM) 840", "adreno", "196610", 16384
        ));

        // iQOO 13 — Snapdragon 8 Elite (3nm) + Q2 Gaming Chip, Adreno 830, 16GB RAM, 144Hz 2K
        list.add(new SpoofProfile(
            "iqoo_13",
            "iQOO 13 (144Hz 2K, Snapdragon 8 Elite, Q2 Gaming Chip, Adreno 830, 16GB RAM)",
            "Vivo",
            "V2405A", "vivo", "vivo",
            "V2405A", "V2405A", "V2405A",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "vivo/V2405A/V2405A:15/AP1A.240505.005/compileV2405A_15.0.1.1:user/release-keys",
            "V2405A_15.0.1.1",
            "Adreno (TM) 830", "adreno", "196610", 16384
        ));

        // Vivo X100 Ultra — Snapdragon 8 Gen 3, Adreno 750
        list.add(new SpoofProfile(
            "vivo_x100_ultra",
            "Vivo X100 Ultra (Snapdragon 8 Gen 3, Adreno 750)",
            "Vivo",
            "V2366A", "vivo", "vivo",
            "V2366A", "V2366A", "V2366A",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "vivo/V2366A/V2366A:14/UP1A.231005.007/compileV2366A_14.0.3.5:user/release-keys",
            "V2366A_14.0.3.5",
            "Adreno (TM) 750", "adreno", "196610", 16384
        ));

        // iQOO 12 — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "iqoo_12",
            "iQOO 12 (Snapdragon 8 Gen 3, Adreno 750)",
            "Vivo",
            "V2307A", "vivo", "vivo",
            "V2307A", "V2307A", "V2307A",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "vivo/V2307A/V2307A:14/UP1A.231005.007/compileV2307A_14.0.3.1:user/release-keys",
            "V2307A_14.0.3.1",
            "Adreno (TM) 750", "adreno", "196610", 16384
        ));

        return list;
    }
}
