package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Infinix device spoof profiles.
 * Sourced from real-world getprop output on Infinix gaming devices (XOS).
 */
public class InfinixProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Infinix GT 20 Pro — Dimensity 8200 Ultimate, 144Hz, 12GB RAM
        list.add(new SpoofProfile(
            "infinix_gt_20_pro",
            "Infinix GT 20 Pro (144Hz, Dimensity 8200 Ultimate, 12GB RAM)",
            "Infinix",
            "X6871", "Infinix", "Infinix",
            "Infinix-X6871", "X6871-GL", "Infinix-X6871",
            "mt6896", "mt6896", "Dimensity8200",
            "MediaTek", "mt6896", "MT6896",
            "arm64-v8a",
            "Infinix/X6871-GL/Infinix-X6871:14/UP1A.231005.007/X6871-V1201:user/release-keys",
            "X6871-V1201",
            "Mali-G610 MC6", "mali", "196610", 12288
        ));

        // Infinix GT 10 Pro — Dimensity 8050, 120Hz, 16GB RAM
        list.add(new SpoofProfile(
            "infinix_gt_10_pro",
            "Infinix GT 10 Pro (120Hz, Dimensity 8050, 16GB RAM)",
            "Infinix",
            "X6739", "Infinix", "Infinix",
            "Infinix-X6739", "X6739-GL", "Infinix-X6739",
            "mt6893", "mt6893", "Dimensity8050",
            "MediaTek", "mt6893", "MT6893",
            "arm64-v8a",
            "Infinix/X6739-GL/Infinix-X6739:13/TP1A.220624.014/X6739-V810:user/release-keys",
            "X6739-V810",
            "Mali-G77 MC9", "mali", "196610", 16384
        ));

        // Infinix Zero 30 5G — Dimensity 8020, 144Hz
        list.add(new SpoofProfile(
            "infinix_zero_30_5g",
            "Infinix Zero 30 5G (144Hz, Dimensity 8020)",
            "Infinix",
            "X6731B", "Infinix", "Infinix",
            "Infinix-X6731B", "X6731B-GL", "Infinix-X6731B",
            "mt6891", "mt6891", "Dimensity8020",
            "MediaTek", "mt6891", "MT6891",
            "arm64-v8a",
            "Infinix/X6731B-GL/Infinix-X6731B:13/TP1A.220624.014/X6731B-V512:user/release-keys",
            "X6731B-V512",
            "Mali-G77 MC9", "mali", "196610", 12288
        ));

        return list;
    }
}
