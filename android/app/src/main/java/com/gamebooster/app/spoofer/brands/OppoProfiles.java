package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class OppoProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // OPPO Find X8 Pro (Dimensity 9400 / 16GB LPDDR5X / ColorOS 15.0)
        list.add(new SpoofProfile(
                "oppo_find_x8_pro",
                "OPPO Find X8 Pro (Dimensity 9400)",
                "OPPO",
                "PKC110",
                "OPPO",
                "OPPO",
                "OP5D1FL1",
                "PKC110",
                "PKC110",
                "mt6991",
                "mt6991",
                "MT6991",
                "k6991v1_64",
                "Dimensity 9400",
                "MediaTek",
                8,
                3630000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "OPPO/PKC110/OP5D1FL1:15/UKQ1.231003.002/ColorOS15.0:user/release-keys",
                "ColorOS15.0",
                "15",
                35,
                "2025-01-01",
                "Immortalis-G925 MC12",
                "ARM",
                "OpenGL ES 3.2 v1.r48p0-01eac0.53d3b762ca1387d85c8e31e5f8ec4c27",
                "1.3.275",
                "20.0.0",
                16384,
                12288
        ));

        // OPPO Find X7 Ultra (Snapdragon 8 Gen 3 / 16GB RAM / ColorOS 14.0)
        list.add(new SpoofProfile(
                "oppo_find_x7_ultra",
                "OPPO Find X7 Ultra (Snapdragon 8 Gen 3)",
                "OPPO",
                "PHY110",
                "OPPO",
                "OPPO",
                "OP595FL1",
                "PHY110",
                "PHY110",
                "qcom",
                "pineapple",
                "SM8650-AB",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "Qualcomm",
                8,
                3300000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "OPPO/PHY110/OP595FL1:14/UKQ1.230917.001/ColorOS14.0:user/release-keys",
                "ColorOS14.0",
                "14",
                34,
                "2024-02-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0530.0",
                "1.3.275",
                "512.530.0",
                16384,
                12288
        ));

        return list;
    }
}
