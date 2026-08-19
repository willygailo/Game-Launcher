package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class AsusRogProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // ASUS ROG Phone 9 Pro (Snapdragon 8 Elite / 24GB LPDDR5X / 185Hz AMOLED)
        list.add(new SpoofProfile(
                "asus_rog9_pro",
                "ASUS ROG Phone 9 Pro (185Hz Extreme)",
                "ASUS ROG",
                "ASUS_AI2501",
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
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "asus/WW_AI2501/ASUS_AI2501:15/UKQ1.231003.002/35.0810.0810.27:user/release-keys",
                "35.0810.0810.27",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0 (GIT@56860db, Idd24e5256e)",
                "1.3.280",
                "512.615.0",
                24576,
                19660,
        185
        ));

        // ASUS ROG Phone 8 Pro (Snapdragon 8 Gen 3 / 24GB RAM / 165Hz)
        list.add(new SpoofProfile(
                "asus_rog8_pro",
                "ASUS ROG Phone 8 Pro (165Hz eSports)",
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
        185
        ));

        return list;
    }
}
