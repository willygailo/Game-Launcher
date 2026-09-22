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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.gamebooster.app.config.MlbbHeroScriptRegistry;
import com.gamebooster.app.config.MlbbHeroScriptRegistry.HeroEntry;
import com.gamebooster.app.config.MlbbHeroScriptDispatcher;

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

        // 3. Bind Radio Buttons (120 FPS to 185 FPS Esports Standard)
        RadioGroup rgFps = view.findViewById(R.id.rg_pre_launch_fps);
        RadioButton rb185 = view.findViewById(R.id.rb_pre_fps_185);
        RadioButton rb165 = view.findViewById(R.id.rb_pre_fps_165);
        RadioButton rb144 = view.findViewById(R.id.rb_pre_fps_144);
        RadioButton rb120 = view.findViewById(R.id.rb_pre_fps_120);

        // All esports high-framerate tiers available for user override
        if (rb185 != null) rb185.setVisibility(View.VISIBLE);
        if (rb165 != null) rb165.setVisibility(View.VISIBLE);
        if (rb144 != null) rb144.setVisibility(View.VISIBLE);
        if (rb120 != null) rb120.setVisibility(View.VISIBLE);

        // Set active selection: load previously saved FPS or default to 185 FPS (minimum 120 FPS)
        int savedFps = GameProfilePreferences.getTargetHz(context, pkg);
        if (savedFps <= 0) {
            savedFps = maxSupportedHz >= 120 ? maxSupportedHz : 185;
        }

        if (savedFps >= 185 && rb185 != null) {
            rb185.setChecked(true);
        } else if (savedFps >= 165 && rb165 != null) {
            rb165.setChecked(true);
        } else if (savedFps >= 144 && rb144 != null) {
            rb144.setChecked(true);
        } else if (rb120 != null) {
            rb120.setChecked(true);
        }

        // 3.5 Drone View Switch State & Zoom Tier Selector
        androidx.appcompat.widget.SwitchCompat switchDrone = view.findViewById(R.id.switch_pre_launch_drone_view);
        android.view.View layoutDroneTier = view.findViewById(R.id.layout_pre_launch_drone_tier);
        android.widget.RadioGroup rgDroneTier = view.findViewById(R.id.rg_pre_launch_drone_tier);
        android.widget.RadioButton rb15 = view.findViewById(R.id.rb_drone_1_5x);
        android.widget.RadioButton rb20 = view.findViewById(R.id.rb_drone_2x);
        android.widget.RadioButton rb30 = view.findViewById(R.id.rb_drone_3x);
        android.widget.RadioButton rb40 = view.findViewById(R.id.rb_drone_4x);
        android.widget.RadioButton rb50 = view.findViewById(R.id.rb_drone_5x);

        String gameKey = CfgProfileManager.resolveGameKey(pkg);
        CompetitiveCfgProfile currentProfile = CfgProfileManager.loadProfile(context, gameKey);
        boolean isMlbb = pkg.toLowerCase().contains("mobile.legends") || pkg.toLowerCase().contains("mobilelegends");

        if (layoutDroneTier != null) {
            layoutDroneTier.setVisibility(isMlbb ? android.view.View.VISIBLE : android.view.View.GONE);
        }

        if (switchDrone != null) {
            boolean droneEnabled = (currentProfile != null) ? currentProfile.isDroneViewUltraEnabled() : true;
            switchDrone.setChecked(droneEnabled);
            if (layoutDroneTier != null && isMlbb) {
                layoutDroneTier.setVisibility(droneEnabled ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            switchDrone.setOnCheckedChangeListener((btn, checked) -> {
                if (layoutDroneTier != null && isMlbb) {
                    layoutDroneTier.setVisibility(checked ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            });
        }

        int currentTier = (currentProfile != null) ? currentProfile.getDroneViewTier() : com.gamebooster.app.config.MlbbDroneViewPatcher.DEFAULT_TIER;
        if (currentTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_1_5X && rb15 != null) rb15.setChecked(true);
        else if (currentTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_2X && rb20 != null) rb20.setChecked(true);
        else if (currentTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_4X && rb40 != null) rb40.setChecked(true);
        else if (currentTier == com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_5X && rb50 != null) rb50.setChecked(true);
        else if (rb30 != null) rb30.setChecked(true);

        // 3.6 2026 Season 42 Hero Script Damage & Modifiers Selector (MLBB Only)
        android.view.View layoutHeroSelector = view.findViewById(R.id.layout_pre_launch_hero_selector);
        final Spinner spinnerHero = view.findViewById(R.id.spinner_pre_launch_hero_script);
        final List<Integer> heroIdList = new ArrayList<>();

        if (layoutHeroSelector != null) {
            layoutHeroSelector.setVisibility(isMlbb ? android.view.View.VISIBLE : android.view.View.GONE);
        }

        if (isMlbb && spinnerHero != null) {
            List<String> heroLabels = new ArrayList<>();
            heroLabels.add("🌟 All Meta Heroes (Global Boost + Top 20)");
            heroIdList.add(0);

            HeroEntry[] metaHeroes = MlbbHeroScriptRegistry.getMetaHeroes();
            if (metaHeroes != null) {
                for (HeroEntry h : metaHeroes) {
                    if (h == null) continue;
                    heroLabels.add("⚔️ " + h.name + " (" + h.role + ") — [ID:" + h.id + "]");
                    heroIdList.add(h.id);
                }
            }

            ArrayAdapter<String> heroAdapter = new ArrayAdapter<String>(
                    context, android.R.layout.simple_spinner_dropdown_item, heroLabels) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    if (v instanceof TextView) {
                        ((TextView) v).setTextColor(Color.WHITE);
                        ((TextView) v).setTextSize(11f);
                    }
                    return v;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View v = super.getDropDownView(position, convertView, parent);
                    if (v instanceof TextView) {
                        ((TextView) v).setTextColor(Color.WHITE);
                        ((TextView) v).setBackgroundColor(Color.parseColor("#1A2332"));
                        ((TextView) v).setTextSize(12f);
                        ((TextView) v).setPadding(24, 16, 24, 16);
                    }
                    return v;
                }
            };
            spinnerHero.setAdapter(heroAdapter);
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
            } else {
                selectedFps = 120;
            }

            final int finalFps = selectedFps;
            final boolean droneEnabled = switchDrone != null && switchDrone.isChecked();

            int selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.DEFAULT_TIER;
            if (rb15 != null && rb15.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_1_5X;
            else if (rb20 != null && rb20.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_2X;
            else if (rb40 != null && rb40.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_4X;
            else if (rb50 != null && rb50.isChecked()) selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_5X;
            else selectedTier = com.gamebooster.app.config.MlbbDroneViewPatcher.TIER_3X;

            // Dismiss dialog immediately
            dismissCurrent();

            String modeName = finalFps >= 144 ? "🚀 NO LIMIT FPS GAMING MODE (" + finalFps + " FPS)" : "🛡️ BALANCE HIGH FPS MODE (120 FPS)";
            Toast.makeText(context.getApplicationContext(), modeName + " — " + (label != null ? label : pkg), Toast.LENGTH_SHORT).show();

            // Build & save competitive profile — always start from defaultCompetitive()
            // so ALL features (aimAssist, damage, recoil, trackingBullet, etc.) are
            // guaranteed ON regardless of what the old saved profile had.
            final String savedGameKey = gameKey;
            CompetitiveCfgProfile profile = CompetitiveCfgProfile.defaultCompetitive(gameKey);
            profile.setTargetFps(finalFps);
            profile.setDroneViewUltraEnabled(droneEnabled);
            profile.setDroneViewTier(selectedTier);
            profile.setAntiLogEnabled(true);
            CfgProfileManager.saveProfile(context, profile);
            GameProfilePreferences.setTargetHz(context, pkg, finalFps);

            // Immediate async pre-launch inject — don't wait for the 15s lobby delay.
            // Applies the full tuning suite NOW so everything is live when the game opens.
            final CompetitiveCfgProfile launchProfile = profile;
            AppExecutors.getInstance().executeCommand(() -> {
                try {
                    com.gamebooster.app.config.CommonConfigTuningInjector.applyAllEnabledTunings(pkg, launchProfile);
                } catch (Throwable ignored) {}
            });

            // 2026 Hero Script Dispatch for MLBB
            if (isMlbb) {
                int heroPos = (spinnerHero != null) ? spinnerHero.getSelectedItemPosition() : 0;
                int heroId = (heroPos >= 0 && heroPos < heroIdList.size()) ? heroIdList.get(heroPos) : 0;
                try {
                    MlbbHeroScriptDispatcher.dispatch(context.getApplicationContext(), pkg, heroId);
                } catch (Throwable ignored) {}
            }

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
            try {
                display = context.getDisplay();
            } catch (Throwable ignored) {}
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
