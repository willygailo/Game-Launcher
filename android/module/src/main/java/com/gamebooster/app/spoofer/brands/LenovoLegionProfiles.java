package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * LenovoLegionProfiles — Lenovo Legion gaming phone device spoof profiles.
 * Legion phones are purpose-built for gaming with ultra-high refresh rates and active cooling.
 */
public class LenovoLegionProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Lenovo Legion Tab Ultimate (Snapdragon 8 Elite / 24GB LPDDR5X / 185Hz AMOLED)
        list.add(new SpoofProfile(
                "lenovo_legion_ultimate",
                "Lenovo Legion Ultimate (185Hz / 24GB)",
                "Lenovo Legion",
                "L72091", "Lenovo", "Lenovo",
                "TB591FC", "L72091", "L72091",
                "qcom", "sun", "SM8750-AB", "sun",
                "Snapdragon 8 Elite",
                "Qualcomm", 8, 4320000, "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Lenovo/L72091/TB591FC:15/UKQ1.231003.002/S300623_231003_ROW:user/release-keys",
                "S300623_231003_ROW", "15", 35, "2025-01-01",
                "Adreno (TM) 830", "Qualcomm",
                "OpenGL ES 3.2 V@0615.0 (GIT@56860db, Idd24e5256e) (Date:11/24/24)",
                "1.3.280", "512.615.0",
                24576, 19660, 185
        ));

        // Lenovo Legion Phone 3i Pro (Snapdragon 8 Gen 2 / 16GB / 165Hz AMOLED)
        list.add(new SpoofProfile(
                "lenovo_legion_3i_pro",
                "Lenovo Legion Phone 3i Pro (165Hz / 16GB)",
                "Lenovo Legion",
                "L72031", "Lenovo", "Lenovo",
                "TB571FC", "L72031", "L72031",
                "qcom", "kalama", "SM8550-AB", "kalama",
                "Snapdragon 8 Gen 2",
                "Qualcomm", 8, 3360000, "ARM64-v9-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Lenovo/L72031/TB571FC:14/UKQ1.230917.001/S200423_230917_ROW:user/release-keys",
                "S200423_230917_ROW", "14", 34, "2024-06-01",
                "Adreno (TM) 740", "Qualcomm",
                "OpenGL ES 3.2 V@0512.0",
                "1.3.250", "512.512.0",
                16384, 12288, 185
        ));

        return list;
    }
}
