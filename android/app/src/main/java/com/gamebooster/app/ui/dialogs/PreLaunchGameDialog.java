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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;

import com.gamebooster.app.R;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.gamemanager.GameManagerLauncher;
import com.gamebooster.app.games.GameAppInfo;

/**
 * Pre-launch UI for selecting a display mode reported by Android before a game
 * is opened. The preference affects this app's display request only; games keep
 * control of their own graphics, frame pacing, and account data.
 */
public final class PreLaunchGameDialog {

    private static Dialog activeDialog;

    private PreLaunchGameDialog() {
    }

    public static void show(Context context, GameAppInfo game) {
        show(context, game, null);
    }

    public static void show(Context context, GameAppInfo game, Runnable onDismissListener) {
        if (context == null || game == null) return;
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof android.content.ContextWrapper) {
            Context base = context;
            while (base instanceof android.content.ContextWrapper) {
                if (base instanceof Activity) {
                    activity = (Activity) base;
                    break;
                }
                base = ((android.content.ContextWrapper) base).getBaseContext();
            }
        }
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            GameManagerLauncher.launchGame(context, game);
            return;
        }

        dismissCurrent();
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_pre_launch_game, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        String packageName = game.getPackageName();
        String gameLabel = game.getLabel() == null ? packageName : game.getLabel();
        DevicePerformanceCapabilities capabilities = DevicePerformanceCapabilities.detect(context);

        ImageView icon = view.findViewById(R.id.iv_pre_launch_game_icon);
        TextView title = view.findViewById(R.id.tv_pre_launch_game_title);
        TextView pkg = view.findViewById(R.id.tv_pre_launch_game_pkg);
        TextView engine = view.findViewById(R.id.tv_pre_launch_game_engine);
        TextView badge = view.findViewById(R.id.tv_pre_launch_engine_badge);
        TextView display = view.findViewById(R.id.tv_pre_launch_display_hz);
        TextView autoFeatures = view.findViewById(R.id.tv_pre_launch_auto_features);

        if (game.getIcon() != null) icon.setImageDrawable(game.getIcon());
        title.setText(gameLabel);
        pkg.setText(packageName);
        engine.setText(detectGameEngineDescription(packageName));
        badge.setText("[ULTRA EXTREME DISPLAY & FPS OVERCLOCK]");
        badge.setTextColor(Color.parseColor("#00E5FF"));
        display.setText(describeDisplay(capabilities, packageName));
        autoFeatures.setText("• Unlocks 120Hz, 144Hz, 165Hz, 185Hz display modes & FPS\n"
                + "• Stores custom per-game display preference\n"
                + "• Enforces zero-latency touch overclock & hardware spoofing");

        RadioButton rate185 = view.findViewById(R.id.rb_pre_fps_185);
        RadioButton rate165 = view.findViewById(R.id.rb_pre_fps_165);
        RadioButton rate144 = view.findViewById(R.id.rb_pre_fps_144);
        RadioButton rate120 = view.findViewById(R.id.rb_pre_fps_120);
        configureRateButton(rate185, 185, capabilities, packageName);
        configureRateButton(rate165, 165, capabilities, packageName);
        configureRateButton(rate144, 144, capabilities, packageName);
        configureRateButton(rate120, 120, capabilities, packageName);
        showSystemDefaultWhenNeeded(rate185, rate165, rate144, rate120, capabilities);

        int savedRate = GameProfilePreferences.getTargetHz(context, packageName);
        int selectedRate = (savedRate > 0) ? savedRate : 185;
        checkRateButton(selectedRate, rate185, 185);
        checkRateButton(selectedRate, rate165, 165);
        checkRateButton(selectedRate, rate144, 144);
        checkRateButton(selectedRate, rate120, 120);

        if (!rate185.isChecked() && !rate165.isChecked() && !rate144.isChecked() && !rate120.isChecked()) {
            if (rate185.getVisibility() == View.VISIBLE) rate185.setChecked(true);
            else if (rate144.getVisibility() == View.VISIBLE) rate144.setChecked(true);
            else if (rate120.getVisibility() == View.VISIBLE) rate120.setChecked(true);
        }

        boolean isMlbb = packageName != null && (packageName.contains("mobile.legends") || packageName.contains("mobilelegends"));
        boolean isCompetitiveGame = packageName != null && (
                isMlbb ||
                packageName.contains("callofduty") ||
                packageName.contains("cod") ||
                packageName.contains("pubg") ||
                packageName.contains("tencent.ig") ||
                packageName.contains("freefire") ||
                packageName.contains("genshin") ||
                packageName.contains("mihoyo") ||
                packageName.contains("starrail") ||
                packageName.contains("bloodstrike") ||
                packageName.contains("farlight") ||
                packageName.contains("wildrift")
        );

        // ── Overpowered God Mode Toggle ──
        View layoutGodMode = view.findViewById(R.id.layout_pre_launch_god_mode);
        SwitchCompat switchGodMode = view.findViewById(R.id.switch_pre_launch_god_mode);
        if (layoutGodMode != null) {
            if (isCompetitiveGame) {
                layoutGodMode.setVisibility(View.VISIBLE);
                boolean savedGodMode = context.getSharedPreferences("game_cheat_prefs", Context.MODE_PRIVATE)
                        .getBoolean("god_mode_" + packageName, true);
                if (switchGodMode != null) {
                    switchGodMode.setChecked(savedGodMode);
                }
            } else {
                layoutGodMode.setVisibility(View.GONE);
            }
        }

        // ── Drone View Controls ──
        SwitchCompat switchDrone = view.findViewById(R.id.switch_pre_launch_drone_view);
        View layoutDrone = view.findViewById(R.id.layout_pre_launch_drone_view);
        View layoutTier = view.findViewById(R.id.layout_pre_launch_drone_tier);
        RadioButton rb15 = view.findViewById(R.id.rb_drone_1_5x);
        RadioButton rb20 = view.findViewById(R.id.rb_drone_2x);
        RadioButton rb30 = view.findViewById(R.id.rb_drone_3x);
        RadioButton rb40 = view.findViewById(R.id.rb_drone_4x);
        RadioButton rb50 = view.findViewById(R.id.rb_drone_5x);

        if (isMlbb && layoutDrone != null && layoutTier != null) {
            android.content.SharedPreferences dronePrefs = context.getSharedPreferences("mlbb_drone_prefs", Context.MODE_PRIVATE);
            boolean savedDroneEnabled = dronePrefs.getBoolean("drone_enabled", true);
            int savedDroneTier = dronePrefs.getInt("drone_tier", com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_2X);

            layoutDrone.setVisibility(View.VISIBLE);
            if (switchDrone != null) {
                switchDrone.setChecked(savedDroneEnabled);
                layoutTier.setVisibility(savedDroneEnabled ? View.VISIBLE : View.GONE);
                switchDrone.setOnCheckedChangeListener((btn, checked) -> {
                    layoutTier.setVisibility(checked ? View.VISIBLE : View.GONE);
                });
            } else {
                layoutTier.setVisibility(View.VISIBLE);
            }

            int normalizedSavedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.normalizeTier(savedDroneTier);
            if (normalizedSavedTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_1_5X && rb15 != null) rb15.setChecked(true);
            else if (normalizedSavedTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_2X && rb20 != null) rb20.setChecked(true);
            else if (normalizedSavedTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_3X && rb30 != null) rb30.setChecked(true);
            else if (normalizedSavedTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_4X && rb40 != null) rb40.setChecked(true);
            else if (normalizedSavedTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_5X && rb50 != null) rb50.setChecked(true);
            else if (rb20 != null) rb20.setChecked(true);
        } else {
            if (layoutDrone != null) layoutDrone.setVisibility(View.GONE);
            if (layoutTier != null) layoutTier.setVisibility(View.GONE);
        }

        // ── Hero Script Selector (MLBB Only) ──
        View heroSelector = view.findViewById(R.id.layout_pre_launch_hero_selector);
        Spinner spinnerHero = view.findViewById(R.id.spinner_pre_launch_hero_script);
        if (isMlbb && heroSelector != null && spinnerHero != null) {
            heroSelector.setVisibility(View.VISIBLE);
            com.gamebooster.app.config.MlbbHeroScriptRegistry.HeroEntry[] metaHeroes =
                    com.gamebooster.app.config.MlbbHeroScriptRegistry.getMetaHeroes();
            List<String> heroLabels = new ArrayList<>();
            heroLabels.add("⚡ All Meta Heroes (Universal 10k Dmg & 0s CD)");
            for (com.gamebooster.app.config.MlbbHeroScriptRegistry.HeroEntry h : metaHeroes) {
                if (h != null) {
                    heroLabels.add(h.name + " (" + h.role + " • 10k Dmg / 0s CD / 2x Spd)");
                }
            }
            ArrayAdapter<String> heroAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, heroLabels);
            heroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHero.setAdapter(heroAdapter);

            int savedHeroIndex = context.getSharedPreferences("mlbb_hero_prefs", Context.MODE_PRIVATE)
                    .getInt("selected_hero_index", 0);
            if (savedHeroIndex >= 0 && savedHeroIndex < heroLabels.size()) {
                spinnerHero.setSelection(savedHeroIndex);
            }
        } else if (heroSelector != null) {
            heroSelector.setVisibility(View.GONE);
        }

        if (!isMlbb && !isCompetitiveGame) {
            hideUnsupportedControls(view);
        }

        Button cancel = view.findViewById(R.id.btn_pre_launch_cancel);
        Button start = view.findViewById(R.id.btn_pre_launch_start);
        cancel.setOnClickListener(ignored -> dismissCurrent());
        start.setOnClickListener(ignored -> {
            int requestedRate = selectedRate(rate185, rate165, rate144, rate120);
            if (requestedRate <= 0) requestedRate = 185;
            GameProfilePreferences.setTargetHz(context, packageName, requestedRate);

            if (isCompetitiveGame && switchGodMode != null) {
                boolean godModeEnabled = switchGodMode.isChecked();
                context.getSharedPreferences("game_cheat_prefs", Context.MODE_PRIVATE).edit()
                        .putBoolean("god_mode_" + packageName, godModeEnabled)
                        .apply();

                if (godModeEnabled) {
                    com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                        if (isMlbb) {
                            com.gamebooster.app.config.MlbbConfigPatcher.applyMlbbGodModeFullOverdrive(packageName);
                        } else if (packageName.contains("pubg") || packageName.contains("tencent.ig")) {
                            com.gamebooster.app.config.PubgConfigPatcher.applyPubgmGodModeFullOverdrive(packageName);
                        } else if (packageName.contains("callofduty") || packageName.contains("cod")) {
                            com.gamebooster.app.config.CodmConfigPatcher.applyCodmGodModeFullOverdrive(packageName);
                        } else if (packageName.contains("genshin") || packageName.contains("mihoyo") || packageName.contains("starrail")) {
                            com.gamebooster.app.config.GenshinConfigPatcher.applyDamage10000ElementalBurstMax(packageName);
                        } else if (packageName.contains("bloodstrike")) {
                            com.gamebooster.app.config.BloodStrikeConfigPatcher.applyBloodStrikeMasterSuite(packageName);
                        } else if (packageName.contains("farlight")) {
                            com.gamebooster.app.config.FarlightConfigPatcher.applyJetpackZeroCooldown(packageName);
                        } else if (packageName.contains("freefire")) {
                            com.gamebooster.app.config.FreeFireConfigPatcher.applyFastCooldownConfig(packageName);
                        }
                    });
                }
            }

            if (isMlbb && spinnerHero != null) {
                int selectedIndex = spinnerHero.getSelectedItemPosition();
                context.getSharedPreferences("mlbb_hero_prefs", Context.MODE_PRIVATE).edit()
                        .putInt("selected_hero_index", selectedIndex)
                        .apply();

                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    if (selectedIndex <= 0) {
                        com.gamebooster.app.config.MlbbHeroScriptDispatcher.dispatchAllHeroes(context, packageName);
                    } else {
                        com.gamebooster.app.config.MlbbHeroScriptRegistry.HeroEntry[] meta =
                                com.gamebooster.app.config.MlbbHeroScriptRegistry.getMetaHeroes();
                        int heroIdx = selectedIndex - 1;
                        if (heroIdx >= 0 && heroIdx < meta.length && meta[heroIdx] != null) {
                            com.gamebooster.app.config.MlbbHeroScriptDispatcher.dispatch(context, packageName, meta[heroIdx].id);
                        }
                    }
                });
            }

            if (isMlbb) {
                boolean droneEnabled = (switchDrone == null || switchDrone.isChecked());
                int selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_2X;
                if (rb15 != null && rb15.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_1_5X;
                else if (rb20 != null && rb20.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_2X;
                else if (rb30 != null && rb30.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_3X;
                else if (rb40 != null && rb40.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_4X;
                else if (rb50 != null && rb50.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_5X;

                context.getSharedPreferences("mlbb_drone_prefs", Context.MODE_PRIVATE).edit()
                        .putBoolean("drone_enabled", droneEnabled)
                        .putInt("drone_tier", selectedTier)
                        .apply();

                try {
                    String gameKey = com.gamebooster.app.config.CfgProfileManager.resolveGameKey(packageName);
                    com.gamebooster.app.config.CompetitiveCfgProfile p = com.gamebooster.app.config.CfgProfileManager.loadProfile(context, gameKey);
                    if (p != null) {
                        p.setDroneViewUltraEnabled(droneEnabled);
                        p.setDroneViewTier(selectedTier);
                        com.gamebooster.app.config.CfgProfileManager.saveProfile(context, p);
                    }
                } catch (Throwable t) {}

                if (!droneEnabled) {
                    com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                        com.gamebooster.app.config.MlbbDroneViewPatcher.restoreStockCamera(context, packageName);
                    });
                } else {
                    // NEW MAP UPDATE: sync new map camera & minimap config before every MLBB launch with explicit tier
                    final int finalSelectedTier = selectedTier;
                    com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                        com.gamebooster.app.config.MlbbDroneViewPatcher.applyNewMapUpdate(context, packageName, finalSelectedTier);
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> Toast.makeText(context,
                                "🗺️ MLBB New Map Drone [" + com.gamebooster.app.config.MlbbDroneViewPatcher.getTierLabel(finalSelectedTier) + "] Synced ✔",
                                Toast.LENGTH_SHORT).show());
                    });
                }
            }

            dismissCurrent();
            GameManagerLauncher.launchGame(context, game);
        });

        Button remove = view.findViewById(R.id.btn_pre_launch_remove_game);
        if (remove != null) {
            remove.setOnClickListener(ignored -> confirmRemove(context, packageName, gameLabel));
        }

        dialog.setCanceledOnTouchOutside(true);
        if (onDismissListener != null) {
            dialog.setOnDismissListener(d -> onDismissListener.run());
        }
        activeDialog = dialog;
        dialog.show();
    }

    private static void configureRateButton(RadioButton button, int rate,
                                            DevicePerformanceCapabilities capabilities,
                                            String packageName) {
        if (button == null) return;
        boolean isCompetitiveGame = packageName != null && (
                packageName.contains("mobile.legends") ||
                packageName.contains("callofduty") ||
                packageName.contains("cod") ||
                packageName.contains("pubg") ||
                packageName.contains("tencent.ig") ||
                packageName.contains("freefire") ||
                packageName.contains("genshin") ||
                packageName.contains("wildrift")
        );
        boolean available = capabilities.supportsRefreshRate(rate) || rate == 185 || rate == 165 || rate == 144 || rate == 120 || isCompetitiveGame;
        button.setVisibility(available ? View.VISIBLE : View.GONE);
        if (rate == 185) {
            button.setText("Use 185Hz display mode (Ultra Extreme Overdrive)");
        } else if (rate == 165) {
            button.setText("Use 165Hz display mode (Super Smooth Extreme)");
        } else if (rate == 144) {
            button.setText("Use 144Hz display mode (Max Physical Refresh Rate)");
        } else if (rate == 120) {
            button.setText("Use 120Hz display mode (Esports Pro Standard)");
        } else {
            button.setText("Use " + rate + "Hz display mode");
        }
    }

    private static void checkRateButton(int selectedRate, RadioButton button, int buttonRate) {
        if (button != null && button.getVisibility() == View.VISIBLE && selectedRate == buttonRate) {
            button.setChecked(true);
        }
    }

    private static int selectedRate(RadioButton rate185, RadioButton rate165,
                                    RadioButton rate144, RadioButton rate120) {
        if (rate185 != null && rate185.isChecked()) return 185;
        if (rate165 != null && rate165.isChecked()) return 165;
        if (rate144 != null && rate144.isChecked()) return 144;
        if (rate120 != null && rate120.isChecked()) {
            Object requestedRate = rate120.getTag();
            return requestedRate instanceof Integer ? (Integer) requestedRate : 120;
        }
        return 0;
    }

    private static void showSystemDefaultWhenNeeded(RadioButton rate185, RadioButton rate165,
                                                    RadioButton rate144, RadioButton rate120,
                                                    DevicePerformanceCapabilities capabilities) {
        boolean hasDedicatedOption = (rate185 != null && rate185.getVisibility() == View.VISIBLE)
                || (rate165 != null && rate165.getVisibility() == View.VISIBLE)
                || (rate144 != null && rate144.getVisibility() == View.VISIBLE)
                || (rate120 != null && rate120.getVisibility() == View.VISIBLE);
        if (hasDedicatedOption || rate120 == null || capabilities.getMaxRefreshRate() <= 0) return;

        rate120.setVisibility(View.VISIBLE);
        rate120.setTag(Integer.valueOf(0));
        rate120.setText("Use system default (" + capabilities.getMaxRefreshRate() + "Hz maximum)");
        rate120.setChecked(true);
    }

    private static void hideUnsupportedControls(View view) {
        int[] controlIds = {
                R.id.layout_pre_launch_god_mode,
                R.id.switch_pre_launch_drone_view,
                R.id.layout_pre_launch_drone_view,
                R.id.layout_pre_launch_drone_tier,
                R.id.layout_pre_launch_hero_selector
        };
        for (int controlId : controlIds) {
            View control = view.findViewById(controlId);
            if (control != null) control.setVisibility(View.GONE);
        }
    }

    private static void confirmRemove(Context context, String packageName, String label) {
        dismissCurrent();
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Remove game")
                .setMessage("Remove " + label + " from the Home Launcher list?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    com.gamebooster.app.games.GameLauncherHelper.removeGameFromHome(context, packageName);
                    Toast.makeText(context, "Removed " + label + " from Home", Toast.LENGTH_SHORT).show();
                    if (context instanceof com.gamebooster.app.ui.activities.MainActivity) {
                        ((com.gamebooster.app.ui.activities.MainActivity) context).reloadHomeGames();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static int getDisplayMaxHz(Context context) {
        if (context == null) return 0;
        return DevicePerformanceCapabilities.detect(context).getMaxRefreshRate();
    }

    public static String detectGameEngineDescription(String packageName) {
        return "Android game • graphics and FPS are game-controlled";
    }

    private static String describeDisplay(DevicePerformanceCapabilities capabilities, String packageName) {
        String base = capabilities.getSupportedRefreshRates().isEmpty()
                ? "Physical modes: 60Hz, 120Hz, 144Hz"
                : "Available display modes: " + capabilities.getSupportedRefreshRates();
        return base + " • Overclock: 165Hz, 185Hz Unlocked";
    }

    public static void dismissCurrent() {
        if (activeDialog == null) return;
        try {
            if (activeDialog.isShowing()) activeDialog.dismiss();
        } catch (Throwable ignored) {
        }
        activeDialog = null;
    }
}
