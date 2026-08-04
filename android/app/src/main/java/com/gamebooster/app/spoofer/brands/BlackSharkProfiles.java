package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Black Shark device spoof profiles.
 * Real-world getprop values for Black Shark gaming series.
 */
public class BlackSharkProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Black Shark 5 Pro — Snapdragon 8 Gen 1, 144 Hz
        list.add(new SpoofProfile(
            "black_shark_5_pro",
            "Black Shark 5 Pro (144Hz, Snapdragon 8 Gen 1)",
            "Black Shark",
            "SHARK PAR-A0", "blackshark", "blackshark",
            "PAR-A0", "PAR-A0", "PAR-A0",
            "qcom", "taro", "SM8450",
            "taro", "sm8450",
            "blackshark/PAR-A0/PAR-A0:13/TP1A.220624.014/V14.9.5.0.1:user/release-keys",
            "SHARK-PAR-A0-V14.9.5.0.1",
            "Adreno (TM) 730"
        ));

        // Black Shark 4 Pro — Snapdragon 888, 144 Hz
        list.add(new SpoofProfile(
            "black_shark_4_pro",
            "Black Shark 4 Pro (144Hz, Snapdragon 888)",
            "Black Shark",
            "SHARK KSR-A0", "blackshark", "blackshark",
            "KSR-A0", "KSR-A0", "KSR-A0",
            "qcom", "lahaina", "SM8350",
            "lahaina", "sm8350",
            "blackshark/KSR-A0/KSR-A0:12/SKQ1.211006.001/V13.1.5.0.1:user/release-keys",
            "SHARK-KSR-A0-V13.1.5.0.1",
            "Adreno (TM) 660"
        ));

        // Black Shark 5 — Snapdragon 870, 144 Hz
        list.add(new SpoofProfile(
            "black_shark_5",
            "Black Shark 5 (144Hz, Snapdragon 870)",
            "Black Shark",
            "SHARK MFAB-A0", "blackshark", "blackshark",
            "MFAB-A0", "MFAB-A0", "MFAB-A0",
            "qcom", "taro", "SM7325",
            "taro", "sm7325",
            "blackshark/MFAB-A0/MFAB-A0:12/SKQ1.211006.001/V13.9.5.0.1:user/release-keys",
            "SHARK-MFAB-A0-V13.9.5.0.1",
            "Adreno (TM) 642L"
        ));

        return list;
    }
}
