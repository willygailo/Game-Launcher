package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * AsusRogProfiles — ASUS Republic of Gamers flagship gaming device profiles.
 * Features 100% authentic, legally whitelisted hardware parameters and maximum refresh rates (185Hz / 165Hz).
 */
public class AsusRogProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. ASUS ROG Phone 9 Pro (Snapdragon 8 Elite / Adreno 830 / 16GB LPDDR5X / 185Hz AMOLED)
        list.add(new SpoofProfile(
                "asus_rog9_pro",
                "ASUS ROG Phone 9 Pro (Snapdragon 8 Elite / 185Hz)",
                "ASUS ROG",
                "ASUS_AI2501_Pro",
                "asus",
                "asus",
                "ASUS_AI2501",
                "WW_AI2501",
                "AI2501",
                "qcom",
                "sun",
                "SM8750-AB",
                "sun",
                "Snapdragon 8 Elite",
                "Qualcomm",
                8,
                4320000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm lrcpc dcpop sha3 asimddp sha512 sve asimdfhm",
                "asus/WW_AI2501/ASUS_AI2501:15/UKQ1.231003.002/35.0810.0810.27:user/release-keys",
                "35.0810.0810.27",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                13107,
                185
        ));

        // 2. ASUS ROG Phone 8 Pro (Snapdragon 8 Gen 3 / Adreno 750 / 24GB RAM / 165Hz LTPO)
        list.add(new SpoofProfile(
                "asus_rog8_pro",
                "ASUS ROG Phone 8 Pro (Snapdragon 8 Gen 3 / 165Hz)",
                "ASUS ROG",
                "ASUS_AI2401",
                "asus",
                "asus",
                "ASUS_AI2401",
                "WW_AI2401",
                "AI2401",
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
                "asus/WW_AI2401/ASUS_AI2401:14/UKQ1.230917.001/34.1420.1420.327:user/release-keys",
                "34.1420.1420.327",
                "14",
                34,
                "2024-06-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0530.0",
                "1.3.275",
                "512.530.0",
                24576,
                18432,
                165
        ));

        // 3. ASUS ROG Phone 7 Ultimate (Snapdragon 8 Gen 2 / Adreno 740 / 16GB RAM / 165Hz)
        list.add(new SpoofProfile(
                "asus_rog7_ultimate",
                "ASUS ROG Phone 7 Ultimate (Snapdragon 8 Gen 2 / 165Hz)",
                "ASUS ROG",
                "ASUS_AI2205_D",
                "asus",
                "asus",
                "ASUS_AI2205",
                "WW_AI2205",
                "AI2205",
                "qcom",
                "kalama",
                "SM8550-AB",
                "kalama",
                "Snapdragon 8 Gen 2",
                "Qualcomm",
                8,
                3200000,
                "ARM64-v9-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "asus/WW_AI2205/ASUS_AI2205:14/UKQ1.230917.001/33.0820.0820.210:user/release-keys",
                "33.0820.0820.210",
                "14",
                34,
                "2024-01-15",
                "Adreno (TM) 740",
                "Qualcomm",
                "OpenGL ES 3.2 V@0512.0",
                "1.3.250",
                "512.512.0",
                16384,
                12288,
                165
        ));

        return list;
    }
}
