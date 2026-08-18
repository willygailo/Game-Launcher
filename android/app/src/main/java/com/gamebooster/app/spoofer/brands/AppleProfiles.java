package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

public class AppleProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // Apple iPhone 16 Pro Max (Apple A18 Pro / 6-core GPU / 8GB LPDDR5X)
        list.add(new SpoofProfile(
                "iphone_16_pro_max",
                "iPhone 16 Pro Max (Apple A18 Pro)",
                "Apple",
                "iPhone17,2",
                "Apple",
                "Apple",
                "iPhone17,2",
                "iPhone17,2",
                "iPhone17,2",
                "Apple A18 Pro",
                "t8140",
                "A18 Pro",
                "t8140",
                "Apple A18 Pro",
                "Apple",
                6,
                4040000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Apple/iPhone17,2/iPhone17,2:18.0/22A3354:user/release-keys",
                "22A3354",
                "18.0",
                35,
                "2024-09-01",
                "Apple A18 Pro GPU",
                "Apple",
                "OpenGL ES 3.0 Metal",
                "1.3.0",
                "Metal 3",
                8192,
                6144,
        120
        ));

        // Apple iPad Pro 13" (Apple M4 10-Core / 16GB LPDDR5X-7500)
        list.add(new SpoofProfile(
                "ipad_pro_m4",
                "iPad Pro 13\" (Apple M4 10-Core)",
                "Apple",
                "iPad16,5",
                "Apple",
                "Apple",
                "iPad16,5",
                "iPad16,5",
                "iPad16,5",
                "Apple M4",
                "t8132",
                "Apple M4",
                "t8132",
                "Apple M4 (10-Core)",
                "Apple",
                10,
                4400000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Apple/iPad16,5/iPad16,5:18.0/22A3354:user/release-keys",
                "22A3354",
                "18.0",
                35,
                "2024-09-01",
                "Apple M4 GPU (10-core)",
                "Apple",
                "OpenGL ES 3.0 Metal",
                "1.3.0",
                "Metal 3",
                16384,
                12288,
        120
        ));

        return list;
    }
}
