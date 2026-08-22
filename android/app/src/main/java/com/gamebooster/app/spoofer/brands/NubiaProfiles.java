package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class NubiaProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. Nubia REDMAGIC 10 Pro+ (Snapdragon 8 Elite + Red Core R3 / 24GB LPDDR5X / 144Hz AMOLED)
        list.add(new SpoofProfile(
                "redmagic_10_pro_plus",
                "REDMAGIC 10 Pro+ (Snapdragon 8 Elite / 24GB)",
                "Nubia",
                "NX789J_Plus",
                "nubia",
                "nubia",
                "NX789J",
                "NX789J",
                "NX789J",
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
                "nubia/NX789J/NX789J:15/UKQ1.231003.002/REDMAGICOS10.0:user/release-keys",
                "REDMAGICOS10.0",
                "15",
                35,
                "2024-11-20",
                "Adreno (TM) 840",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                24576,
                20480,
                144
        ));

        // 2. Nubia REDMAGIC 10 Pro (Snapdragon 8 Elite + Red Core R3 / 16GB / 144Hz)
        list.add(new SpoofProfile(
                "redmagic_10_pro",
                "REDMAGIC 10 Pro (Snapdragon 8 Elite / 16GB)",
                "Nubia",
                "NX789J",
                "nubia",
                "nubia",
                "NX789J",
                "NX789J",
                "NX789J",
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
                "nubia/NX789J/NX789J:15/UKQ1.231003.002/REDMAGICOS10.0:user/release-keys",
                "REDMAGICOS10.0",
                "15",
                35,
                "2024-11-20",
                "Adreno (TM) 840",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                13107,
                144
        ));

        // 3. Nubia Z70 Ultra (Snapdragon 8 Elite / 16GB / 144Hz 1.5K)
        list.add(new SpoofProfile(
                "nubia_z70_ultra",
                "Nubia Z70 Ultra (Snapdragon 8 Elite)",
                "Nubia",
                "NX733J",
                "nubia",
                "nubia",
                "NX733J",
                "NX733J",
                "NX733J",
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
                "nubia/NX733J/NX733J:15/UKQ1.231003.002/MyOS15.0:user/release-keys",
                "MyOS15.0",
                "15",
                35,
                "2024-11-25",
                "Adreno (TM) 830",
                "Qualcomm",
                "OpenGL ES 3.2 V@0615.0",
                "1.3.280",
                "512.615.0",
                16384,
                13107,
                144
        ));

        // 4. Nubia REDMAGIC 9S Pro (Snapdragon 8 Gen 3 Leading Version 3.4GHz / 16GB / 120Hz)
        list.add(new SpoofProfile(
                "redmagic_9s_pro",
                "REDMAGIC 9S Pro (Snapdragon 8 Gen 3 Leading)",
                "Nubia",
                "NX769J_Plus",
                "nubia",
                "nubia",
                "NX769J",
                "NX769J",
                "NX769J",
                "qcom",
                "pineapple",
                "SM8650-AC",
                "pineapple",
                "Snapdragon 8 Gen 3 Leading Version",
                "Qualcomm",
                8,
                3400000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "nubia/NX769J/NX769J:14/UKQ1.230917.001/REDMAGICOS9.5:user/release-keys",
                "REDMAGICOS9.5",
                "14",
                34,
                "2024-07-01",
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
