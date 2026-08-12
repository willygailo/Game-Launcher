package com.gamebooster.app.feature.performance.device;

import android.os.Build;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static JSONObject getDeviceSpecsJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("model", Build.MODEL);
            json.put("manufacturer", Build.MANUFACTURER);
            json.put("brand", Build.BRAND);
            json.put("board", Build.BOARD);
            json.put("hardware", Build.HARDWARE);
            json.put("androidVersion", Build.VERSION.RELEASE);
            json.put("sdkInt", Build.VERSION.SDK_INT);

            ChipsetVendor vendor = detectChipsetVendor();
            json.put("chipsetVendor", vendor.name());
            json.put("chipsetName", getChipsetName());
        } catch (JSONException ignored) {}
        return json;
    }

    public static String getChipsetName() {
        String hardware = Build.HARDWARE.toLowerCase();
        String board = Build.BOARD.toLowerCase();

        if (hardware.contains("qcom") || board.contains("sm8750") || board.contains("sun") || board.contains("sm8650") || board.contains("sm8550") || board.contains("sm8450") || board.contains("sm")) {
            if (board.contains("sm8750") || board.contains("sun")) return "Snapdragon 8 Elite";
            if (board.contains("sm8650") || board.contains("kalama")) return "Snapdragon 8 Gen 3";
            if (board.contains("sm8550")) return "Snapdragon 8 Gen 2";
            return "Qualcomm Snapdragon";
        } else if (hardware.contains("mt") || board.contains("mt")) {
            if (board.contains("mt6989") || board.contains("mt6991")) return "MediaTek Dimensity 9300/9400";
            if (board.contains("mt6896")) return "MediaTek Dimensity 8200 Ultimate";
            return "MediaTek Dimensity/Helio";
        } else if (hardware.contains("exynos") || board.contains("universal") || board.contains("s5e")) {
            return "Samsung Exynos";
        } else if (hardware.contains("ums") || board.contains("sp")) {
            return "Unisoc Tiger";
        } else if (hardware.contains("gs") || board.contains("zuma") || board.contains("ripcurrent")) {
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

        if (hardware.contains("qcom") || board.contains("sm") || board.contains("sdm") || board.contains("sun")) {
            return ChipsetVendor.QUALCOMM;
        } else if (hardware.contains("mt") || board.contains("mt")) {
            return ChipsetVendor.MEDIATEK;
        } else if (hardware.contains("exynos") || board.contains("universal") || board.contains("s5e")) {
            return ChipsetVendor.EXYNOS;
        } else if (hardware.contains("ums") || board.contains("sp")) {
            return ChipsetVendor.UNISOC;
        } else if (hardware.contains("gs") || board.contains("zuma")) {
            return ChipsetVendor.TENSOR;
        } else if (hardware.contains("kirin") || board.contains("hi")) {
            return ChipsetVendor.KIRIN;
        } else {
            return ChipsetVendor.GENERIC;
        }
    }
}
