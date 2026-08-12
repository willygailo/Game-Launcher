package com.gamebooster.app.feature.spoofer.brands;

import com.gamebooster.app.feature.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Tecno device spoof profiles.
 * Sourced from real-world getprop output on Tecno gaming devices (HiOS).
 */
public class TecnoProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Tecno Camon 30 Pro 5G — Dimensity 8200 Ultimate, 144Hz, 12GB RAM
        list.add(new SpoofProfile(
            "tecno_camon_30_pro",
            "Tecno Camon 30 Pro 5G (144Hz, Dimensity 8200 Ultimate, 12GB RAM)",
            "Tecno",
            "CL8", "TECNO", "TECNO",
            "TECNO-CL8", "CL8-GL", "TECNO-CL8",
            "mt6896", "mt6896", "Dimensity8200",
            "MediaTek", "mt6896", "MT6896",
            "arm64-v8a",
            "TECNO/CL8-GL/TECNO-CL8:14/UP1A.231005.007/CL8-V1014:user/release-keys",
            "CL8-V1014",
            "Mali-G610 MC6", "mali", "196610", 12288
        ));

        // Tecno Pova 6 Pro 5G — Dimensity 6080, 120Hz, 12GB RAM
        list.add(new SpoofProfile(
            "tecno_pova_6_pro",
            "Tecno Pova 6 Pro 5G (120Hz, Dimensity 6080, 12GB RAM)",
            "Tecno",
            "LI7", "TECNO", "TECNO",
            "TECNO-LI7", "LI7-GL", "TECNO-LI7",
            "mt6833", "mt6833", "Dimensity6080",
            "MediaTek", "mt6833", "MT6833",
            "arm64-v8a",
            "TECNO/LI7-GL/TECNO-LI7:14/UP1A.231005.007/LI7-V822:user/release-keys",
            "LI7-V822",
            "Mali-G57 MC2", "mali", "196610", 12288
        ));

        // Tecno Phantom V Fold — Dimensity 9000+, 120Hz, 12GB RAM
        list.add(new SpoofProfile(
            "tecno_phantom_v_fold",
            "Tecno Phantom V Fold (120Hz, Dimensity 9000+, 12GB RAM)",
            "Tecno",
            "AD10", "TECNO", "TECNO",
            "TECNO-AD10", "AD10-GL", "TECNO-AD10",
            "mt6983", "mt6983", "Dimensity9000Plus",
            "MediaTek", "mt6983", "MT6983",
            "arm64-v8a",
            "TECNO/AD10-GL/TECNO-AD10:13/TP1A.220624.014/AD10-V654:user/release-keys",
            "AD10-V654",
            "Mali-G710 MC10", "mali", "196610", 12288
        ));

        return list;
    }
}
