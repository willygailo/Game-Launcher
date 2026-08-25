package com.gamebooster.app.device;

import android.os.Build;
import com.gamebooster.app.device.DeviceSpecModel;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofProfile;

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
        APPLE,
        GENERIC
    }

    public static DeviceSpecModel getDeviceSpecModel() {
        SpoofProfile activeSpoof = DeviceSpooferEngine.getActiveProfile();
        if (activeSpoof != null) {
            ChipsetVendor vendor = inferVendorFromProfile(activeSpoof);
            return new DeviceSpecModel(
                    activeSpoof.manufacturer,
                    activeSpoof.model,
                    activeSpoof.board,
                    activeSpoof.hardware,
                    activeSpoof.androidVersion,
                    activeSpoof.sdkInt,
                    activeSpoof.chipname != null ? activeSpoof.chipname : activeSpoof.socModel,
                    vendor
            );
        }

        ChipsetVendor vendor = detectChipsetVendor();
        String chipsetName = getChipsetName();
        return new DeviceSpecModel(
                Build.MANUFACTURER != null ? Build.MANUFACTURER : "Generic",
                Build.MODEL != null ? Build.MODEL : "Android Device",
                Build.BOARD != null ? Build.BOARD : "universal",
                Build.HARDWARE != null ? Build.HARDWARE : "android",
                Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "16",
                Build.VERSION.SDK_INT > 0 ? Build.VERSION.SDK_INT : 36,
                chipsetName,
                vendor
        );
    }

    public static Map<String, String> getDeviceSpecs() {
        Map<String, String> specs = new HashMap<>();

        SpoofProfile activeSpoof = DeviceSpooferEngine.getActiveProfile();
        if (activeSpoof != null) {
            specs.put("model", activeSpoof.model);
            specs.put("manufacturer", activeSpoof.manufacturer);
            specs.put("brand", activeSpoof.brand);
            specs.put("board", activeSpoof.board);
            specs.put("hardware", activeSpoof.hardware);
            specs.put("android_version", activeSpoof.androidVersion);
            specs.put("sdk_int", String.valueOf(activeSpoof.sdkInt));
            specs.put("chipset_vendor", inferVendorFromProfile(activeSpoof).name());
            specs.put("chipset_name", activeSpoof.chipname != null ? activeSpoof.chipname : activeSpoof.socModel);
            specs.put("gpu_renderer", activeSpoof.glRenderer);
            specs.put("ram_total_mb", String.valueOf(activeSpoof.ramTotalMb));
            return specs;
        }

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

    private static ChipsetVendor inferVendorFromProfile(SpoofProfile profile) {
        if (profile == null) return ChipsetVendor.QUALCOMM;
        String mfg = profile.socManufacturer != null ? profile.socManufacturer.toLowerCase() : "";
        String soc = profile.socModel != null ? profile.socModel.toLowerCase() : "";
        if (mfg.contains("mediatek") || soc.contains("dimensity")) return ChipsetVendor.MEDIATEK;
        if (mfg.contains("samsung") || soc.contains("exynos")) return ChipsetVendor.EXYNOS;
        if (mfg.contains("google") || soc.contains("tensor")) return ChipsetVendor.TENSOR;
        if (mfg.contains("unisoc")) return ChipsetVendor.UNISOC;
        if (mfg.contains("hisilicon") || soc.contains("kirin")) return ChipsetVendor.KIRIN;
        return ChipsetVendor.QUALCOMM;
    }

    private static String socModelOrEmpty() {
        return Build.SOC_MODEL != null ? Build.SOC_MODEL.toLowerCase() : "";
    }

    private static String getChipsetName() {
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String soc = socModelOrEmpty();

        if (hardware.contains("qcom") || board.contains("sm8") || board.contains("sm7") || board.contains("sm6") || board.contains("sdm") || soc.contains("sm") || soc.contains("qcom") || soc.contains("qualcomm")) {
            if (board.contains("sm8750") || soc.contains("sm8750") || hardware.contains("sm8750")) return "Snapdragon 8 Elite (Gen 4)";
            if (board.contains("sm8650") || soc.contains("sm8650") || hardware.contains("sm8650")) return "Snapdragon 8 Gen 3";
            if (board.contains("sm8550") || soc.contains("sm8550") || hardware.contains("sm8550")) return "Snapdragon 8 Gen 2";
            if (board.contains("sm8450") || board.contains("sm8475") || soc.contains("sm8450") || soc.contains("sm8475")) return "Snapdragon 8/8+ Gen 1";
            if (board.contains("sm7675") || soc.contains("sm7675")) return "Snapdragon 7+ Gen 3";
            if (board.contains("sm8350") || soc.contains("sm8350")) return "Snapdragon 888/888+";
            return "Qualcomm Snapdragon (High-Perf Adreno)";
        } else if (hardware.contains("mt") || board.contains("mt") || soc.contains("dimensity") || soc.contains("mtk")) {
            if (board.contains("mt6991") || soc.contains("9400") || hardware.contains("mt6991")) return "MediaTek Dimensity 9400 (Immortalis-G925)";
            if (board.contains("mt6989") || soc.contains("9300") || hardware.contains("mt6989")) return "MediaTek Dimensity 9300+ (Immortalis-G720)";
            if (board.contains("mt6985") || soc.contains("9200") || hardware.contains("mt6985")) return "MediaTek Dimensity 9200+ (Immortalis-G715)";
            if (board.contains("mt6983") || soc.contains("9000") || hardware.contains("mt6983")) return "MediaTek Dimensity 9000+ (Mali-G710)";
            if (board.contains("mt6895") || soc.contains("8100") || soc.contains("8200")) return "MediaTek Dimensity 8100/8200";
            return "MediaTek Dimensity (Extreme Gaming)";
        } else if (hardware.contains("exynos") || board.contains("universal") || board.contains("s5e")) {
            if (board.contains("s5e9955") || soc.contains("2500")) return "Samsung Exynos 2500 (Xclipse 950)";
            if (board.contains("s5e9945") || soc.contains("2400")) return "Samsung Exynos 2400 (Xclipse 940)";
            if (board.contains("s5e9925") || soc.contains("2200")) return "Samsung Exynos 2200 (Xclipse 920)";
            return "Samsung Exynos (AMD RDNA)";
        } else if (hardware.contains("ums") || board.contains("sp") || board.contains("t820") || board.contains("t760")) {
            return "Unisoc T-Series (Tiger High-Perf)";
        } else if (hardware.contains("gs") || board.contains("slider") || board.contains("zuma") || board.contains("ripcurrent")) {
            if (board.contains("zuma") || soc.contains("zuma")) return "Google Tensor G3/G4 (Titan M2)";
            return "Google Tensor";
        } else if (hardware.contains("kirin") || board.contains("hi")) {
            if (board.contains("9000") || soc.contains("9000")) return "HiSilicon Kirin 9000/9010 (Maleoon 910)";
            return "HiSilicon Kirin";
        } else {
            return Build.HARDWARE != null ? Build.HARDWARE : "Android Performance System";
        }
    }

    public static ChipsetVendor detectChipsetVendor() {
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String soc = socModelOrEmpty();

        if (hardware.contains("qcom") || board.contains("sm") || board.contains("sdm") || soc.contains("sm")) {
            return ChipsetVendor.QUALCOMM;
        } else if (hardware.contains("mt") || board.contains("mt") || soc.contains("dimensity")) {
            return ChipsetVendor.MEDIATEK;
        } else if (hardware.contains("exynos") || board.contains("universal") || board.contains("s5e")) {
            return ChipsetVendor.EXYNOS;
        } else if (hardware.contains("ums") || board.contains("sp") || board.contains("t820")) {
            return ChipsetVendor.UNISOC;
        } else if (hardware.contains("gs") || board.contains("slider") || board.contains("zuma")) {
            return ChipsetVendor.TENSOR;
        } else if (hardware.contains("kirin") || board.contains("hi")) {
            return ChipsetVendor.KIRIN;
        } else {
            return ChipsetVendor.GENERIC;
        }
    }
}
