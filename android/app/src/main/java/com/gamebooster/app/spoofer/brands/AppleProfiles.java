package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * AppleProfiles — Apple iOS/iPadOS 120Hz ProMotion gaming device profiles.
 * Used for cross-platform games with iOS-exclusive 120 FPS high graphics profiles.
 */
public class AppleProfiles {
    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // 1. Apple iPhone 16 Pro Max (Apple A18 Pro / 6-core GPU / 8GB LPDDR5X / 120Hz ProMotion)
        list.add(new SpoofProfile(
                "iphone_16_pro_max",
                "iPhone 16 Pro Max (Apple A18 Pro / 120Hz)",
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
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm lrcpc dcpop sha3 asimddp sha512 sve asimdfhm",
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

        // 2. Apple iPhone 15 Pro Max (Apple A17 Pro / 6-core GPU / 8GB RAM / 120Hz ProMotion)
        list.add(new SpoofProfile(
                "iphone_15_pro_max",
                "iPhone 15 Pro Max (Apple A17 Pro / 120Hz)",
                "Apple",
                "iPhone16,2",
                "Apple",
                "Apple",
                "iPhone16,2",
                "iPhone16,2",
                "iPhone16,2",
                "Apple A17 Pro",
                "t8130",
                "A17 Pro",
                "t8130",
                "Apple A17 Pro",
                "Apple",
                6,
                3780000,
                "ARM64-v9.2-A",
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp",
                "Apple/iPhone16,2/iPhone16,2:17.0/21A329:user/release-keys",
                "21A329",
                "17.0",
                34,
                "2023-09-22",
                "Apple A17 Pro GPU",
                "Apple",
                "OpenGL ES 3.0 Metal",
                "1.3.0",
                "Metal 3",
                8192,
                6144,
                120
        ));

        // 3. Apple iPad Pro 13" (Apple M4 10-Core / 16GB LPDDR5X / 120Hz Tandem OLED)
        list.add(new SpoofProfile(
                "ipad_pro_m4",
                "iPad Pro 13\" (Apple M4 10-Core / 120Hz)",
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
                "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm lrcpc dcpop sha3 asimddp sha512 sve asimdfhm",
                "Apple/iPad16,5/iPad16,5:18.0/22A3354:user/release-keys",
                "22A3354",
                "18.0",
                35,
                "2024-05-15",
                "Apple M4 GPU (10-core)",
                "Apple",
                "OpenGL ES 3.0 Metal",
                "1.3.0",
                "Metal 3",
                16384,
                13107,
                120
        ));

        return list;
    }
}
