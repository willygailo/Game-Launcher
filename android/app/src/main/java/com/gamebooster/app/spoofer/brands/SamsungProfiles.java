package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class SamsungProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Samsung Galaxy S25 Ultra (Snapdragon 8 Elite for Galaxy / Adreno 830 / 16GB RAM)
        list.add(new SpoofProfile(
                "samsung_s25_ultra",
                "Samsung Galaxy S25 Ultra (Snapdragon 8 Elite)",
                "Samsung",
                "SM-S938B",
                "samsung",
                "samsung",
                "e2q",
                "e2qxxx",
                "e2q",
                "qcom",
                "sun",
                "SM8750-AC",
                "sun",
                "Snapdragon 8 Elite for Galaxy",
                "Qualcomm",
                8,
                4470000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "samsung/e2qxxx/e2q:15/AP3A.240905.015/S938BXXU1AYB1:user/release-keys",
                "AP3A.240905.015.S938BXXU1AYB1",
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

        // Samsung Galaxy S24 Ultra (Snapdragon 8 Gen 3 for Galaxy / Adreno 750 / 12GB RAM)
        list.add(new SpoofProfile(
                "samsung_s24_ultra",
                "Samsung Galaxy S24 Ultra (Snapdragon 8 Gen 3)",
                "Samsung",
                "SM-S928B",
                "samsung",
                "samsung",
                "e1q",
                "e1qxxx",
                "e1q",
                "qcom",
                "pineapple",
                "SM8650-AC",
                "pineapple",
                "Snapdragon 8 Gen 3 for Galaxy",
                "Qualcomm",
                8,
                3390000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "samsung/e1qxxx/e1q:14/UP1A.231005.007/S928BXXU1AXB5:user/release-keys",
                "UP1A.231005.007.S928BXXU1AXB5",
                "14",
                34,
                "2024-03-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0530.0",
                "1.3.275",
                "512.530.0",
                12288,
                9216
        ));

        // Samsung Galaxy S23 Ultra (Snapdragon 8 Gen 2 for Galaxy / Adreno 740 / 12GB RAM)
        list.add(new SpoofProfile(
                "samsung_s23_ultra",
                "Samsung Galaxy S23 Ultra (Snapdragon 8 Gen 2)",
                "Samsung",
                "SM-S918B",
                "samsung",
                "samsung",
                "dm3q",
                "dm3qxxx",
                "dm3q",
                "qcom",
                "kalama",
                "SM8550-AC",
                "kalama",
                "Snapdragon 8 Gen 2 for Galaxy",
                "Qualcomm",
                8,
                3360000,
                "ARM64-v9-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "samsung/dm3qxxx/dm3q:14/UP1A.231005.007/S918BXXU3BWJM:user/release-keys",
                "UP1A.231005.007.S918BXXU3BWJM",
                "14",
                34,
                "2023-11-01",
                "Adreno (TM) 740",
                "Qualcomm",
                "OpenGL ES 3.2 V@0512.0",
                "1.3.250",
                "512.512.0",
                12288,
                9216
        ));

        return list;
    }
}
