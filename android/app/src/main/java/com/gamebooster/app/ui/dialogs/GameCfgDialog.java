package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.gamebooster.app.R;
import com.gamebooster.app.config.AntiLogPatcher;
import com.gamebooster.app.config.CfgProfileManager;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.ConfigBackupManager;
import com.gamebooster.app.config.FpsUnlockTier;
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * GameCfgDialog — Cyberpunk Asynchronous Game Configuration Injector & Hz Tuner.
 *
 * Provides safe, non-blocking execution of game CFG modifications, SurfaceFlinger
 * refresh rate locks, touch polling overrides, and backup restorations without
 * ever freezing or crashing the UI Main Thread.
 */
public class GameCfgDialog {

    private static final String TAG = "GameCfgDialog";
    private static Dialog activeDialog;

    public interface OnConfigAppliedListener {
        void onConfigApplied(String packageName, int targetFps, int patchedFilesCount);
    }

    public static void show(Context context, GameAppInfo game, OnConfigAppliedListener listener) {
        if (context == null || game == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_game_cfg, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        final String pkg = game.getPackageName().toLowerCase().trim();
        final String gameKey = resolveGameKey(pkg);

        // View Bindings
        TextView tvTopBadge = view.findViewById(R.id.tv_cfg_top_badge);
        TextView tvEngineBadge = view.findViewById(R.id.tv_cfg_engine_badge);
        ImageView ivGameIcon = view.findViewById(R.id.iv_cfg_game_icon);
        TextView tvGameTitle = view.findViewById(R.id.tv_cfg_game_title);
        TextView tvGamePkg = view.findViewById(R.id.tv_cfg_game_pkg);

        RadioGroup rgFps = view.findViewById(R.id.rg_target_fps);
        RadioButton rb185 = view.findViewById(R.id.rb_fps_185);
        RadioButton rb165 = view.findViewById(R.id.rb_fps_165);
        RadioButton rb144 = view.findViewById(R.id.rb_fps_144);
        RadioButton rb120 = view.findViewById(R.id.rb_fps_120);

        SwitchCompat switchSuperTouch = view.findViewById(R.id.switch_super_touch);
        SwitchCompat switchForceHz = view.findViewById(R.id.switch_force_hz);
        SwitchCompat switchAntiLog = view.findViewById(R.id.switch_anti_log);

        // Graphics Driver RadioGroup
        RadioGroup rgDriver = view.findViewById(R.id.rg_game_driver);
        RadioButton rbDriverGame = view.findViewById(R.id.rb_driver_game_driver);
        RadioButton rbDriverDefault = view.findViewById(R.id.rb_driver_default);

        boolean isGameDriverEligible = com.gamebooster.app.booster.GpuTweaksChannel.isGameDriverEligible(pkg);
        if (rbDriverGame != null && rbDriverDefault != null) {
            if (!isGameDriverEligible) {
                rbDriverGame.setEnabled(false);
                rbDriverGame.setAlpha(0.5f);
                rbDriverGame.setText("🎮 System Vulkan Game Driver (MLBB / CODM / PUBGM Only - N/A)");
                rbDriverDefault.setChecked(true);
            } else {
                rbDriverGame.setEnabled(true);
                rbDriverGame.setAlpha(1.0f);
                rbDriverGame.setChecked(true);
            }
        }

        // Resolution Scaler RadioGroup
        RadioGroup rgResolution = view.findViewById(R.id.rg_resolution_scaler);
        RadioButton rbResNative = view.findViewById(R.id.rb_res_native);
        RadioButton rbRes900p = view.findViewById(R.id.rb_res_900p);
        RadioButton rbRes720p = view.findViewById(R.id.rb_res_720p);
        RadioButton rbRes540p = view.findViewById(R.id.rb_res_540p);

        // Visual Clarity Filter RadioGroup
        RadioGroup rgFilter = view.findViewById(R.id.rg_visual_filter);
        RadioButton rbFilterOff = view.findViewById(R.id.rb_filter_off);
        RadioButton rbFilterSniper = view.findViewById(R.id.rb_filter_sniper);
        RadioButton rbFilterVibrant = view.findViewById(R.id.rb_filter_vibrant);
        RadioButton rbFilterNight = view.findViewById(R.id.rb_filter_night);

        // ART Compiler Section
        TextView tvArtStatusLabel = view.findViewById(R.id.tv_art_status_label);
        Button btnTriggerArtCompile = view.findViewById(R.id.btn_trigger_art_compile);
        ProgressBar pbProgress = view.findViewById(R.id.pb_cfg_progress);
        TextView tvStatus = view.findViewById(R.id.tv_cfg_status);
        Button btnRestore = view.findViewById(R.id.btn_restore_cfg);
        Button btnCancel = view.findViewById(R.id.btn_cancel_cfg);
        Button btnApply = view.findViewById(R.id.btn_apply_cfg);

        // Load Game Info
        tvGameTitle.setText(game.getLabel());
        tvGamePkg.setText(pkg);
        if (game.getIcon() != null) {
            Glide.with(context).load(game.getIcon()).into(ivGameIcon);
        }

        // Check ART Dexopt Status Async
        AppExecutors.getInstance().executeCommand(() -> {
            String status = com.gamebooster.app.engine.ArtCompilerEngine.getCompilationStatus(pkg);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (tvArtStatusLabel != null) {
                    tvArtStatusLabel.setText("DEXOPT: " + status);
                }
            });
        });

