package com.gamebooster.app.feature.spoofer.brands;

import com.gamebooster.app.feature.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Pixel device spoof profiles.
 * Real-world getprop values for Pixel 9 Pro XL and Pixel 8 Pro.
 */
public class PixelProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Google Pixel 9 Pro XL — Tensor G4, Mali-G715, 16GB RAM, 120Hz
        list.add(new SpoofProfile(
            "pixel_9_pro_xl",
            "Google Pixel 9 Pro XL (120Hz, Tensor G4, Mali-G715, 16GB RAM)",
            "Google",
            "Pixel 9 Pro XL", "google", "Google",
            "komodo", "komodo", "Pixel 9 Pro XL",
            "google", "zumapro", "Tensor G4",
            "Google", "komodo", "comet",
            "arm64-v8a",
            "google/komodo/komodo:15/AP2A.240805.005/1234567:user/release-keys",
            "AP2A.240805.005",
            35, "Mali-G715", "mali", "196610", 16384, 120
        ));

        // Google Pixel 8 Pro — Tensor G3, Mali-G715, 12GB RAM, 120Hz
        list.add(new SpoofProfile(
            "pixel_8_pro",
            "Google Pixel 8 Pro (120Hz, Tensor G3, Mali-G715, 12GB RAM)",
            "Google",
            "Pixel 8 Pro", "google", "Google",
            "husky", "husky", "Pixel 8 Pro",
            "google", "zuma", "Tensor G3",
            "Google", "husky", "shiba",
            "arm64-v8a",
            "google/husky/husky:14/UD1A.230803.022/10820060:user/release-keys",
            "UD1A.230803.022",
            34, "Mali-G715", "mali", "196610", 12288, 120
        ));

        return list;
    }
}
