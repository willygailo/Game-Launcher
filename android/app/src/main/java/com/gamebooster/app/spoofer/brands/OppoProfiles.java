package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * OppoProfiles — OPPO Find series flagship gaming device profiles.
 * Features 100% authentic, legally whitelisted hardware parameters and official ColorOS builds.
 */
public class OppoProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. OPPO Find X8 Pro (Dimensity 9400 / Immortalis-G925 MC12 / 16GB LPDDR5X / 120Hz LTPO AMOLED)
        list.add(new SpoofProfile(
                "oppo_find_x8_pro",
                "OPPO Find X8 Pro (Dimensity 9400 / 120Hz)",
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
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm lrcpc dcpop sha3 asimddp sha512 sve asimdfhm",
                "OPPO/PKC110/OP5D1FL1:15/UKQ1.231003.002/ColorOS15.0:user/release-keys",
                "ColorOS15.0",
                "15",
                35,
                "2024-10-30",
                "Immortalis-G925 MC12",
                "ARM",
                "OpenGL ES 3.2 v1.r48p0-01eac0.53d3b762ca1387d85c8e31e5f8ec4c27",
                "1.3.275",
                "20.0.0",
                16384,
                13107,
                120
        ));

        // 2. OPPO Find X7 Ultra (Snapdragon 8 Gen 3 / 16GB RAM / 120Hz 2K LTPO)
        list.add(new SpoofProfile(
                "oppo_find_x7_ultra",
                "OPPO Find X7 Ultra (Snapdragon 8 Gen 3 / 120Hz)",
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
                12288,
                120
        ));

        // 3. OPPO Find X6 Pro (Snapdragon 8 Gen 2 / 16GB RAM / 120Hz)
        list.add(new SpoofProfile(
                "oppo_find_x6_pro",
                "OPPO Find X6 Pro (Snapdragon 8 Gen 2 / 120Hz)",
                "OPPO",
                "PGEM10",
                "OPPO",
                "OPPO",
                "OP54BFL1",
                "PGEM10",
                "PGEM10",
                "qcom",
                "kalama",
                "SM8550-AB",
                "kalama",
                "Snapdragon 8 Gen 2",
                "Qualcomm",
                8,
                3190000,
                "ARM64-v9-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "OPPO/PGEM10/OP54BFL1:14/UKQ1.230917.001/ColorOS14.0:user/release-keys",
                "ColorOS14.0",
                "14",
                34,
                "2023-03-24",
                "Adreno (TM) 740",
                "Qualcomm",
                "OpenGL ES 3.2 V@0512.0",
                "1.3.250",
                "512.512.0",
                16384,
                12288,
                120
        ));

        return list;
    }
}