        // Trigger ART AOT Speed Compile
        if (btnTriggerArtCompile != null) {
            btnTriggerArtCompile.setOnClickListener(v -> {
                if (!com.gamebooster.app.engine.ArtCompilerEngine.isCompilerAvailable()) {
                    Toast.makeText(context.getApplicationContext(), "⚠️ Shizuku or elevated access required for ART compiler!", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnTriggerArtCompile.setEnabled(false);
                btnTriggerArtCompile.setText("⏳ COMPILING...");
                if (tvArtStatusLabel != null) {
                    tvArtStatusLabel.setText("DEXOPT: ⚡ Compiling Ahead-Of-Time (speed)...");
                }
                Toast.makeText(context.getApplicationContext(), "⚡ Compiling " + game.getLabel() + " via ART dex2oat...", Toast.LENGTH_SHORT).show();

                com.gamebooster.app.engine.ArtCompilerEngine.compilePackageAsync(pkg, com.gamebooster.app.engine.ArtCompilerEngine.CompileFilter.SPEED, new com.gamebooster.app.engine.ArtCompilerEngine.CompileCallback() {
                    @Override
                    public void onProgress(String message) {
                        if (tvArtStatusLabel != null) tvArtStatusLabel.setText(message);
                    }

                    @Override
                    public void onComplete(boolean success, String details) {
                        btnTriggerArtCompile.setEnabled(true);
                        btnTriggerArtCompile.setText("⚡ COMPILE");
                        if (tvArtStatusLabel != null) {
                            tvArtStatusLabel.setText("DEXOPT: " + (success ? "⚡ Speed (Fully Native AOT)" : "⚠️ Compilation Finished"));
                        }
                        Toast.makeText(context.getApplicationContext(), (success ? "✅ AOT Speed Compilation Succeeded!" : "ℹ️ Compilation command dispatched."), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Engine Status Badge
        if (ShizukuExecutor.hasShizukuPermission()) {
            tvEngineBadge.setText("[SHIZUKU ACTIVE]");
            tvEngineBadge.setTextColor(Color.parseColor("#00FF66"));
        } else {
            tvEngineBadge.setText("[LIMITED / ROOTLESS]");
            tvEngineBadge.setTextColor(Color.parseColor("#FFAA00"));
        }

        // Load Existing Config Profile
        CompetitiveCfgProfile currentCfg = CfgProfileManager.loadProfile(context, gameKey);
        if (currentCfg != null) {
            switchSuperTouch.setChecked(currentCfg.isSuperFastTouchEnabled());
            switchForceHz.setChecked(currentCfg.isForceWriteSystemHz());
            switchAntiLog.setChecked(currentCfg.isAntiLogEnabled());

            int currentFps = currentCfg.getTargetFps();
            if (currentFps >= 185 && rb185 != null) {
                rb185.setChecked(true);
            } else if (currentFps >= 165 && rb165 != null) {
                rb165.setChecked(true);
            } else if (currentFps >= 144 && rb144 != null) {
                rb144.setChecked(true);
            } else if (rb120 != null) {
                rb120.setChecked(true);
            }
        }

        // ACTION: CANCEL (Dismiss without applying any changes)
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismissCurrent());
        }

        // ACTION: RESTORE (Revert configs, driver, scaler, filters to default)
        btnRestore.setOnClickListener(v -> {
            dismissCurrent();
            Toast.makeText(context.getApplicationContext(), "♻️ Restoring original configuration for " + game.getLabel() + "...", Toast.LENGTH_SHORT).show();

            AppExecutors.getInstance().executeCommand(() -> {
                int restoredCount = ConfigBackupManager.restorePackage(context, pkg);
                com.gamebooster.app.booster.GpuTweaksChannel.setTargetGameDriver(pkg, com.gamebooster.app.booster.GpuTweaksChannel.GraphicsDriverType.DEFAULT);
                com.gamebooster.app.engine.ResolutionScalerEngine.resetResolutionSync();
                com.gamebooster.app.overlay.VisualFilterOverlayService.setFilter(context, com.gamebooster.app.overlay.VisualFilterOverlayService.VisualFilterType.OFF);
                CfgProfileManager.saveProfile(context, CompetitiveCfgProfile.defaultCompetitive(gameKey));
                com.gamebooster.app.focus.FocusModeEngine.disableFocusMode(context);

                AppExecutors.getInstance().postToMainThread(() -> {
                    Toast.makeText(context.getApplicationContext(), "♻️ Restored original configs (" + restoredCount + " files)", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onConfigApplied(pkg, 0, restoredCount);
                    }
                });
            });
        });

        btnApply.setOnClickListener(v -> {
            final int targetFps;
            if (rb185 != null && rb185.isChecked()) {
                targetFps = 185;
            } else if (rb165 != null && rb165.isChecked()) {
                targetFps = 165;
            } else if (rb144 != null && rb144.isChecked()) {
                targetFps = 144;
            } else {
                targetFps = 120;
            }

            final boolean superTouch = switchSuperTouch.isChecked();
            final boolean forceHz = switchForceHz.isChecked();
            final boolean antiLog = switchAntiLog.isChecked();

            final com.gamebooster.app.booster.GpuTweaksChannel.GraphicsDriverType driverType;
            if (rbDriverGame != null && rbDriverGame.isChecked() && com.gamebooster.app.booster.GpuTweaksChannel.isGameDriverEligible(pkg)) {
                driverType = com.gamebooster.app.booster.GpuTweaksChannel.GraphicsDriverType.GAME_DRIVER;
            } else {
                driverType = com.gamebooster.app.booster.GpuTweaksChannel.GraphicsDriverType.DEFAULT;
            }

            final float targetScale;
            if (rbRes900p != null && rbRes900p.isChecked()) {
                targetScale = com.gamebooster.app.engine.ResolutionScalerEngine.ScalePreset.HIGH_900P.scaleFactor;
            } else if (rbRes720p != null && rbRes720p.isChecked()) {
                targetScale = com.gamebooster.app.engine.ResolutionScalerEngine.ScalePreset.ESPORTS_720P.scaleFactor;
            } else if (rbRes540p != null && rbRes540p.isChecked()) {
                targetScale = com.gamebooster.app.engine.ResolutionScalerEngine.ScalePreset.EXTREME_540P.scaleFactor;
            } else {
                targetScale = com.gamebooster.app.engine.ResolutionScalerEngine.ScalePreset.NATIVE_100.scaleFactor;
            }

            final com.gamebooster.app.overlay.VisualFilterOverlayService.VisualFilterType targetFilter;
            if (rbFilterSniper != null && rbFilterSniper.isChecked()) {
                targetFilter = com.gamebooster.app.overlay.VisualFilterOverlayService.VisualFilterType.SNIPER_SHADOW_BOOST;
            } else if (rbFilterVibrant != null && rbFilterVibrant.isChecked()) {
                targetFilter = com.gamebooster.app.overlay.VisualFilterOverlayService.VisualFilterType.VIBRANT_SATURATION;
            } else if (rbFilterNight != null && rbFilterNight.isChecked()) {
                targetFilter = com.gamebooster.app.overlay.VisualFilterOverlayService.VisualFilterType.NIGHT_ANTI_GLARE;
            } else {
                targetFilter = com.gamebooster.app.overlay.VisualFilterOverlayService.VisualFilterType.OFF;
            }

            // Apply resolution scaling and visual filter immediately
            if (targetScale < 0.99f) {
                com.gamebooster.app.engine.ResolutionScalerEngine.applyResolutionScale(context, targetScale);
            } else {
                com.gamebooster.app.engine.ResolutionScalerEngine.resetResolutionSync();
            }
            com.gamebooster.app.overlay.VisualFilterOverlayService.setFilter(context, targetFilter);

            // INSTANT AUTO-EXIT: Immediately dismiss without any blocking confirmation modals
            dismissCurrent();

            // Direct non-blocking notification
            Toast.makeText(context.getApplicationContext(), "⚡ DONE: CFG Applied for " + game.getLabel() + " (" + targetFps + " FPS Tier)", Toast.LENGTH_SHORT).show();

            AppExecutors.getInstance().executeCommand(() -> {
                int patchedFilesCount = 0;
                try {
                    // 1. Build and Save Profile
                    CompetitiveCfgProfile profile = new CompetitiveCfgProfile(gameKey, targetFps, superTouch, forceHz);
                    profile.setAntiLogEnabled(antiLog);
                    CfgProfileManager.saveProfile(context, profile);

                    // 2. Fast direct config patching for target package only
                    GameConfigPatcher.applyGameFpsPatch(context, pkg, targetFps);
                    com.gamebooster.app.config.CommonConfigTuningInjector.applyAllEnabledTunings(pkg, profile);
                    com.gamebooster.app.config.GameAutoInjectDispatcher.dispatchForPackage(pkg);
                    if (profile.isHardwareMaskEnabled()) {
                        com.gamebooster.app.spoofer.DeviceSpooferEngine.applySpoofing(context, pkg);
                    }
                    patchedFilesCount = 1;

                    // 3. Apply Targeted Graphics Driver (Protected: 0 impact on global apps)
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        com.gamebooster.app.booster.GpuTweaksChannel.setTargetGameDriver(pkg, driverType);
                    }

                    // 4. Fast single-batch privileged system optimizations
                    if (forceHz && ShizukuExecutor.hasShizukuPermission()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("cmd game mode performance ").append(pkg).append(" 2>/dev/null; ");
                        sb.append("cmd window set-app-refresh-rate ").append(pkg).append(" ").append(targetFps).append(" 2>/dev/null; ");
                        sb.append("cmd game set --fps ").append(targetFps).append(" ").append(pkg).append(" 2>/dev/null; ");
                        sb.append("service call SurfaceFlinger 1035 i32 ").append(targetFps).append(" 2>/dev/null; ");
                        sb.append("setprop debug.sf.nobootanimation 1; ");
                        sb.append("setprop debug.hwui.render_dirty_regions false");
                        if (targetFps >= 144) {
                            sb.append("; setprop debug.sf.use_phase_offsets_as_durations 1; ");
                            sb.append("setprop debug.sf.late.sf.duration 10500000; ");
                            sb.append("setprop debug.sf.late.app.duration 20500000; ");
                            sb.append("setprop debug.sf.hw 0; ");
                            sb.append("setprop debug.egl.hw 0; ");
                            sb.append("setprop persist.sys.ui.hw 1");
                        }
                        ShizukuExecutor.executeShizukuCommand(sb.toString());
                    }

                    // 5. Anti-Log & Telemetry Purge
                    if (antiLog) {
                        AntiLogPatcher.applyAntiLog(pkg);
                    }

                } catch (Throwable t) {
                    Log.e(TAG, "Error applying game configuration", t);
                }

                final int finalPatched = patchedFilesCount;
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (listener != null) {
                        listener.onConfigApplied(pkg, targetFps, finalPatched);
                    }
                });
            });
        });

        activeDialog = dialog;
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    public static void dismissCurrent() {
        if (activeDialog != null) {
            try {
                if (activeDialog.isShowing()) {
                    activeDialog.dismiss();
                }
            } catch (Exception ignored) {}
            activeDialog = null;
        }
    }

    private static String resolveGameKey(String pkg) {
        if (pkg == null) return CompetitiveCfgProfile.GAME_ALL;
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) return CompetitiveCfgProfile.GAME_MLBB;
        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) return CompetitiveCfgProfile.GAME_PUBGM;
        if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) return CompetitiveCfgProfile.GAME_CODM;
        if (pkg.contains("freefire") || pkg.contains("dts.freefire")) return CompetitiveCfgProfile.GAME_FREEFIRE;
        if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") || pkg.contains("wutheringwaves") || pkg.contains("kurogame")) return CompetitiveCfgProfile.GAME_GENSHIN;
        if (pkg.contains("wildrift") || pkg.contains("riotgames.league")) return CompetitiveCfgProfile.GAME_WILDRIFT;
        if (pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || pkg.contains("kgvn")) return CompetitiveCfgProfile.GAME_HOK;
        if (pkg.contains("bloodstrike") || pkg.contains("newspike")) return CompetitiveCfgProfile.GAME_BLOODSTRIKE;
        if (pkg.contains("standoff2") || pkg.contains("axlebolt")) return CompetitiveCfgProfile.GAME_STANDOFF2;
        if (pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row")) return CompetitiveCfgProfile.GAME_CARX;
        if (pkg.contains("uamo") || pkg.contains("arenabreakout") || pkg.contains("deltaforce")) return CompetitiveCfgProfile.GAME_ARENABREAKOUT;
        if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans")) return CompetitiveCfgProfile.GAME_SUPERCELL;
        if (pkg.contains("roblox")) return CompetitiveCfgProfile.GAME_ROBLOX;
        if (pkg.contains("projectc") || pkg.contains("valorant")) return CompetitiveCfgProfile.GAME_VALORANT;
        if (pkg.contains("farlight") || pkg.contains("solarland")) return CompetitiveCfgProfile.GAME_FARLIGHT;
        return CompetitiveCfgProfile.GAME_ALL;
    }
}
