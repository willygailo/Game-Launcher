package com.gamespace.app.utils;

import android.os.Build;
import com.gamespace.app.core.DeviceSpecModel;

import java.util.HashMap;
import java.util.Map;

public class DeviceDetector {

    public enum ChipsetVendor {
        QUALCOMM,
        MEDIATEK,
        EXYNOS,
        UNISOC,
        TENSOR,
        KIRIN,
        GENERIC
    }

    public static DeviceSpecModel getDeviceSpecModel() {
        ChipsetVendor vendor = detectChipsetVendor();
        String chipsetName = getChipsetName();
        return new DeviceSpecModel(
                Build.MANUFACTURER,
                Build.MODEL,
                Build.BOARD,
                Build.HARDWARE,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                chipsetName,
                vendor
        );
    }

    public static Map<String, String> getDeviceSpecs() {
        Map<String, String> specs = new HashMap<>();
        specs.put("model", Build.MODEL);
        specs.put("manufacturer", Build.MANUFACTURER);
        specs.put("board", Build.BOARD);
        specs.put("hardware", Build.HARDWARE);
        specs.put("android_version", Build.VERSION.RELEASE);
        specs.put("sdk_int", String.valueOf(Build.VERSION.SDK_INT));

        ChipsetVendor vendor = detectChipsetVendor();
        specs.put("chipset_vendor", vendor.name());
        specs.put("chipset_name", getChipsetName());

        return specs;
    }

    private static String getChipsetName() {
        String hardware = Build.HARDWARE.toLowerCase();
        String board = Build.BOARD.toLowerCase();

        if (hardware.contains("qcom") || board.contains("sm") || board.contains("sdm")) {
            return "Qualcomm Snapdragon";
        } else if (hardware.contains("mt") || board.contains("mt")) {
            return "MediaTek Dimensity/Helio";
        } else if (hardware.contains("exynos") || board.contains("universal")) {
            return "Samsung Exynos";
        } else if (hardware.contains("ums") || board.contains("sp")) {
            return "Unisoc Tiger";
        } else if (hardware.contains("gs") || board.contains("slider")) {
            return "Google Tensor";
        } else if (hardware.contains("kirin") || board.contains("hi")) {
            return "HiSilicon Kirin";
        } else {
            return Build.HARDWARE;
        }
    }

    public static ChipsetVendor detectChipsetVendor() {
        String hardware = Build.HARDWARE.toLowerCase();
        String board = Build.BOARD.toLowerCase();

        if (hardware.contains("qcom") || board.contains("sm") || board.contains("sdm")) {
            return ChipsetVendor.QUALCOMM;
        } else if (hardware.contains("mt") || board.contains("mt")) {
            return ChipsetVendor.MEDIATEK;
        } else if (hardware.contains("exynos") || board.contains("universal")) {
            return ChipsetVendor.EXYNOS;
        } else if (hardware.contains("ums") || board.contains("sp")) {
            return ChipsetVendor.UNISOC;
        } else if (hardware.contains("gs") || board.contains("slider")) {
            return ChipsetVendor.TENSOR;
        } else if (hardware.contains("kirin") || board.contains("hi")) {
            return ChipsetVendor.KIRIN;
        } else {
            return ChipsetVendor.GENERIC;
        }
    }
}
