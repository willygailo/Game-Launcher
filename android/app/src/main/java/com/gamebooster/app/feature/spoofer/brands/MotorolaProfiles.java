package com.gamebooster.app.feature.spoofer.brands;

import com.gamebooster.app.feature.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Motorola device spoof profiles.
 */
public class MotorolaProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Motorola Edge 50 Ultra — Snapdragon 8s Gen 3, 144Hz
        list.add(new SpoofProfile(
            "moto_edge50_ultra",
            "Motorola Edge 50 Ultra (144Hz, Snapdragon 8s Gen 3, 16GB RAM)",
            "Motorola",
            "XT2401-2", "motorola", "motorola",
            "edge50ultra", "edge50ultra_g", "edge50ultra",
            "qcom", "kalama", "SM8635",
            "qcom", "kalama", "sm8635",
            "arm64-v8a",
            "motorola/edge50ultra_g/edge50ultra:14/U3VZS34.19-25-3/01f46:user/release-keys",
            "U3VZS34.19-25-3",
            34, "Adreno (TM) 735", "adreno", "196610", 16384, 144
        ));

        // Motorola Moto G85 5G — Snapdragon 6s Gen 3, 120Hz
        list.add(new SpoofProfile(
            "moto_g85_5g",
            "Motorola Moto G85 5G (120Hz, Snapdragon 6s Gen 3, 12GB RAM)",
            "Motorola",
            "XT2427-1", "motorola", "motorola",
            "g85", "g85_g", "g85",
            "qcom", "holi", "SM6375",
            "qcom", "holi", "sm6375",
            "arm64-v8a",
            "motorola/g85_g/g85:14/U3VZS34.45-12/12a84:user/release-keys",
            "U3VZS34.45-12",
            34, "Adreno (TM) 619", "adreno", "196610", 12288, 120
        ));

        return list;
    }
}
