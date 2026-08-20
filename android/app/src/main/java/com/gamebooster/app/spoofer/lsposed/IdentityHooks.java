package com.gamebooster.app.spoofer.lsposed;

import android.content.Context;
import android.provider.Settings;

import com.gamebooster.app.spoofer.SpoofProfile;

import java.net.NetworkInterface;
import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * IdentityHooks — spoofs device identifiers the target game may collect:
 * IMEI/MEID/IMSI/SIM serial/Carrier info via TelephonyManager, ANDROID_ID via
 * Settings.Secure, MAC Address via WifiInfo & NetworkInterface, Bluetooth address,
 * MediaDrm unique device ID, and WebView default User-Agent.
 *
 * All values are deterministic per profile so the game always sees the
 * same stable, legitimate flagship identity across sessions.
 */
public final class IdentityHooks {

    private IdentityHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        String imei = generateImei(profile);
        String meid = generateMeid(profile);
        String imsi = "310150" + String.format("%09d", profile.id.hashCode() & 0x7fffffff).substring(0, 9);
        String simSerial = "8901" + String.format("%016d", (profile.id.hashCode() & 0x7fffffff) % 10000000000000000L);
        String androidId = String.format("%016x", profile.id.hashCode() & 0x7fffffffL);
        String macAddress = generateMacAddress(profile);
        byte[] macBytes = parseMacBytes(macAddress);
        String adId = UUID.nameUUIDFromBytes(profile.id.getBytes()).toString();

        // 1. TelephonyManager
        Class<?> tmClass = XposedHelpers.findClass("android.telephony.TelephonyManager", lpparam.classLoader);
        if (tmClass != null) {
            hookReplace(tmClass, "getImei", imei);
            hookReplace(tmClass, "getMeid", meid);
            hookReplace(tmClass, "getDeviceId", imei);
            hookReplace(tmClass, "getSubscriberId", imsi);
            hookReplace(tmClass, "getSimSerialNumber", simSerial);
            hookReplace(tmClass, "getSimOperator", "310150");
            hookReplace(tmClass, "getSimOperatorName", "T-Mobile");
            hookReplace(tmClass, "getNetworkOperator", "310150");
            hookReplace(tmClass, "getNetworkOperatorName", "T-Mobile");
            hookReplace(tmClass, "getSimCountryIso", "us");
            hookReplace(tmClass, "getNetworkCountryIso", "us");
            hookReplace(tmClass, "getLine1Number", "");
            hookReplace(tmClass, "getNai", "");

            // int-slot overloads (getImei(int), getMeid(int), getDeviceId(int))
            hookReplaceIntSlot(tmClass, "getImei", imei);
            hookReplaceIntSlot(tmClass, "getMeid", meid);
            hookReplaceIntSlot(tmClass, "getDeviceId", imei);
            hookReplaceIntSlot(tmClass, "getSubscriberId", imsi);
            hookReplaceIntSlot(tmClass, "getSimSerialNumber", simSerial);
            hookReplaceIntSlot(tmClass, "getSimOperator", "310150");
            hookReplaceIntSlot(tmClass, "getSimOperatorName", "T-Mobile");
            hookReplaceIntSlot(tmClass, "getNetworkOperator", "310150");
            hookReplaceIntSlot(tmClass, "getNetworkOperatorName", "T-Mobile");
            hookReplaceIntSlot(tmClass, "getSimCountryIso", "us");
            hookReplaceIntSlot(tmClass, "getNetworkCountryIso", "us");
        }

