package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class XiaomiProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. Xiaomi 15 Ultra (Snapdragon 8 Elite / 16GB LPDDR5X / 120Hz LTPO AMOLED)
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
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm lrcpc dcpop sha3 asimddp sha512 sve asimdfhm",
                "Xiaomi/xuanyuan_global/xuanyuan:15/UKQ1.231003.002/HyperOS2.0:user/release-keys",
                "HyperOS2.0",
                "15",
                35,
                "2025-02-25",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                13107,
                120
        ));

        // 2. Redmi K80 Pro (Snapdragon 8 Elite / 16GB RAM / 120Hz OLED)
        list.add(new SpoofProfile(
                "redmi_k80_pro",
                "Redmi K80 Pro (Snapdragon 8 Elite)",
                "Xiaomi",
                "24122RKC7C",
                "Redmi",
                "Xiaomi",
                "miro",
                "miro_global",
                "miro",
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
                "Redmi/miro_global/miro:15/UKQ1.231003.002/HyperOS2.0:user/release-keys",
                "HyperOS2.0",
                "15",
                35,
                "2024-11-27",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                13107,
                120
        ));

        // 3. POCO F7 Ultra (Snapdragon 8 Elite / 16GB RAM / 120Hz AMOLED)
        list.add(new SpoofProfile(
                "poco_f7_ultra",
                "POCO F7 Ultra (Snapdragon 8 Elite)",
                "Xiaomi",
                "24122RKC7G",
                "POCO",
                "Xiaomi",
                "miro",
                "miro_poco",
                "miro",
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
                "POCO/miro_poco/miro:15/UKQ1.231003.002/HyperOS2.0:user/release-keys",
                "HyperOS2.0",
                "15",
                35,
                "2025-03-01",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                13107,
                120
        ));

        // 4. Xiaomi 15 Pro (Snapdragon 8 Elite / 16GB RAM / 120Hz LTPO)
        list.add(new SpoofProfile(
                "xiaomi_15_pro",
                "Xiaomi 15 Pro (Snapdragon 8 Elite)",
                "Xiaomi",
                "25010PN0DG",
                "Xiaomi",
                "Xiaomi",
                "dada",
                "dada_global",
                "dada",
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
                "Xiaomi/dada_global/dada:15/UKQ1.231003.002/HyperOS2.1:user/release-keys",
                "HyperOS2.1",
                "15",
                35,
                "2024-10-29",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                13107,
                120
        ));

        // 5. Xiaomi 14 Ultra (Snapdragon 8 Gen 3 / 16GB RAM / 120Hz)
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

        return list;
    }
}

