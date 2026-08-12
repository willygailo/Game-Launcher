package com.gamebooster.app.feature.spoofer.brands;

import com.gamebooster.app.feature.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * OPPO device spoof profiles.
 * Real-world getprop values for OPPO Find X and Reno series.
 */
public class OppoProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // OPPO Find X7 Ultra — Snapdragon 8 Gen 3
        list.add(new SpoofProfile(
            "oppo_find_x7_ultra",
            "OPPO Find X7 Ultra (Snapdragon 8 Gen 3)",
            "OPPO",
            "PHZ110", "OPPO", "OPPO",
            "PHZ110", "PHZ110", "PHZ110",
            "qcom", "kalama", "SM8650",
            "kalama", "sm8650",
            "OPPO/PHZ110/PHZ110:14/UP1A.231005.007/S.202406251826:user/release-keys",
            "PHZ110_14.0.0.600(CN01)",
            "Adreno (TM) 750"
        ));

        // OPPO Find X6 Pro — Dimensity 9200
        list.add(new SpoofProfile(
            "oppo_find_x6_pro",
            "OPPO Find X6 Pro (Dimensity 9200)",
            "OPPO",
            "PGEM10", "OPPO", "OPPO",
            "PGEM10", "PGEM10", "PGEM10",
            "mt6985", "mt6985", "Dimensity9200",
            "mt6985", "mt6985",
            "OPPO/PGEM10/PGEM10:13/TP1A.220624.014/S.202309251512:user/release-keys",
            "PGEM10_13.0.0.501(CN01)",
            "Mali-G715 MC11"
        ));

        // OPPO Reno 12 Pro — Dimensity 7300 Energy
        list.add(new SpoofProfile(
            "oppo_reno12_pro",
            "OPPO Reno 12 Pro (Dimensity 7300 Energy)",
            "OPPO",
            "CPH2585", "OPPO", "OPPO",
            "CPH2585", "CPH2585", "CPH2585",
            "mt6893", "mt6893", "Dimensity7300Energy",
            "mt6893", "mt6893",
            "OPPO/CPH2585/CPH2585:14/UP1A.231005.007/S.202405201200:user/release-keys",
            "CPH2585_14.0.0.300(EX01)",
            "Mali-G615 MC2"
        ));

        return list;
    }
}
