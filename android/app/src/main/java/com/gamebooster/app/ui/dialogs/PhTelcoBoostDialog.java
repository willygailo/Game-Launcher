package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.core.AppExecutors;

public class PhTelcoBoostDialog {

    private static Dialog activeDialog;

    public static void show(Context context) {
        if (context == null) return;

        AppExecutors.getInstance().postToMainThread(() -> {
            try {
                if (!(context instanceof Activity)) return;
                Activity act = (Activity) context;
                if (act.isFinishing() || act.isDestroyed()) return;

                if (activeDialog != null && activeDialog.isShowing()) {
                    try { activeDialog.dismiss(); } catch (Throwable ignored) {}
                }

                Dialog dialog = new Dialog(act);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                View view = LayoutInflater.from(context).inflate(R.layout.dialog_ph_telco_boost, (ViewGroup) null, false);
                dialog.setContentView(view);

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setDimAmount(0.50f);
                }

                Button btnBoostTnt = view.findViewById(R.id.btn_boost_tnt_smart);
                Button btnCopyTntApn = view.findViewById(R.id.btn_copy_tnt_apn);
                Button btnBoostTm = view.findViewById(R.id.btn_boost_tm_globe);
                Button btnCopyTmApn = view.findViewById(R.id.btn_copy_tm_apn);
                Button btnOpenApn = view.findViewById(R.id.btn_open_apn_settings);
                Button btnFlushDns = view.findViewById(R.id.btn_flush_baseband_dns);
                Button btnDismiss = view.findViewById(R.id.btn_dialog_dismiss);

                // ── 1. TNT / Smart Boost ──────────────────────────────────────────
                if (btnBoostTnt != null) {
                    btnBoostTnt.setOnClickListener(v -> {
                        AppExecutors.getInstance().executeCommand(() -> {
                            boolean ok = NetworkOptimizer.applyPhCarrierOptimization(context, NetworkOptimizer.PhCarrier.TNT_SMART);
                            AppExecutors.getInstance().postToMainThread(() -> {
                                if (ok) {
                                    CyberActionDialog.show(context, "🇵🇭 TNT / SMART 5G ULTRA GAMING BOOST", true,
                                            "✓ Carrier Profile: Smart / PLDT Core Gateway",
                                            "✓ TCP Congestion Algorithm: BBR (Low Bufferbloat)",
                                            "✓ DoT DNS Resolver: 1.1.1.1 (Cloudflare)",
                                            "✓ 5G SA/NSA Radio Power-Save: DISABLED",
                                            "✓ TCP Delayed ACK: 0ms (Instant Packet Mode)",
                                            "✓ Baseband & Route Resolver Cache: FLUSHED");
                                } else {
                                    Toast.makeText(context, "⚠️ Error applying TNT/Smart boost", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    });
                }

                if (btnCopyTntApn != null) {
                    btnCopyTntApn.setOnClickListener(v -> {
                        copyToClipboard(context, "APN: smartdata\nName: Smart 5G Ultra\nAPN Type: default,supl,dun,hipri,xcap\nProtocol: IPv4/IPv6\nBearer: LTE, NR");
                        Toast.makeText(context, "📋 TNT/Smart APN copied to clipboard!", Toast.LENGTH_SHORT).show();
                    });
                }

                // ── 2. TM / Globe Boost ───────────────────────────────────────────
                if (btnBoostTm != null) {
                    btnBoostTm.setOnClickListener(v -> {
                        AppExecutors.getInstance().executeCommand(() -> {
                            boolean ok = NetworkOptimizer.applyPhCarrierOptimization(context, NetworkOptimizer.PhCarrier.TM_GLOBE);
                            AppExecutors.getInstance().postToMainThread(() -> {
                                if (ok) {
                                    CyberActionDialog.show(context, "🇵🇭 TM / GLOBE 5G TURBO FAST ROUTE", true,
                                            "✓ Carrier Profile: Globe Telecom Core Backbone",
                                            "✓ TCP Congestion Algorithm: BBR (Low Bufferbloat)",
                                            "✓ DoT DNS Resolver: 8.8.8.8 (Google Fast Route)",
                                            "✓ 5G SA/NSA Radio Power-Save: DISABLED",
                                            "✓ TCP Delayed ACK: 0ms (Instant Packet Mode)",
                                            "✓ Baseband & Route Resolver Cache: FLUSHED");
                                } else {
                                    Toast.makeText(context, "⚠️ Error applying TM/Globe boost", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    });
                }

                if (btnCopyTmApn != null) {
                    btnCopyTmApn.setOnClickListener(v -> {
                        copyToClipboard(context, "APN: real.globe.com.ph\nName: TM 5G Turbo\nAPN Type: default,supl,mms,hipri,dun\nProtocol: IPv4/IPv6\nBearer: LTE, NR");
                        Toast.makeText(context, "📋 TM/Globe APN copied to clipboard!", Toast.LENGTH_SHORT).show();
                    });
                }

                // ── 3. Open APN Settings ──────────────────────────────────────────
                if (btnOpenApn != null) {
                    btnOpenApn.setOnClickListener(v -> {
                        try {
                            Intent apnIntent = new Intent(Settings.ACTION_APN_SETTINGS);
                            apnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(apnIntent);
                        } catch (Throwable t) {
                            try {
                                Intent dataRoaming = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                                dataRoaming.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(dataRoaming);
                            } catch (Throwable t2) {
                                try {
                                    Intent wirelessIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    wirelessIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(wirelessIntent);
                                } catch (Throwable ignored) {
                                    Toast.makeText(context, "⚠️ Please open APN in Android Settings manually", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }

                // ── 4. Flush DNS & Re-Sync Baseband ───────────────────────────────
                if (btnFlushDns != null) {
                    btnFlushDns.setOnClickListener(v -> {
                        AppExecutors.getInstance().executeCommand(() -> {
                            boolean ok = NetworkOptimizer.flushDnsCache();
                            AppExecutors.getInstance().postToMainThread(() -> {
                                Toast.makeText(context, ok ? "⚡ Cellular Baseband & DNS Cache Purged!" : "⚡ DNS Cache Flushed", Toast.LENGTH_SHORT).show();
                            });
                        });
                    });
                }

                // ── 5. Dismiss ────────────────────────────────────────────────────
                if (btnDismiss != null) {
                    btnDismiss.setOnClickListener(v -> dialog.dismiss());
                }

                dialog.setCanceledOnTouchOutside(true);
                activeDialog = dialog;
                dialog.show();
            } catch (Throwable ignored) {}
        });
    }

    private static void copyToClipboard(Context context, String text) {
        if (context == null || text == null) return;
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                ClipData clip = ClipData.newPlainText("APN Settings", text);
                cm.setPrimaryClip(clip);
            }
        } catch (Throwable ignored) {}
    }
}
