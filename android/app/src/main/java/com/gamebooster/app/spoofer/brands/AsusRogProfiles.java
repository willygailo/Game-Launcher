package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * ASUS ROG Phone device spoof profiles.
 * Real-world getprop values for ROG Phone gaming series — 144/165 Hz flagship devices.
 */
public class AsusRogProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // ROG Phone 9 Pro — Snapdragon 8 Elite, 165 Hz, 24GB RAM
        list.add(new SpoofProfile(
            "asus_rog9_pro",
            "ASUS ROG Phone 9 Pro (165Hz, Snapdragon 8 Elite, Adreno 830, 24GB RAM)",
            "ASUS ROG",
            "ASUS_AI2501", "asus", "asus",
            "ASUS_AI2501", "WW_AI2501", "ASUS_AI2501",
            "qcom", "sun", "SM8750",
            "qcom", "sun", "SM8750",
            "arm64-v8a",
            "asus/WW_AI2501/ASUS_AI2501:15/AP3A.241105.007/37.0210.0210.100-0:user/release-keys",
            "WW_AI2501-37.0210.0210.100",
            35, "Adreno (TM) 830", "adreno", "196610", 24576, 165
        ));

        // ROG Phone 8 Pro — Snapdragon 8 Gen 3, 165 Hz
        list.add(new SpoofProfile(
            "asus_rog8_pro",
            "ASUS ROG Phone 8 Pro (165Hz, Snapdragon 8 Gen 3)",
            "ASUS ROG",
            "ASUS_AI2401", "asus", "asus",
            "ASUS_AI2401", "WW_AI2401", "ASUS_AI2401",
            "qcom", "kalama", "SM8650",
            "qcom", "kalama", "sm8650",
            "arm64-v8a",
            "asus/WW_AI2401/ASUS_AI2401:14/UP1A.231005.007/36.0210.0210.238-0:user/release-keys",
            "WW_AI2401-36.0210.0210.238",
            34, "Adreno (TM) 750", "adreno", "196610", 16384, 165
        ));

        // ROG Phone 7 Ultimate — Snapdragon 8 Gen 2, 165 Hz
        list.add(new SpoofProfile(
            "asus_rog7_ultimate",
            "ASUS ROG Phone 7 Ultimate (165Hz, Snapdragon 8 Gen 2)",
            "ASUS ROG",
            "ASUS_AI2205", "asus", "asus",
            "ASUS_AI2205", "WW_AI2205", "ASUS_AI2205",
            "qcom", "kalama", "SM8550",
            "qcom", "kalama", "sm8550",
            "arm64-v8a",
            "asus/WW_AI2205/ASUS_AI2205:13/TKQ1.221013.001/35.0804.0804.156-0:user/release-keys",
            "WW_AI2205-35.0804.0804.156",
            33, "Adreno (TM) 740", "adreno", "196610", 16384, 165
        ));

        // ROG Phone 6 — Snapdragon 8+ Gen 1, 165 Hz
        list.add(new SpoofProfile(
            "asus_rog6",
            "ASUS ROG Phone 6 (165Hz, Snapdragon 8+ Gen 1)",
            "ASUS ROG",
            "ASUS_AI2201", "asus", "asus",
            "ASUS_AI2201", "WW_AI2201", "ASUS_AI2201",
            "qcom", "taro", "SM8475",
            "qcom", "taro", "sm8475",
            "arm64-v8a",
            "asus/WW_AI2201/ASUS_AI2201:13/TP1A.220624.014/33.0810.0810.321-0:user/release-keys",
            "WW_AI2201-33.0810.0810.321",
            33, "Adreno (TM) 730", "adreno", "196610", 12288, 165
        ));

        // ROG Phone 5s — Snapdragon 888+, 144 Hz
        list.add(new SpoofProfile(
            "asus_rog5s",
            "ASUS ROG Phone 5s (144Hz, Snapdragon 888+)",
            "ASUS ROG",
            "ASUS_I007D", "asus", "asus",
            "ASUS_I007D", "WW_I007D", "ASUS_I007D",
            "qcom", "lahaina", "SM8350",
            "qcom", "lahaina", "sm8350",
            "arm64-v8a",
            "asus/WW_I007D/ASUS_I007D:13/TKQ1.221013.001/31.0810.0810.219-0:user/release-keys",
            "WW_I007D-31.0810.0810.219",
            33, "Adreno (TM) 660", "adreno", "196610", 12288, 144
        ));

        return list;
    }
}
