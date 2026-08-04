package com.gamebooster.app.spoofer.brands;

import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Apple device spoof profiles.
 * Spoof as iPad/iPhone to unlock Tablet FOV, Ultra Graphics, and iOS-tier asset quality in games.
 * Note: These identifiers are injected into Android build properties to fool game servers.
 */
public class AppleProfiles {

    public static List<SpoofProfile> getProfiles() {
        List<SpoofProfile> list = new ArrayList<>();

        // iPad Pro M4 — Tablet FOV + Ultra Graphics
        list.add(new SpoofProfile(
            "apple_ipad_pro_m4",
            "iPad Pro M4 (Tablet FOV + Ultra Graphics)",
            "Apple",
            "iPad16,3", "Apple", "Apple",
            "iPad16,3", "iPad16,3", "iPad16,3",
            "apple", "m4", "M4",
            "m4", "m4",
            "apple/iPad16,3/iPad16,3:18.0/22A3354/0:user/release-keys",
            "iPad16,3_18.0.1",
            "Apple GPU M4"
        ));

        // iPad Pro M2 — Tablet FOV + Extreme Graphics
        list.add(new SpoofProfile(
            "apple_ipad_pro_m2",
            "iPad Pro M2 (Tablet FOV + Extreme Graphics)",
            "Apple",
            "iPad13,8", "Apple", "Apple",
            "iPad13,8", "iPad13,8", "iPad13,8",
            "apple", "m2", "M2",
            "m2", "m2",
            "apple/iPad13,8/iPad13,8:17.0/21A340/0:user/release-keys",
            "iPad13,8_17.6.1",
            "Apple GPU M2"
        ));

        // iPhone 16 Pro Max — Max Graphics + 120 FPS
        list.add(new SpoofProfile(
            "apple_iphone_16_pro_max",
            "iPhone 16 Pro Max (Max Graphics + 120 FPS)",
            "Apple",
            "iPhone17,2", "Apple", "Apple",
            "iPhone17,2", "iPhone17,2", "iPhone17,2",
            "apple", "a18pro", "A18Pro",
            "a18pro", "a18pro",
            "apple/iPhone17,2/iPhone17,2:18.0/22A3354/0:user/release-keys",
            "iPhone17,2_18.0.1",
            "Apple GPU A18 Pro"
        ));

        // iPhone 15 Pro Max — Max Graphics
        list.add(new SpoofProfile(
            "apple_iphone_15_pro_max",
            "iPhone 15 Pro Max (Max Graphics + 120 FPS)",
            "Apple",
            "iPhone16,2", "Apple", "Apple",
            "iPhone16,2", "iPhone16,2", "iPhone16,2",
            "apple", "a17pro", "A17Pro",
            "a17pro", "a17pro",
            "apple/iPhone16,2/iPhone16,2:17.0/21A340/0:user/release-keys",
            "iPhone16,2_17.6.1",
            "Apple GPU A17 Pro"
        ));

        return list;
    }
}
