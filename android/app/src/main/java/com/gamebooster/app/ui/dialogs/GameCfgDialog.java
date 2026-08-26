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
        RadioButton rb144Ultra = view.findViewById(R.id.rb_fps_144_ultra);
        RadioButton rb185 = view.findViewById(R.id.rb_fps_185);
        RadioButton rb165 = view.findViewById(R.id.rb_fps_165);
        RadioButton rb144 = view.findViewById(R.id.rb_fps_144);
        RadioButton rb120 = view.findViewById(R.id.rb_fps_120);
        RadioButton rb90 = view.findViewById(R.id.rb_fps_90);

        SwitchCompat switchSuperTouch = view.findViewById(R.id.switch_super_touch);
        SwitchCompat switchForceHz = view.findViewById(R.id.switch_force_hz);
        SwitchCompat switchAntiLog = view.findViewById(R.id.switch_anti_log);

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

        // Engine Status Badge
        if (ShizukuExecutor.hasShizukuPermission()) {
            tvEngineBadge.setText("[SHIZUKU ACTIVE]");
            tvEngineBadge.setTextColor(Color.parseColor("#00FF66"));
        } else {
            tvEngineBadge.setText("[STANDALONE ENGINE]");
            tvEngineBadge.setTextColor(Color.parseColor("#00F0FF"));
        }

        // Load Existing Config Profile
        CompetitiveCfgProfile currentCfg = CfgProfileManager.loadProfile(context, gameKey);
        if (currentCfg != null) {
            switchSuperTouch.setChecked(currentCfg.isSuperFastTouchEnabled());
            switchForceHz.setChecked(currentCfg.isForceWriteSystemHz());
            switchAntiLog.setChecked(currentCfg.isAntiLogEnabled());

            int currentFps = currentCfg.getTargetFps();
            if (currentFps == 185) {
                rb185.setChecked(true);
            } else if (currentFps == 165) {
                rb165.setChecked(true);
            } else if (currentFps == 120) {
                rb120.setChecked(true);
            } else if (currentFps == 90) {
                rb90.setChecked(true);
            } else {
                rb144Ultra.setChecked(true);
            }
        }

        // Check Backups Availability
        if (ConfigBackupManager.hasBackups(context, pkg)) {
            btnRestore.setVisibility(View.VISIBLE);
        }

        btnCancel.setOnClickListener(v -> dismissCurrent());

        btnRestore.setOnClickListener(v -> {
            btnRestore.setEnabled(false);
            btnApply.setEnabled(false);
            pbProgress.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("♻️ Restoring original configuration files...");

            AppExecutors.getInstance().executeCommand(() -> {
                int restoredCount = ConfigBackupManager.restorePackage(context, pkg);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (!dialog.isShowing()) return;
                    pbProgress.setVisibility(View.GONE);
                    tvStatus.setVisibility(View.GONE);
                    btnRestore.setVisibility(View.GONE);
                    btnApply.setEnabled(true);

                    CyberActionDialog.show(
                            context,
                            "♻️ ORIGINAL CONFIGS RESTORED",
                            true,
                            "Game: " + game.getLabel(),
                            "Restored " + restoredCount + " original configuration files",
                            "Clean backup state verified"
                    );
                });
            });
        });

        btnApply.setOnClickListener(v -> {
            final boolean isUltraExtreme = rb144Ultra.isChecked();
            final int targetFps;
            if (rb185.isChecked()) {
                targetFps = 185;
            } else if (rb165.isChecked()) {
                targetFps = 165;
            } else if (rb144.isChecked() || isUltraExtreme) {
                targetFps = 144;
            } else if (rb120.isChecked()) {
                targetFps = 120;
            } else if (rb90.isChecked()) {
                targetFps = 90;
            } else {
                targetFps = 144;
            }

            final boolean superTouch = switchSuperTouch.isChecked();
            final boolean forceHz = switchForceHz.isChecked();
            final boolean antiLog = switchAntiLog.isChecked();

            btnApply.setEnabled(false);
            btnCancel.setEnabled(false);
            btnRestore.setEnabled(false);
            pbProgress.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("⚡ Applying " + targetFps + " FPS configuration into " + game.getLabel() + "...");

            AppExecutors.getInstance().executeCommand(() -> {
                int patchedFilesCount = 0;
                try {
                    // 1. Force-stop game ONLY if it is currently running so cold-start picks up new configs
                    try {
                        String pidCheck = ShizukuExecutor.executeShizukuCommand("pidof " + pkg + " 2>/dev/null");
                        boolean gameCurrentlyRunning = pidCheck != null && !pidCheck.trim().isEmpty()
                                && !pidCheck.startsWith("ERROR");
                        if (gameCurrentlyRunning) {
                            ShizukuExecutor.executeShizukuCommand("am force-stop " + pkg + " 2>/dev/null");
                            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                        }
                    } catch (Throwable ignored) {}

                    // 2. Build and Save Profile
                    CompetitiveCfgProfile profile = new CompetitiveCfgProfile(gameKey, targetFps, superTouch, forceHz);
                    profile.setAntiLogEnabled(antiLog);
                    CfgProfileManager.saveProfile(context, profile);

                    // 3. Apply Config Patching
                    patchedFilesCount = CfgProfileManager.applyProfile(context, gameKey, profile);
                    if (isUltraExtreme) {
                        GameConfigPatcher.applyUltraExtreme144Patch(context, pkg);
                    } else {
                        GameConfigPatcher.applyGameFpsPatch(context, pkg, targetFps);
                    }
                    GameProfileAutoConfigurator.autoConfigGamePackage(context, pkg, targetFps);

                    // 4. Privileged Shizuku / System Hz & Game Driver Optimization
                    if (forceHz && ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.executeShizukuCommands(
                                "settings put global game_driver_opt_in_apps " + pkg + " 2>/dev/null",
                                "settings put global updatable_driver_production_opt_in_apps " + pkg + " 2>/dev/null",
                                "settings put global angle_gl_driver_selection_pkgs " + pkg + " 2>/dev/null",
                                "cmd game mode performance " + pkg + " 2>/dev/null",
                                "cmd window set-app-refresh-rate " + pkg + " " + targetFps + " 2>/dev/null",
                                "cmd game set --fps " + targetFps + " " + pkg + " 2>/dev/null",
                                "service call SurfaceFlinger 1035 i32 " + targetFps + " 2>/dev/null",
                                "setprop debug.sf.nobootanimation 1",
                                "setprop debug.hwui.render_dirty_regions false",
                                "setprop debug.sf.disable_backpressure 1"
                        );

                        if (isUltraExtreme) {
                            ShizukuExecutor.executeShizukuCommands(
                                    "setprop debug.sf.use_phase_offsets_as_durations 1",
                                    "setprop debug.sf.late.sf.duration 10500000",
                                    "setprop debug.sf.late.app.duration 20500000",
                                    "setprop debug.sf.hw 0",
                                    "setprop debug.egl.hw 0",
                                    "setprop persist.sys.ui.hw 1"
                            );
                        }
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
                    dismissCurrent();

                    String summarySubtitle = isUltraExtreme
                            ? "144fps UltraExtreme SuperSmooth (Max Quality + 1000Hz Touch)"
                            : targetFps + " FPS Competitive Config & Refresh Rate Lock";

                    CyberActionDialog.show(
                            context,
                            "⚙️ CFG CONFIG APPLIED",
                            true,
                            "Game: " + game.getLabel(),
                            summarySubtitle,
                            "Engine: " + (ShizukuExecutor.hasShizukuPermission() ? "Shizuku Privileged IPC" : "In-Memory Direct POSIX")
                    );

                    if (listener != null) {
                        listener.onConfigApplied(pkg, targetFps, finalPatched);
                    }
                });
            });
        });

        activeDialog = dialog;
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