        // 2. Settings.Secure & Settings.Global
        try {
            XposedHelpers.findAndHookMethod(Settings.Secure.class, "getString",
                    android.content.ContentResolver.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String name = (String) param.args[1];
                            if ("android_id".equals(name)) param.setResult(androidId);
                            else if ("bluetooth_address".equals(name)) param.setResult(macAddress);
                            else if ("advertising_id".equals(name)) param.setResult(adId);
                        }
                    });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(Settings.Global.class, "getString",
                    android.content.ContentResolver.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String name = (String) param.args[1];
                            if ("device_name".equals(name)) param.setResult(profile.model);
                        }
                    });
        } catch (Throwable ignored) {}

        // 3. WifiInfo & NetworkInterface
        Class<?> wifiInfoClass = XposedHelpers.findClass("android.net.wifi.WifiInfo", lpparam.classLoader);
        if (wifiInfoClass != null) {
            hookReplace(wifiInfoClass, "getMacAddress", macAddress);
            hookReplace(wifiInfoClass, "getBSSID", "02:00:00:00:00:00");
            hookReplace(wifiInfoClass, "getSSID", "\"GameSpace_HighSpeed\"");
        }

        try {
            XposedHelpers.findAndHookMethod(NetworkInterface.class, "getHardwareAddress", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.setResult(macBytes);
                }
            });
        } catch (Throwable ignored) {}

        // 4. BluetoothAdapter
        Class<?> btAdapterClass = XposedHelpers.findClass("android.bluetooth.BluetoothAdapter", lpparam.classLoader);
        if (btAdapterClass != null) {
            hookReplace(btAdapterClass, "getAddress", macAddress);
            hookReplace(btAdapterClass, "getName", profile.model);
        }

        // 5. MediaDrm Device Unique ID
        Class<?> mediaDrmClass = XposedHelpers.findClass("android.media.MediaDrm", lpparam.classLoader);
        if (mediaDrmClass != null) {
            try {
                XposedHelpers.findAndHookMethod(mediaDrmClass, "getPropertyByteArray", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String prop = (String) param.args[0];
                        if ("deviceUniqueId".equalsIgnoreCase(prop)) {
                            param.setResult(macBytes);
                        }
                    }
                });
            } catch (Throwable ignored) {}
        }

        // 6. WebView default User-Agent — reflect the spoofed model
        try {
            XposedHelpers.findAndHookMethod("android.webkit.WebSettings", lpparam.classLoader,
                    "getDefaultUserAgent", Context.class, XC_MethodReplacement.returnConstant(
                            "Mozilla/5.0 (Linux; Android " + profile.androidVersion + "; "
                            + profile.model + " Build/" + profile.displayId
                            + ") AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/121.0.0.0 Mobile Safari/537.36"));
        } catch (Throwable ignored) {}
    }

    private static void hookReplace(Class<?> clazz, String method, String value) {
        try {
            XposedHelpers.findAndHookMethod(clazz, method, XC_MethodReplacement.returnConstant(value));
        } catch (Throwable ignored) {}
    }

    private static void hookReplaceIntSlot(Class<?> clazz, String method, String value) {
        try {
            XposedHelpers.findAndHookMethod(clazz, method, int.class,
                    XC_MethodReplacement.returnConstant(value));
        } catch (Throwable ignored) {}
    }

    /** Deterministic Luhn-valid 15-digit IMEI derived from the profile id. */
    private static String generateImei(SpoofProfile profile) {
        String base = String.format("%014d", (profile.id.hashCode() & 0x7fffffff) % 100000000000000L);
        return base + luhnDigit(base);
    }

    /** Deterministic 14-digit MEID (not Luhn-checked, standard 14-digit format). */
    private static String generateMeid(SpoofProfile profile) {
        return String.format("%014d", (profile.id.hashCode() & 0x7fffffff) % 100000000000000L);
    }

    private static String generateMacAddress(SpoofProfile profile) {
        int h = profile.id.hashCode() & 0x7fffffff;
        return String.format("02:%02X:%02X:%02X:%02X:%02X",
                (h >> 20) & 0xFF, (h >> 16) & 0xFF, (h >> 12) & 0xFF, (h >> 8) & 0xFF, h & 0xFF);
    }

    private static byte[] parseMacBytes(String mac) {
        String[] parts = mac.split(":");
        byte[] bytes = new byte[6];
        for (int i = 0; i < Math.min(6, parts.length); i++) {
            try {
                bytes[i] = (byte) Integer.parseInt(parts[i], 16);
            } catch (Exception e) {
                bytes[i] = 0;
            }
        }
        return bytes;
    }

    private static int luhnDigit(String digits) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}