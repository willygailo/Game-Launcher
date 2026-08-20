package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * LenovoLegionProfiles — Lenovo Legion gaming phone and tablet device spoof profiles.
 * Legion devices feature purpose-built gaming hardware with high refresh rates and active cooling.
 */
public class LenovoLegionProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. Lenovo Legion Y90 (Snapdragon 8 Gen 1 / 18GB LPDDR5 + SSD RAID 0 / 144Hz AMOLED / Dual Fan)
        list.add(new SpoofProfile(
                "lenovo_legion_y90",
                "Lenovo Legion Y90 (144Hz / 18GB Dual Fan)",
                "Lenovo Legion",
                "L71061",
                "Lenovo",
                "Lenovo",
                "TB-9707F",
                "L71061",
                "L71061",
                "qcom",
                "taro",
                "SM8450",
                "taro",
                "Snapdragon 8 Gen 1",
                "Qualcomm",
                8,
                3000000,
                "ARM64-v9.0-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Lenovo/L71061/TB-9707F:14/UKQ1.230917.001/ZUI_14.0:user/release-keys",
                "ZUI_14.0",
                "14",
                34,
                "2024-03-01",
                "Adreno (TM) 730",
                "Qualcomm",
                "OpenGL ES 3.2 V@0512.0",
                "1.3.250",
                "512.512.0",
                18432,
                14336,
                144
        ));

        // 2. Lenovo Legion Tab Gen 3 / Y700 (Snapdragon 8 Gen 3 / 16GB / 165Hz Gaming Display)
        list.add(new SpoofProfile(
                "lenovo_legion_y700_gen3",
                "Lenovo Legion Tab Gen 3 (Snapdragon 8 Gen 3 / 165Hz)",
                "Lenovo Legion",
                "TB321FU",
                "Lenovo",
                "Lenovo",
                "TB321FU",
                "TB321FU",
                "TB321FU",
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
                "Lenovo/TB321FU/TB321FU:15/UKQ1.231003.002/ZUI_16.0:user/release-keys",
                "ZUI_16.0",
                "15",
                35,
                "2024-10-01",
                "Adreno (TM) 750",
                "Qualcomm",
                "OpenGL ES 3.2 V@0530.0",
                "1.3.275",
                "512.530.0",
                16384,
                12288,
                165
        ));

        // 3. Lenovo Legion Y70 (Snapdragon 8+ Gen 1 / 16GB / 144Hz OLED)
        list.add(new SpoofProfile(
                "lenovo_legion_y70",
                "Lenovo Legion Y70 (144Hz / 16GB)",
                "Lenovo Legion",
                "L71091",
                "Lenovo",
                "Lenovo",
                "L71091",
                "L71091",
                "L71091",
                "qcom",
                "cape",
                "SM8475",
                "cape",
                "Snapdragon 8+ Gen 1",
                "Qualcomm",
                8,
                3190000,
                "ARM64-v9.0-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Lenovo/L71091/L71091:14/UKQ1.230917.001/ZUI_14.5:user/release-keys",
                "ZUI_14.5",
                "14",
                34,
                "2023-11-01",
                "Adreno (TM) 730",
                "Qualcomm",
                "OpenGL ES 3.2 V@0512.0",
                "1.3.250",
                "512.512.0",
                16384,
                12288,
                144
        ));

        return list;
    }
}
