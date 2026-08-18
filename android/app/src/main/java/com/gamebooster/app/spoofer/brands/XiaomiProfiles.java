package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class XiaomiProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Xiaomi 15 Ultra (Snapdragon 8 Elite / 16GB LPDDR5X / HyperOS 2.0)
        list.add(new SpoofProfile(
                "xiaomi_15_ultra",
                "Xiaomi 15 Ultra (Snapdragon 8 Elite)",
                "Xiaomi",
                "25010PN30G",
                "Xiaomi",
                "Xiaomi",
                "xuanyuan",
                "xuanyuan_global",
                "xuanyuan",
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
                "Xiaomi/xuanyuan_global/xuanyuan:15/UKQ1.231003.002/HyperOS2.0:user/release-keys",
                "HyperOS2.0",
                "15",
                35,
                "2025-01-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                12288,
        120
        ));

        // Xiaomi 14 Ultra (Snapdragon 8 Gen 3 / 16GB RAM / HyperOS 1.0)
        list.add(new SpoofProfile(
                "xiaomi_14_ultra",
                "Xiaomi 14 Ultra (Snapdragon 8 Gen 3)",
                "Xiaomi",
                "24030PN60G",
                "Xiaomi",
                "Xiaomi",
                "aurora",
                "aurora_global",
                "aurora",
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
                "Xiaomi/aurora_global/aurora:14/UKQ1.230917.001/HyperOS1.0:user/release-keys",
                "HyperOS1.0",
                "14",
                34,
                "2024-03-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0530.0",
                "1.3.275",
                "512.530.0",
                16384,
                12288,
        120
        ));

        // Xiaomi 15 Pro (Snapdragon 8 Elite / 12GB RAM / HyperOS 2.0 / 120Hz)
        list.add(new SpoofProfile(
                "xiaomi_15_pro",
                "Xiaomi 15 Pro (Snapdragon 8 Elite)",
                "Xiaomi",
                "25010PN0DG", "Xiaomi", "Xiaomi",
                "dada", "dada_global", "dada",
                "qcom", "sun", "SM8750-AB", "sun",
                "Snapdragon 8 Elite",
                "Qualcomm", 8, 4320000, "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Xiaomi/dada_global/dada:15/UKQ1.231003.002/HyperOS2.1:user/release-keys",
                "HyperOS2.1", "15", 35, "2025-02-01",
                "Adreno (TM) 830", "Qualcomm",
                "OpenGL ES 3.2 V@0615.0 (GIT@56860db)",
                "1.3.280", "512.615.0",
                12288, 9216, 120
        ));

        return list;
    }
}

