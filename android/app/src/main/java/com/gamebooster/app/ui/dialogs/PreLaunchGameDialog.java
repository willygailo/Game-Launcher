package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.config.AntiLogPatcher;
import com.gamebooster.app.config.CfgProfileManager;
import com.gamebooster.app.config.CommonConfigTuningInjector;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.gamemanager.GameManagerLauncher;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;

/**
 * PreLaunchGameDialog — Smart Cyberpunk Pre-Launch Performance & Hardware Refresh Rate Dialog.
 *
 * Automatically detects the Android OS and display hardware maximum refresh rate (Hz),
 * filters the available FPS options (e.g. 144Hz screen shows 144/120/90/60 FPS),
 * auto-detects the game title and engine, auto-applies all game-specific config files
 * and Shizuku performance tunings, and launches the game seamlessly.
 */
public class PreLaunchGameDialog {

    private static final String TAG = "PreLaunchGameDialog";
    private static Dialog activeDialog;

    public static void show(Context context, GameAppInfo game) {
        if (context == null || game == null) return;
        if (!(context instanceof Activity)) {
            // If context is not an activity, launch directly
            GameManagerLauncher.launchGame(context, game);
            return;
        }

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_pre_launch_game, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        final String pkg = game.getPackageName();
        final String label = game.getLabel();

        // 1. Bind Identity Views
        ImageView ivIcon = view.findViewById(R.id.iv_pre_launch_game_icon);
        TextView tvTitle = view.findViewById(R.id.tv_pre_launch_game_title);
        TextView tvPkg = view.findViewById(R.id.tv_pre_launch_game_pkg);
        TextView tvEngine = view.findViewById(R.id.tv_pre_launch_game_engine);
        TextView tvDisplayHz = view.findViewById(R.id.tv_pre_launch_display_hz);
        TextView tvEngineBadge = view.findViewById(R.id.tv_pre_launch_engine_badge);

        if (game.getIcon() != null) {
            ivIcon.setImageDrawable(game.getIcon());
        }
        tvTitle.setText(label != null ? label : pkg);
        tvPkg.setText(pkg);
        tvEngine.setText(detectGameEngineDescription(pkg));

        if (ShizukuExecutor.hasShizukuPermission()) {
            tvEngineBadge.setText("[SHIZUKU ACTIVE]");
            tvEngineBadge.setTextColor(Color.parseColor("#00FF66"));
        } else {
            tvEngineBadge.setText("[LIMITED / ROOTLESS]");
            tvEngineBadge.setTextColor(Color.parseColor("#FFAA00"));
        }

        // 2. Hardware Max Refresh Rate Detection
        int maxSupportedHz = getDisplayMaxHz(context);
        tvDisplayHz.setText("Detected Display: Up to " + maxSupportedHz + "Hz Native Refresh Rate");

        // 3. Bind Radio Buttons
        RadioGroup rgFps = view.findViewById(R.id.rg_pre_launch_fps);
        RadioButton rb185 = view.findViewById(R.id.rb_pre_fps_185);
        RadioButton rb165 = view.findViewById(R.id.rb_pre_fps_165);
        RadioButton rb144 = view.findViewById(R.id.rb_pre_fps_144);
        RadioButton rb120 = view.findViewById(R.id.rb_pre_fps_120);
        RadioButton rb90 = view.findViewById(R.id.rb_pre_fps_90);
        RadioButton rb60 = view.findViewById(R.id.rb_pre_fps_60);

        // Dynamically show FPS choices based on detected hardware max Hz
        if (maxSupportedHz >= 185) {
            if (rb185 != null) rb185.setVisibility(View.VISIBLE);
            if (rb165 != null) rb165.setVisibility(View.VISIBLE);
            if (rb144 != null) rb144.setVisibility(View.VISIBLE);
            if (rb120 != null) rb120.setVisibility(View.VISIBLE);
            if (rb90 != null) rb90.setVisibility(View.VISIBLE);
        } else if (maxSupportedHz >= 165) {
            if (rb185 != null) rb185.setVisibility(View.GONE);
            if (rb165 != null) rb165.setVisibility(View.VISIBLE);
            if (rb144 != null) rb144.setVisibility(View.VISIBLE);
            if (rb120 != null) rb120.setVisibility(View.VISIBLE);
            if (rb90 != null) rb90.setVisibility(View.VISIBLE);
        } else if (maxSupportedHz >= 144) {
            if (rb185 != null) rb185.setVisibility(View.GONE);
            if (rb165 != null) rb165.setVisibility(View.GONE);
            if (rb144 != null) rb144.setVisibility(View.VISIBLE);
            if (rb120 != null) rb120.setVisibility(View.VISIBLE);
            if (rb90 != null) rb90.setVisibility(View.VISIBLE);
        } else if (maxSupportedHz >= 120) {
            if (rb185 != null) rb185.setVisibility(View.GONE);
            if (rb165 != null) rb165.setVisibility(View.GONE);
            if (rb144 != null) rb144.setVisibility(View.GONE);
            if (rb120 != null) rb120.setVisibility(View.VISIBLE);
            if (rb90 != null) rb90.setVisibility(View.VISIBLE);
        } else if (maxSupportedHz >= 90) {
            if (rb185 != null) rb185.setVisibility(View.GONE);
            if (rb165 != null) rb165.setVisibility(View.GONE);
            if (rb144 != null) rb144.setVisibility(View.GONE);
            if (rb120 != null) rb120.setVisibility(View.GONE);
            if (rb90 != null) rb90.setVisibility(View.VISIBLE);
        } else {
            if (rb185 != null) rb185.setVisibility(View.GONE);
            if (rb165 != null) rb165.setVisibility(View.GONE);
            if (rb144 != null) rb144.setVisibility(View.GONE);
            if (rb120 != null) rb120.setVisibility(View.GONE);
            if (rb90 != null) rb90.setVisibility(View.GONE);
        }

        // Set active selection: load previously saved FPS or default to hardware max
        int savedFps = GameProfilePreferences.getTargetHz(context, pkg);
        if (savedFps <= 0) {
            savedFps = maxSupportedHz;
        }

        if (savedFps >= 185 && rb185 != null && rb185.getVisibility() == View.VISIBLE) {
            rb185.setChecked(true);
        } else if (savedFps >= 165 && rb165 != null && rb165.getVisibility() == View.VISIBLE) {
            rb165.setChecked(true);
        } else if (savedFps >= 144 && rb144 != null && rb144.getVisibility() == View.VISIBLE) {
            rb144.setChecked(true);
        } else if (savedFps >= 120 && rb120 != null && rb120.getVisibility() == View.VISIBLE) {
            rb120.setChecked(true);
        } else if (savedFps >= 90 && rb90 != null && rb90.getVisibility() == View.VISIBLE) {
            rb90.setChecked(true);
        } else if (rb60 != null) {
            rb60.setChecked(true);
        }

        // 4. Action Buttons
        Button btnCancel = view.findViewById(R.id.btn_pre_launch_cancel);
        Button btnStart = view.findViewById(R.id.btn_pre_launch_start);

        btnCancel.setOnClickListener(v -> dismissCurrent());

        btnStart.setOnClickListener(v -> {
            int selectedFps;
            if (rb185 != null && rb185.isChecked()) {
                selectedFps = 185;
            } else if (rb165 != null && rb165.isChecked()) {
                selectedFps = 165;
            } else if (rb144 != null && rb144.isChecked()) {
                selectedFps = 144;
            } else if (rb120 != null && rb120.isChecked()) {
                selectedFps = 120;
            } else if (rb90 != null && rb90.isChecked()) {
                selectedFps = 90;
            } else {
                selectedFps = 60;
            }

            final int finalFps = selectedFps;

            // Dismiss dialog immediately
            dismissCurrent();

            Toast.makeText(context.getApplicationContext(), "⚡ Pre-Launch Tuning for " + (label != null ? label : pkg) + " (" + finalFps + " FPS)...", Toast.LENGTH_SHORT).show();

            // Build & save competitive profile
            String gameKey = CfgProfileManager.resolveGameKey(pkg);
            CompetitiveCfgProfile profile = CfgProfileManager.loadProfile(context, gameKey);
            if (profile == null) {
                profile = new CompetitiveCfgProfile(gameKey, finalFps, true, true);
            } else {
                profile.setTargetFps(finalFps);
            }
            profile.setAntiLogEnabled(true);
            CfgProfileManager.saveProfile(context, profile);
            GameProfilePreferences.setTargetHz(context, pkg, finalFps);

            // Launch the game via unified engine (which executes full cold-start pre-injection and auto-opens the game)
            GameManagerLauncher.launchGame(context, game);
        });

        Button btnRemoveGame = view.findViewById(R.id.btn_pre_launch_remove_game);
        if (btnRemoveGame != null) {
            btnRemoveGame.setOnClickListener(v -> {
                dismissCurrent();
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("🗑️ REMOVE GAME")
                        .setMessage("Remove " + (label != null ? label : pkg) + " from the Home Launcher list?")
                        .setPositiveButton("REMOVE", (d, w) -> {
                            com.gamebooster.app.games.GameLauncherHelper.removeGameFromHome(context, pkg);
                            Toast.makeText(context, "🗑️ Removed " + (label != null ? label : pkg) + " from Home", Toast.LENGTH_SHORT).show();
                            if (context instanceof com.gamebooster.app.ui.activities.MainActivity) {
                                ((com.gamebooster.app.ui.activities.MainActivity) context).reloadHomeGames();
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            });
        }

        dialog.setCanceledOnTouchOutside(true);
        activeDialog = dialog;
        dialog.show();
    }

    /**
     * Accurately detects the active display's maximum supported hardware refresh rate (Hz).
     */
    @SuppressWarnings("deprecation")
    public static int getDisplayMaxHz(Context context) {
        int maxHz = 60;
        if (context == null) return maxHz;

        try {
            Display display = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    display = context.getDisplay();
                } catch (Throwable ignored) {}
            }
            if (display == null) {
                if (context instanceof Activity) {
                    display = ((Activity) context).getWindowManager().getDefaultDisplay();
                } else {
                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    if (wm != null) display = wm.getDefaultDisplay();
                }
            }

            if (display != null) {
                Display.Mode[] modes = display.getSupportedModes();
                if (modes != null) {
                    for (Display.Mode m : modes) {
                        int rate = Math.round(m.getRefreshRate());
                        if (rate > maxHz) maxHz = rate;
                    }
                }
                int currentRate = Math.round(display.getRefreshRate());
                if (currentRate > maxHz) maxHz = currentRate;
            }
        } catch (Throwable ignored) {}

        // If device spoofing profile is active, check if profile advertises a higher gaming display
        try {
            if (SpoofPreferences.isSpoofEnabled(context)) {
                String activeProfileId = SpoofPreferences.getActiveProfileId(context);
                if (activeProfileId != null) {
                    com.gamebooster.app.spoofer.SpoofProfile p = DeviceSpooferEngine.getProfileById(activeProfileId);
                    if (p != null && p.maxRefreshRateHz > maxHz) {
                        maxHz = p.maxRefreshRateHz;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return maxHz;
    }

    /**
     * Returns a human-friendly description of the detected game engine and title.
     */
    public static String detectGameEngineDescription(String packageName) {
        if (packageName == null) return "🎮 Android Game • Auto-Tuning Ready";
        String pkg = packageName.toLowerCase();

        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") ||
            pkg.contains("vng.pubgmobile") || pkg.contains("arenabreakout") || pkg.contains("deltaforce") ||
            pkg.contains("farlight") || pkg.contains("projectc") || pkg.contains("valorant")) {
            return "🎮 Unreal Engine 4/5 (ShadowTracker) • Auto-Patch Ready";
        } else if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            return "🎮 Moonton Unity Engine (Dragon2017) • Ultra FPS Ready";
        } else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone") || pkg.contains("bloodstrike")) {
            return "🎮 IW / Unity Engine • Ultra Graphics Ready";
        } else if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") ||
                   pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") || pkg.contains("wutheringwaves")) {
            return "🎮 MiHoYo / Kuro Next-Gen Engine • 120 FPS Ready";
        } else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
            return "🎮 Garena 111dots Unity Engine • High FPS Ready";
        } else if (pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") ||
                   pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("wildrift")) {
            return "🎮 TiMi / Riot Games Engine • Extreme Hz Ready";
        } else if (pkg.contains("roblox")) {
            return "🎮 Roblox Luau 3D Engine • High-Rate Ready";
        } else if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
            return "🎮 Axlebolt Unity FPS Engine • 144/165Hz Ready";
        } else if (pkg.contains("carx") || pkg.contains("asphalt")) {
            return "🎮 CarX Engine / Havok Physics • Ultra Tier Ready";
        } else if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans")) {
            return "🎮 Supercell Titan Engine • High Refresh Ready";
        }
        return "🎮 Android Game • Auto-Tuning Ready";
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
}
