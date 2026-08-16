package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class VivoProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. Vivo iQOO 15 Pro (Extreme Flagship - Snapdragon 8 Elite / Adreno 840 / 24GB RAM / 185Hz)
        list.add(new SpoofProfile(
                "iqoo_15_pro",
                "Vivo iQOO 15 Pro (Snapdragon 8 Elite / 24GB RAM / 185Hz)",
                "Vivo / iQOO",
                "V2508A",
                "vivo",
                "vivo",
                "V2508A",
                "V2508A",
                "V2508A",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "Qualcomm",
                8,
                4320000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp sve sve2",
                "vivo/V2508A/V2508A:15/UKQ1.241003.001/OriginOS6.0:user/release-keys",
                "OriginOS6.0",
                "15",
                35,
                "2025-02-01",
                "Adreno (TM) 840",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                24576,
                19660
        ));

        // 2. Vivo iQOO 13 (Snapdragon 8 Elite / Adreno 830 / 16GB RAM / 144Hz)
        list.add(new SpoofProfile(
                "iqoo_13",
                "Vivo iQOO 13 (Snapdragon 8 Elite / 16GB RAM / 144Hz)",
                "Vivo / iQOO",
                "V2408A",
                "vivo",
                "vivo",
                "V2408A",
                "V2408A",
                "V2408A",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "Qualcomm",
                8,
                4320000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "vivo/V2408A/V2408A:15/UKQ1.231003.002/OriginOS5.0:user/release-keys",
                "OriginOS5.0",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                12288
        ));

        // 3. Vivo iQOO Neo 10 Pro (MediaTek Dimensity 9400 / Immortalis-G925)
        list.add(new SpoofProfile(
                "iqoo_neo10_pro",
                "Vivo iQOO Neo 10 Pro (Dimensity 9400 / 16GB RAM / 144Hz)",
                "Vivo / iQOO",
                "V2426A",
                "vivo",
                "vivo",
                "V2426A",
                "V2426A",
                "V2426A",
                "mt6991",
                "mt6991",
                "MediaTek Dimensity 9400",
                "k6991v1_64",
                "Dimensity 9400",
                "MediaTek",
                8,
                3620000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "vivo/V2426A/V2426A:15/UKQ1.240810.001/OriginOS5.0:user/release-keys",
                "OriginOS5.0",
                "15",
                35,
                "2025-01-01",
                "ARM Immortalis-G925 MC12",
                "ARM",
                "OpenGL ES 3.2 v1.r48p0",
                "1.3.280",
                "512.0.0",
                16384,
                12288
        ));

        return list;
    }
}
