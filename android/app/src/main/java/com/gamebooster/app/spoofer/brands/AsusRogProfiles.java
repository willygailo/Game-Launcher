package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class AsusRogProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. ASUS ROG Phone 9 Pro (Snapdragon 8 Elite / 24GB RAM / 185Hz Extreme Gaming)
        list.add(new SpoofProfile(
                "asus_rog9_pro",
                "ASUS ROG Phone 9 Pro (Snapdragon 8 Elite / 24GB RAM / 185Hz)",
                "ASUS ROG",
                "ASUS_AI2401",
                "asus",
                "asus",
                "ASUS_AI2401",
                "WW_AI2401",
                "AI2401",
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
                "asus/WW_AI2401/ASUS_AI2401:15/UKQ1.231003.002/35.0810.0810.27:user/release-keys",
                "35.0810.0810.27",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                24576,
                19660
        ));

        // 2. ASUS ROG Phone 8 Pro (Snapdragon 8 Gen 3 / 16GB RAM / 165Hz)
        list.add(new SpoofProfile(
                "asus_rog8_pro",
                "ASUS ROG Phone 8 Pro (Snapdragon 8 Gen 3 / 16GB RAM / 165Hz)",
                "ASUS ROG",
                "ASUS_AI2401_A",
                "asus",
                "asus",
                "ASUS_AI2401_A",
                "WW_AI2401_A",
                "AI2401_A",
                "qcom",
                "pineapple",
                "SM8650",
                "pineapple",
                "Snapdragon 8 Gen 3",
                "Qualcomm",
                8,
                3300000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "asus/WW_AI2401_A/ASUS_AI2401_A:14/UKQ1.230917.001/34.1420.1420.327:user/release-keys",
                "34.1420.1420.327",
                "14",
                34,
                "2024-10-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0582.0",
                "1.3.275",
                "512.582.0",
                16384,
                12288
        ));

        return list;
    }
}
