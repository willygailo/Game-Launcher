package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;
import com.gamebooster.app.ui.adapters.SpoofProfileAdapter;

import java.util.ArrayList;
import java.util.List;

public class SpoofBrandSelectorDialog {

    public interface OnProfileSelectedListener {
        void onProfileSelected(SpoofProfile profile);
    }

    private static Dialog activeDialog;

    public static void show(Context context, OnProfileSelectedListener listener) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_spoof_brand_selector, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.7f);
        }

        TextView tvSubtitle = view.findViewById(R.id.tv_dialog_brand_subtitle);
        TextView btnClose = view.findViewById(R.id.btn_dialog_close);
        RecyclerView rvProfiles = view.findViewById(R.id.rv_dialog_spoof_profiles);
        Button btnReset = view.findViewById(R.id.btn_dialog_reset_spoof);
        Button btnDone = view.findViewById(R.id.btn_dialog_done);

        // Brand Filter Buttons
        Button btnAll = view.findViewById(R.id.btn_chip_all);
        Button btnRog = view.findViewById(R.id.btn_chip_rog);
        Button btnSamsung = view.findViewById(R.id.btn_chip_samsung);
        Button btnNubia = view.findViewById(R.id.btn_chip_nubia);
        Button btnXiaomi = view.findViewById(R.id.btn_chip_xiaomi);
        Button btnRealme = view.findViewById(R.id.btn_chip_realme);
        Button btnOneplus = view.findViewById(R.id.btn_chip_oneplus);
        Button btnBlackshark = view.findViewById(R.id.btn_chip_blackshark);
        Button btnApple = view.findViewById(R.id.btn_chip_apple);
        Button btnVivo = view.findViewById(R.id.btn_chip_vivo);
        Button btnOppo = view.findViewById(R.id.btn_chip_oppo);
        Button btnLenovo = view.findViewById(R.id.btn_chip_lenovo);

        Button[] allButtons = new Button[]{
                btnAll, btnRog, btnSamsung, btnNubia, btnXiaomi, btnRealme,
                btnOneplus, btnBlackshark, btnApple, btnVivo, btnOppo, btnLenovo
        };

        rvProfiles.setLayoutManager(new LinearLayoutManager(context));
        rvProfiles.setHasFixedSize(true);

        List<SpoofProfile> allProfiles = new ArrayList<>(DeviceSpooferEngine.getAllProfiles().values());
        String currentActiveId = SpoofPreferences.getActiveProfileId(context);

        final SpoofProfileAdapter adapter = new SpoofProfileAdapter(context, allProfiles, selectedProfile -> {
            if (selectedProfile == null) return;

            // 1. Immediately persist and activate selected profile
            SpoofPreferences.setSpoofEnabled(context, true);
            SpoofPreferences.setActiveProfileId(context, selectedProfile.id);

            Toast.makeText(context, "⚡ Activating: " + selectedProfile.displayName, Toast.LENGTH_SHORT).show();

            // 2. Perform background injection
            AppExecutors.getInstance().executeCommand(() -> {
                boolean applied = DeviceSpooferEngine.applyProfile(context, selectedProfile, null);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (listener != null) {
                        listener.onProfileSelected(selectedProfile);
                    }
                    CyberActionDialog.show(
                            context,
                            "🎭 DEVICE IDENTITY ACTIVE",
                            true,
                            "Brand: " + (selectedProfile.brandLabel != null ? selectedProfile.brandLabel : selectedProfile.brand),
                            "Model: " + selectedProfile.displayName + " (" + selectedProfile.model + ")",
                            "GPU: " + selectedProfile.glRenderer + " (165Hz Max FPS Ready)"
                    );
                });
            });
        });

        if (currentActiveId != null) {
            adapter.setActiveProfileId(currentActiveId);
        }
        rvProfiles.setAdapter(adapter);

        // Helper to update chip highlights
        Runnable updateChipStyles = () -> {
            // reset all to dark
            for (Button b : allButtons) {
                if (b != null) {
                    b.setBackgroundResource(R.drawable.btn_cyber_dark);
                    b.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan));
                }
            }
        };

        // Bind Brand Click Listeners
        if (btnAll != null) {
            btnAll.setOnClickListener(v -> {
                updateChipStyles.run();
                btnAll.setBackgroundResource(R.drawable.btn_cyber_cyan);
                btnAll.setTextColor(0xFF000000);
                adapter.updateProfiles(new ArrayList<>(DeviceSpooferEngine.getAllProfiles().values()));
                if (rvProfiles != null) rvProfiles.scrollToPosition(0);
                if (tvSubtitle != null) tvSubtitle.setText("Showing all 11 gaming brands (" + SpoofProfileRegistry.getTotalCount() + " flagship models)");
            });
        }

        bindBrandFilter(btnRog, "ASUS ROG", "⚡ ASUS ROG Phone (185Hz / 165Hz Gaming Flagships)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnSamsung, "Samsung", "📱 SAMSUNG Galaxy (Ultra Gaming Lineup)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnNubia, "Nubia", "🎮 NUBIA RedMagic (165Hz eSports Lineup)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnXiaomi, "Xiaomi", "🚀 XIAOMI & POCO (Snapdragon 8 Series)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnRealme, "Realme", "🔥 REALME GT (Extreme Performance)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnOneplus, "OnePlus", "🏎️ ONEPLUS (Ultra Fast Oxygen/ColorOS)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnBlackshark, "Black Shark", "🦈 BLACK SHARK (Gaming Flagships)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnApple, "Apple", "🍎 APPLE (iPhone & iPad Pro 120Hz)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnVivo, "Vivo", "🎯 VIVO & iQOO (eSports Certified)", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnOppo, "Oppo", "💎 OPPO Find & Reno Series", allButtons, adapter, rvProfiles, tvSubtitle, context);
        bindBrandFilter(btnLenovo, "Lenovo Legion", "💻 LENOVO LEGION (Twin Fan Flagships)", allButtons, adapter, rvProfiles, tvSubtitle, context);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnDone != null) {
            btnDone.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                AppExecutors.getInstance().executeCommand(() -> {
                    DeviceSpooferEngine.resetSpoofing();
                    SpoofPreferences.clearActiveProfile(context);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        adapter.setActiveProfileId(null);
                        dialog.dismiss();
                        if (listener != null) {
                            listener.onProfileSelected(null);
                        }
                        Toast.makeText(context, "🔄 Device Identity Reset to Native Hardware", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        activeDialog = dialog;
        dialog.show();
    }

    private static void bindBrandFilter(Button btn, String brandLabel, String description,
                                        Button[] allButtons, SpoofProfileAdapter adapter,
                                        RecyclerView rvProfiles, TextView tvSubtitle, Context context) {
        if (btn == null) return;
        btn.setOnClickListener(v -> {
            for (Button b : allButtons) {
                if (b != null) {
                    b.setBackgroundResource(R.drawable.btn_cyber_dark);
                    b.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan));
                }
            }
            btn.setBackgroundResource(R.drawable.btn_cyber_cyan);
            btn.setTextColor(0xFF000000);

            List<SpoofProfile> brandProfiles = SpoofProfileRegistry.getByBrand(brandLabel);
            adapter.updateProfiles(brandProfiles);
            if (rvProfiles != null) {
                rvProfiles.scrollToPosition(0);
            }
            if (tvSubtitle != null) {
                tvSubtitle.setText(description + " (" + brandProfiles.size() + " models available)");
            }
        });
    }

    public static void dismissCurrent() {
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
            } catch (Throwable ignored) {}
            activeDialog = null;
        }
    }
}
