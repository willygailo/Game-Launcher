package com.gamebooster.app.spoofer.lsposed;

import android.content.Context;
import android.provider.Settings;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * IdentityHooks — spoofs device identifiers the target game may collect:
 * IMEI/MEID/IMSI/SIM serial via TelephonyManager, ANDROID_ID via
 * Settings.Secure, and the WebView default User-Agent. All values are
 * deterministic per profile (Luhn-valid IMEI) so the game always sees the
 * same stable identity across sessions.
 */
public final class IdentityHooks {

    private IdentityHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        String imei = generateImei(profile);
        String meid = generateMeid(profile);
        String imsi = "310150" + String.format("%09d", profile.id.hashCode() & 0x7fffffff).substring(0, 9);
        String simSerial = "8901" + String.format("%016d", (profile.id.hashCode() & 0x7fffffff) % 10000000000000000L);
        String androidId = String.format("%016x", profile.id.hashCode() & 0x7fffffffL);

        Class<?> tmClass = XposedHelpers.findClass("android.telephony.TelephonyManager", lpparam.classLoader);
        if (tmClass != null) {
            hookReplace(tmClass, "getImei", imei);
            hookReplace(tmClass, "getMeid", meid);
            hookReplace(tmClass, "getDeviceId", imei);
            hookReplace(tmClass, "getSubscriberId", imsi);
            hookReplace(tmClass, "getSimSerialNumber", simSerial);
            // int-slot overloads (getImei(int), getMeid(int), getDeviceId(int))
            hookReplaceIntSlot(tmClass, "getImei", imei);
            hookReplaceIntSlot(tmClass, "getMeid", meid);
            hookReplaceIntSlot(tmClass, "getDeviceId", imei);
        }

        // Settings.Secure.getString(resolver, "android_id")
        try {
            XposedHelpers.findAndHookMethod(Settings.Secure.class, "getString",
                    android.content.ContentResolver.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String name = (String) param.args[1];
                            if ("android_id".equals(name)) param.setResult(androidId);
                        }
                    });
        } catch (Throwable ignored) {}

        // WebView default User-Agent — reflect the spoofed model
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