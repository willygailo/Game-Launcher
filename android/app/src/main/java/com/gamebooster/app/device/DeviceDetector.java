package com.gamebooster.app.device;

import android.os.Build;
import com.gamebooster.app.device.DeviceSpecModel;

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

    public enum OemBrand {
        XIAOMI,
        SAMSUNG,
        OPPO,
        ONEPLUS,
        REALME,
        TRANSSION, // Infinix, Tecno, itel
        ASUS,
        REDMAGIC,
        VIVO_IQOO,
        MOTOROLA,
        GOOGLE,
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
        specs.put("brand", Build.BRAND);
        specs.put("board", Build.BOARD);
        specs.put("hardware", Build.HARDWARE);
        specs.put("android_version", Build.VERSION.RELEASE);
        specs.put("sdk_int", String.valueOf(Build.VERSION.SDK_INT));

        ChipsetVendor vendor = detectChipsetVendor();
        specs.put("chipset_vendor", vendor.name());
        specs.put("chipset_name", getChipsetName());
        specs.put("oem_brand", detectOemBrand().name());
        specs.put("soc_generation", getDetailedSocDescription());

        return specs;
    }

    public static String getChipsetName() {
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String soc = getSocModelFromBuild();

        if (hardware.contains("qcom") || board.contains("sm") || board.contains("sdm") || board.contains("sun") || board.contains("pineapple") || board.contains("kalama") || board.contains("taro") || board.contains("lahaina")) {
            return "Qualcomm Snapdragon";
        } else if (hardware.contains("mt") || board.contains("mt") || board.contains("k69") || board.contains("k68") || board.contains("k85")) {
            return "MediaTek Dimensity / Helio";
        } else if (hardware.contains("exynos") || board.contains("universal") || board.contains("s5e")) {
            return "Samsung Exynos";
        } else if (hardware.contains("ums") || board.contains("sp") || board.contains("uis") || hardware.contains("unisoc") || hardware.contains("spreadtrum")) {
            return "Unisoc Tiger";
        } else if (hardware.contains("gs") || board.contains("slider") || board.contains("cloudripper") || board.contains("zuma") || board.contains("ripcurrent")) {
            return "Google Tensor";
        } else if (hardware.contains("kirin") || board.contains("hi") || board.contains("kirin")) {
            return "HiSilicon Kirin";
        } else {
            return !soc.isEmpty() ? soc : (!Build.HARDWARE.isEmpty() ? Build.HARDWARE : "Universal Core");
        }
    }

    public static String getDetailedSocDescription() {
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String hard = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String soc = getSocModelFromBuild().toLowerCase();

        // Qualcomm Flagships & Midranges
        if (board.contains("sun") || soc.contains("sm8750")) return "Snapdragon 8 Elite (Oryon Gen 2)";
        if (board.contains("pineapple") || soc.contains("sm8650")) return "Snapdragon 8 Gen 3 (Adreno 750)";
        if (board.contains("kalama") || soc.contains("sm8550")) return "Snapdragon 8 Gen 2 (Adreno 740)";
        if (board.contains("taro") || soc.contains("sm8450") || soc.contains("sm8475")) return "Snapdragon 8 Gen 1 / 8+ Gen 1";
        if (board.contains("lahaina") || soc.contains("sm8350")) return "Snapdragon 888 / 888+";
        if (board.contains("kona") || soc.contains("sm8250")) return "Snapdragon 865 / 870";
        if (soc.contains("sm7475") || soc.contains("sm7550") || soc.contains("sm7675")) return "Snapdragon 7+ Gen 2 / 7 Gen 3";

        // MediaTek Flagships & Midranges
        if (board.contains("k85") || board.contains("mt6991") || soc.contains("mt6991")) return "Dimensity 9400 (Immortalis-G925)";
        if (board.contains("mt6989") || soc.contains("mt6989")) return "Dimensity 9300 / 9300+ (Immortalis-G720)";
        if (board.contains("mt6985") || soc.contains("mt6985")) return "Dimensity 9200 / 9200+";
        if (board.contains("mt6895") || soc.contains("mt6895") || soc.contains("mt6897")) return "Dimensity 8100 / 8200 / 8300";
        if (board.contains("mt6877") || soc.contains("mt6877")) return "Dimensity 7050 / 7200 / 1080";
        if (hard.contains("mt6789") || hard.contains("g99")) return "MediaTek Helio G99 (Mali-G57 MC2)";
        if (hard.contains("mt6769") || hard.contains("g88") || hard.contains("g85")) return "MediaTek Helio G85 / G88";

        // Samsung Exynos
        if (board.contains("s5e9945") || soc.contains("s5e9945")) return "Exynos 2400 (Xclipse 940 AMD RDNA3)";
        if (board.contains("s5e9925") || soc.contains("s5e9925")) return "Exynos 2200 (Xclipse 920 AMD RDNA2)";
        if (board.contains("s5e8845")) return "Exynos 1480 (Xclipse 530)";
        if (board.contains("s5e8835")) return "Exynos 1380";

        // Google Tensor
        if (board.contains("zuma") || board.contains("ripcurrent")) return "Google Tensor G3 / G4";
        if (board.contains("cloudripper")) return "Google Tensor G2";
        if (board.contains("slider")) return "Google Tensor G1";

        // Unisoc
        if (hard.contains("ums9230") || hard.contains("t606")) return "Unisoc Tiger T606 (Mali-G57)";
        if (hard.contains("ums9620") || hard.contains("t820")) return "Unisoc Tiger T820 (5G Gaming)";
        if (hard.contains("t616") || hard.contains("t618")) return "Unisoc Tiger T616 / T618";

        return getChipsetName();
    }

    public static ChipsetVendor detectChipsetVendor() {
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String soc = getSocModelFromBuild().toLowerCase();

        if (hardware.contains("qcom") || board.contains("sm") || board.contains("sdm") || board.contains("sun") || board.contains("pineapple") || board.contains("kalama") || board.contains("taro") || board.contains("lahaina") || soc.contains("sm8") || soc.contains("sm7") || soc.contains("sm6")) {
            return ChipsetVendor.QUALCOMM;
        } else if (hardware.contains("mt") || board.contains("mt") || board.contains("k69") || board.contains("k68") || board.contains("k85") || soc.contains("mt6")) {
            return ChipsetVendor.MEDIATEK;
        } else if (hardware.contains("exynos") || board.contains("universal") || board.contains("s5e") || soc.contains("exynos")) {
            return ChipsetVendor.EXYNOS;
        } else if (hardware.contains("ums") || board.contains("sp") || board.contains("uis") || hardware.contains("unisoc") || hardware.contains("spreadtrum")) {
            return ChipsetVendor.UNISOC;
        } else if (hardware.contains("gs") || board.contains("slider") || board.contains("cloudripper") || board.contains("zuma") || board.contains("ripcurrent") || soc.contains("tensor")) {
            return ChipsetVendor.TENSOR;
        } else if (hardware.contains("kirin") || board.contains("hi") || soc.contains("kirin")) {
            return ChipsetVendor.KIRIN;
        } else {
            return ChipsetVendor.GENERIC;
        }
    }

    public static OemBrand detectOemBrand() {
        String mfr = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        String combined = mfr + " " + brand;

        if (combined.contains("xiaomi") || combined.contains("redmi") || combined.contains("poco")) {
            return OemBrand.XIAOMI;
        } else if (combined.contains("samsung")) {
            return OemBrand.SAMSUNG;
        } else if (combined.contains("oneplus")) {
            return OemBrand.ONEPLUS;
        } else if (combined.contains("oppo")) {
            return OemBrand.OPPO;
        } else if (combined.contains("realme")) {
            return OemBrand.REALME;
        } else if (combined.contains("infinix") || combined.contains("tecno") || combined.contains("itel") || combined.contains("transsion")) {
            return OemBrand.TRANSSION;
        } else if (combined.contains("asus") || combined.contains("rog")) {
            return OemBrand.ASUS;
        } else if (combined.contains("nubia") || combined.contains("redmagic") || combined.contains("zte")) {
            return OemBrand.REDMAGIC;
        } else if (combined.contains("vivo") || combined.contains("iqoo")) {
            return OemBrand.VIVO_IQOO;
        } else if (combined.contains("motorola") || combined.contains("moto") || combined.contains("lenovo")) {
            return OemBrand.MOTOROLA;
        } else if (combined.contains("google") || combined.contains("pixel")) {
            return OemBrand.GOOGLE;
        } else {
            return OemBrand.GENERIC;
        }
    }

    private static String getSocModelFromBuild() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                return Build.SOC_MODEL != null ? Build.SOC_MODEL : "";
            } catch (Throwable ignored) {}
        }
        return "";
    }
}

