package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class BlackSharkProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();
        list.add(new SpoofProfile(
                "blackshark_5_pro",
                "Xiaomi Black Shark 5 Pro",
                "Black Shark",
                "SHARK KTUS-H0",
                "blackshark",
                "Xiaomi",
                "ktus",
                "ktus",
                "ktus",
                "qcom",
                "taro",
                "SM8450",
                "taro",
                "Snapdragon 8 Gen 1",
                "blackshark/ktus/ktus:12/SKQ1.211006.001/JOYUI22.06.10:user/release-keys",
                "JOYUI22.06.10",
                "Adreno (TM) 730"
        ));
        return list;
    }
}
