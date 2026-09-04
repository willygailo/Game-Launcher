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

    private static String socManufacturerOrEmpty() {
        return Build.SOC_MANUFACTURER != null ? Build.SOC_MANUFACTURER.toLowerCase() : "";
    }

    private static String getChipsetName() {
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String soc = socModelOrEmpty();
        String mfg = socManufacturerOrEmpty();

        // 1. Qualcomm Snapdragon
        if (mfg.contains("qualcomm") || mfg.contains("qcom") || hardware.contains("qcom") || hardware.contains("qualcomm")
                || hardware.contains("snapdragon") || board.contains("sm") || board.contains("sdm") || board.contains("sun")
                || board.contains("pineapple") || board.contains("kalama") || board.contains("taro") || board.contains("lahaina")
                || soc.contains("sm") || soc.contains("qcom") || soc.contains("qualcomm")) {
            if (board.contains("sm8750") || soc.contains("sm8750") || hardware.contains("sm8750") || board.contains("sun")) return "Qualcomm Snapdragon 8 Elite (Adreno 840)";
            if (board.contains("sm8650") || soc.contains("sm8650") || hardware.contains("sm8650") || board.contains("pineapple")) return "Qualcomm Snapdragon 8 Gen 3 (Adreno 750)";
            if (board.contains("sm8550") || soc.contains("sm8550") || hardware.contains("sm8550") || board.contains("kalama")) return "Qualcomm Snapdragon 8 Gen 2 (Adreno 740)";
            if (board.contains("sm8475") || soc.contains("sm8475")) return "Qualcomm Snapdragon 8+ Gen 1 (Adreno 730)";
            if (board.contains("sm8450") || soc.contains("sm8450") || hardware.contains("sm8450") || board.contains("taro")) return "Qualcomm Snapdragon 8 Gen 1 (Adreno 730)";
            if (board.contains("sm7675") || soc.contains("sm7675")) return "Qualcomm Snapdragon 7+ Gen 3 (Adreno 732)";
            if (board.contains("sm7475") || soc.contains("sm7475")) return "Qualcomm Snapdragon 7+ Gen 2 (Adreno 725)";
            if (board.contains("sm7435") || soc.contains("sm7435")) return "Qualcomm Snapdragon 7s Gen 2 (Adreno 710)";
            if (board.contains("sm6450") || soc.contains("sm6450")) return "Qualcomm Snapdragon 6 Gen 1 (Adreno 710)";
            if (board.contains("sm8350") || soc.contains("sm8350") || board.contains("lahaina")) return "Qualcomm Snapdragon 888/888+ (Adreno 660)";
            if (board.contains("sm8250") || soc.contains("sm8250") || board.contains("kona")) return "Qualcomm Snapdragon 865/870 (Adreno 650)";
            return "Qualcomm Snapdragon (High-Perf Adreno)";
        }

        // 2. MediaTek Dimensity
        if (mfg.contains("mediatek") || mfg.contains("mtk") || hardware.contains("mt") || hardware.contains("mediatek")
                || hardware.contains("dimensity") || board.contains("mt") || board.contains("k69") || board.contains("k68")
                || soc.contains("dimensity") || soc.contains("mtk") || soc.contains("mt6") || soc.contains("mt8")) {
            if (board.contains("mt6991") || soc.contains("9400") || hardware.contains("mt6991")) return "MediaTek Dimensity 9400 (Immortalis-G925)";
            if (board.contains("mt6989") || soc.contains("9300") || hardware.contains("mt6989")) return "MediaTek Dimensity 9300/9300+ (Immortalis-G720)";
            if (board.contains("mt6985") || soc.contains("9200") || hardware.contains("mt6985")) return "MediaTek Dimensity 9200/9200+ (Immortalis-G715)";
            if (board.contains("mt6983") || soc.contains("9000") || hardware.contains("mt6983")) return "MediaTek Dimensity 9000/9000+ (Mali-G710)";
            if (board.contains("mt6897") || soc.contains("8300")) return "MediaTek Dimensity 8300 (Mali-G615)";
            if (board.contains("mt6895") || soc.contains("8100") || soc.contains("8200")) return "MediaTek Dimensity 8100/8200 (Mali-G610)";
            if (board.contains("mt6886") || soc.contains("7200")) return "MediaTek Dimensity 7200 (Mali-G610)";
            if (board.contains("mt6877") || soc.contains("7050") || soc.contains("1080")) return "MediaTek Dimensity 7050 (Mali-G68)";
            if (soc.contains("6080") || soc.contains("810")) return "MediaTek Dimensity 6080 (Mali-G57)";
            return "MediaTek Dimensity (Extreme Gaming)";
        }

        // 3. Samsung Exynos
        if (mfg.contains("samsung") || hardware.contains("exynos") || hardware.contains("s5e")
                || board.contains("universal") || board.contains("s5e") || board.contains("erd")
                || soc.contains("exynos") || soc.contains("s5e")) {
            if (board.contains("s5e9955") || soc.contains("2500")) return "Samsung Exynos 2500 (Xclipse 950)";
            if (board.contains("s5e9945") || soc.contains("2400")) return "Samsung Exynos 2400 (Xclipse 940)";
            if (board.contains("s5e9925") || soc.contains("2200")) return "Samsung Exynos 2200 (Xclipse 920)";
            if (board.contains("s5e8845") || soc.contains("1480")) return "Samsung Exynos 1480 (Xclipse 530)";
            if (board.contains("s5e8835") || soc.contains("1380")) return "Samsung Exynos 1380 (Mali-G68)";
            if (board.contains("s5e8825") || soc.contains("1280")) return "Samsung Exynos 1280 (Mali-G68)";
            if (board.contains("s5e9840") || soc.contains("2100")) return "Samsung Exynos 2100 (Mali-G78)";
            if (board.contains("s5e9830") || soc.contains("990")) return "Samsung Exynos 990 (Mali-G77)";
            return "Samsung Exynos (AMD RDNA / Mali)";
        }

        // 4. Google Tensor
        if (mfg.contains("google") || hardware.contains("gs") || hardware.contains("tensor") || hardware.contains("zuma")
                || board.contains("slider") || board.contains("whitechapel") || board.contains("cloudripper") || board.contains("zuma")
                || board.contains("ripcurrent") || board.contains("comet") || board.contains("akita")
                || soc.contains("tensor") || soc.contains("gs101") || soc.contains("gs201") || soc.contains("zuma")) {
            if (board.contains("comet") || soc.contains("zuma_pro") || board.contains("zuma_pro")) return "Google Tensor G4 (Mali-G715 / Titan M2)";
            if (board.contains("zuma") || soc.contains("zuma") || board.contains("akita")) return "Google Tensor G3 (Mali-G715 / Titan M2)";
            if (board.contains("cloudripper") || board.contains("cheetah") || board.contains("panther") || soc.contains("gs201")) return "Google Tensor G2 (Mali-G710)";
            if (board.contains("whitechapel") || board.contains("oriole") || board.contains("raven") || soc.contains("gs101")) return "Google Tensor G1 (Mali-G78)";
            return "Google Tensor (Titan M2)";
        }

        // 5. HiSilicon Kirin
        if (mfg.contains("hisilicon") || mfg.contains("huawei") || hardware.contains("kirin") || hardware.contains("hi36")
                || hardware.contains("hi62") || board.contains("kirin") || board.contains("hi36") || board.contains("hi62")
                || soc.contains("kirin") || soc.contains("hi36") || soc.contains("maleoon")) {
            if (soc.contains("9010") || board.contains("9010")) return "HiSilicon Kirin 9010 (Maleoon 910)";
            if (soc.contains("9000s") || board.contains("9000s")) return "HiSilicon Kirin 9000S (Maleoon 910)";
            if (soc.contains("9000") || board.contains("9000") || board.contains("hi36a0") || soc.contains("hi36a0")) return "HiSilicon Kirin 9000 (Mali-G78 MP24)";
            if (soc.contains("990") || board.contains("990") || board.contains("hi3690") || soc.contains("hi3690")) return "HiSilicon Kirin 990 5G (Mali-G76 MP16)";
            if (soc.contains("980") || board.contains("980") || board.contains("hi3680") || soc.contains("hi3680")) return "HiSilicon Kirin 980 (Mali-G76 MP10)";
            return "HiSilicon Kirin (Maleoon / Mali)";
        }

        // 6. UNISOC (Spreadtrum / Tiger)
        if (mfg.contains("unisoc") || mfg.contains("spreadtrum") || mfg.contains("sprd") || hardware.contains("ums")
                || hardware.contains("sprd") || hardware.contains("unisoc") || hardware.contains("sharkl") || board.contains("sp")
                || board.contains("ums") || board.contains("t820") || board.contains("t760") || board.contains("t618")
                || board.contains("t616") || board.contains("t612") || board.contains("t610") || board.contains("t606")
                || soc.contains("unisoc") || soc.contains("ums") || soc.contains("t820") || soc.contains("t760")
                || soc.contains("t619") || soc.contains("t618") || soc.contains("t616") || soc.contains("t612") || soc.contains("t610") || soc.contains("t606")) {
            if (board.contains("t820") || soc.contains("t820") || hardware.contains("ums9620")) return "UNISOC Tiger T820 5G (Mali-G57 MC4)";
            if (board.contains("t760") || soc.contains("t760") || hardware.contains("ums9230")) return "UNISOC Tiger T760 5G (Mali-G57 MC4)";
            if (board.contains("t619") || soc.contains("t619")) return "UNISOC Tiger T619 (Mali-G57 MP1)";
            if (board.contains("t618") || soc.contains("t618") || hardware.contains("ums512")) return "UNISOC Tiger T618 (Mali-G52 MP2)";
            if (board.contains("t616") || soc.contains("t616")) return "UNISOC Tiger T616 (Mali-G57 MP1)";
            if (board.contains("t612") || soc.contains("t612")) return "UNISOC Tiger T612 (Mali-G57)";
            if (board.contains("t610") || soc.contains("t610")) return "UNISOC Tiger T610 (Mali-G52 MP2)";
            if (board.contains("t606") || soc.contains("t606") || hardware.contains("ums9230")) return "UNISOC Tiger T606 (Mali-G57 MP1)";
            return "UNISOC Tiger T-Series (Mali High-Perf)";
        }

        return Build.HARDWARE != null ? Build.HARDWARE : "Android Performance System";
    }

    public static ChipsetVendor detectChipsetVendor() {
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String soc = socModelOrEmpty();
        String mfg = socManufacturerOrEmpty();

        if (mfg.contains("qualcomm") || mfg.contains("qcom")
                || hardware.contains("qcom") || hardware.contains("qualcomm") || hardware.contains("snapdragon")
                || board.contains("sm") || board.contains("sdm") || board.contains("sun") || board.contains("pineapple")
                || board.contains("kalama") || board.contains("taro") || board.contains("lahaina")
                || soc.contains("sm") || soc.contains("qcom") || soc.contains("qualcomm")) {
            return ChipsetVendor.QUALCOMM;
        } else if (mfg.contains("mediatek") || mfg.contains("mtk")
                || hardware.contains("mt") || hardware.contains("mediatek") || hardware.contains("dimensity")
                || board.contains("mt") || board.contains("k69") || board.contains("k68")
                || soc.contains("dimensity") || soc.contains("mtk") || soc.contains("mt6") || soc.contains("mt8")) {
            return ChipsetVendor.MEDIATEK;
        } else if (mfg.contains("samsung")
                || hardware.contains("exynos") || hardware.contains("s5e")
                || board.contains("universal") || board.contains("s5e") || board.contains("erd")
                || soc.contains("exynos") || soc.contains("s5e")) {
            return ChipsetVendor.EXYNOS;
        } else if (mfg.contains("google")
                || hardware.contains("gs") || hardware.contains("tensor") || hardware.contains("zuma")
                || board.contains("slider") || board.contains("whitechapel") || board.contains("cloudripper")
                || board.contains("zuma") || board.contains("ripcurrent") || board.contains("comet") || board.contains("akita")
                || soc.contains("tensor") || soc.contains("gs101") || soc.contains("gs201") || soc.contains("zuma")) {
            return ChipsetVendor.TENSOR;
        } else if (mfg.contains("hisilicon") || mfg.contains("huawei")
                || hardware.contains("kirin") || hardware.contains("hi36") || hardware.contains("hi62")
                || board.contains("kirin") || board.contains("hi36") || board.contains("hi62")
                || soc.contains("kirin") || soc.contains("hi36") || soc.contains("maleoon")) {
            return ChipsetVendor.KIRIN;
        } else if (mfg.contains("unisoc") || mfg.contains("spreadtrum") || mfg.contains("sprd")
                || hardware.contains("ums") || hardware.contains("sprd") || hardware.contains("unisoc") || hardware.contains("sharkl")
                || board.contains("sp") || board.contains("ums") || board.contains("t820") || board.contains("t760")
                || board.contains("t618") || board.contains("t616") || board.contains("t612") || board.contains("t610") || board.contains("t606")
                || soc.contains("unisoc") || soc.contains("ums") || soc.contains("t820") || soc.contains("t760")
                || soc.contains("t619") || soc.contains("t618") || soc.contains("t616") || soc.contains("t612") || soc.contains("t610") || soc.contains("t606")) {
            return ChipsetVendor.UNISOC;
        } else {
            return ChipsetVendor.GENERIC;
        }
    }
}
