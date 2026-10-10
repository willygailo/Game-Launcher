package com.gamebooster.app.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.mods.GameModEngine;
import com.gamebooster.app.mods.GameModProfile;

/**
 * GameModFragment — UI control panel for MLBB and CODM mods.
 *
 * Shows per-game toggle switches + sliders for:
 *  - Damage multiplier (1x–10x)
 *  - Attack speed multiplier (MLBB)
 *  - No skill cooldown (MLBB)
 *  - Map vision / fog remove (MLBB)
 *  - Aimbot (CODM)
 *  - Speed hack (CODM)
 *  - No recoil (CODM)
 *  - FPS unlock
 *  - Anti-ban SSL unpin
 *
 * Also provides:
 *  - PULL APK buttons (ADB pull from device)
 *  - INJECT NOW buttons (immediate Frida injection into running game)
 *  - Frida server start/stop control
 */
public class GameModFragment extends Fragment {

    private static final String TAG = "GameModFragment";
    private static final Handler UI = new Handler(Looper.getMainLooper());

    // ── MLBB views ────────────────────────────────────────────────────────────
    private SwitchCompat swMlbbDamage, swMlbbAspd, swMlbbDef, swMlbbCdr,
                         swMlbbMana, swMlbbEnergy, swMlbbDrone, swMlbbNoCool,
                         swMlbbMap, swMlbbSpread, swMlbbFps, swMlbbAntiBan;
    private SeekBar      sbMlbbDamage, sbMlbbAspd, sbMlbbDef, sbMlbbCdr, sbMlbbDrone, sbMlbbFps;
    private TextView     tvMlbbDmgVal, tvMlbbAspdVal, tvMlbbDefVal, tvMlbbCdrVal,
                         tvMlbbManaVal, tvMlbbEnergyVal, tvMlbbDroneVal, tvMlbbFpsVal;
    private Button       btnMlbbPull, btnMlbbInject;

    // ── CODM views ────────────────────────────────────────────────────────────
    private SwitchCompat swCodmAimbot, swCodmDamage, swCodmSpeed,
                         swCodmAttack, swCodmDefense, swCodmHealth, swCodmCrit,
                         swCodmRecoil, swCodmSpread, swCodmFps, swCodmAntiBan;
    private SeekBar      sbCodmDamage, sbCodmSpeed, sbCodmAttack, sbCodmDefense,
                         sbCodmHealth, sbCodmCrit, sbCodmFps;
    private TextView     tvCodmDmgVal, tvCodmSpeedVal, tvCodmAttackVal, tvCodmDefVal,
                         tvCodmHpVal, tvCodmCritVal, tvCodmFpsVal;
    private Button       btnCodmPull, btnCodmInject;


    // ── Common ────────────────────────────────────────────────────────────────
    private TextView tvModStatus, tvFridaStatus;
    private Button   btnFridaToggle, btnModSave;

    private GameModProfile profile;
    private boolean fridaRunning = false;

    // Multiplier mapping for seekbars: index → float value
    private static final float[] DMG_VALUES   = {1f,2f,3f,4f,5f,6f,7f,8f,9f,10f};
    private static final float[] ASPD_VALUES  = {0.5f,1f,1.5f,2f,2.5f,3f,3.5f,4f,4.5f,5f};
    private static final float[] DEF_VALUES   = {0.5f,1f,1.5f,2f,2.5f,3f,3.5f,4f,4.5f,5f};
    private static final int[]   CDR_VALUES   = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    private static final int[]   DRONE_TIERS  = {15, 20, 30, 40, 50, 100};
    private static final String[] DRONE_LABELS = {"1.5X", "2.0X", "3.0X", "4.0X", "5.0X", "10.0X"};
    private static final float[] SPEED_VALUES = {1f,1.25f,1.5f,1.75f,2f,2.5f,3f,3.5f,4f,5f};
    private static final int[]   FPS_VALUES   = {60, 90, 120, 144, 165};

    public static GameModFragment newInstance() { return new GameModFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_mod, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        bindViews(v);
        profile = GameModProfile.load(requireContext());
        populateFromProfile();
        wireListeners();
        setStatus("IDLE — configure mods and launch a game");
    }

    // ─── Bind ─────────────────────────────────────────────────────────────────

    private void bindViews(View v) {
        tvModStatus   = v.findViewById(R.id.tv_mod_status);
        tvFridaStatus = v.findViewById(R.id.tv_frida_status);
        btnFridaToggle= v.findViewById(R.id.btn_frida_toggle);
        btnModSave    = v.findViewById(R.id.btn_mod_save);

        // MLBB
        swMlbbDamage  = v.findViewById(R.id.sw_mlbb_damage);
        swMlbbAspd    = v.findViewById(R.id.sw_mlbb_aspd);
        swMlbbDef     = v.findViewById(R.id.sw_mlbb_def);
        swMlbbCdr     = v.findViewById(R.id.sw_mlbb_cdr);
        swMlbbMana    = v.findViewById(R.id.sw_mlbb_mana);
        swMlbbEnergy  = v.findViewById(R.id.sw_mlbb_energy);
        swMlbbDrone   = v.findViewById(R.id.sw_mlbb_drone);
        swMlbbNoCool  = v.findViewById(R.id.sw_mlbb_nocool);
        swMlbbMap     = v.findViewById(R.id.sw_mlbb_map);
        swMlbbSpread  = v.findViewById(R.id.sw_mlbb_spread);
        swMlbbFps     = v.findViewById(R.id.sw_mlbb_fps);
        swMlbbAntiBan = v.findViewById(R.id.sw_mlbb_antiban);

        sbMlbbDamage  = v.findViewById(R.id.sb_mlbb_damage);
        sbMlbbAspd    = v.findViewById(R.id.sb_mlbb_aspd);
        sbMlbbDef     = v.findViewById(R.id.sb_mlbb_def);
        sbMlbbCdr     = v.findViewById(R.id.sb_mlbb_cdr);
        sbMlbbDrone   = v.findViewById(R.id.sb_mlbb_drone);
        sbMlbbFps     = v.findViewById(R.id.sb_mlbb_fps);

        tvMlbbDmgVal  = v.findViewById(R.id.tv_mlbb_dmg_value);
        tvMlbbAspdVal = v.findViewById(R.id.tv_mlbb_aspd_value);
        tvMlbbDefVal  = v.findViewById(R.id.tv_mlbb_def_value);
        tvMlbbCdrVal  = v.findViewById(R.id.tv_mlbb_cdr_value);
        tvMlbbManaVal = v.findViewById(R.id.tv_mlbb_mana_value);
        tvMlbbEnergyVal = v.findViewById(R.id.tv_mlbb_energy_value);
        tvMlbbDroneVal = v.findViewById(R.id.tv_mlbb_drone_value);
        tvMlbbFpsVal  = v.findViewById(R.id.tv_mlbb_fps_value);

        btnMlbbPull   = v.findViewById(R.id.btn_mlbb_pull);
        btnMlbbInject = v.findViewById(R.id.btn_mlbb_inject);

        // CODM
        swCodmAimbot  = v.findViewById(R.id.sw_codm_aimbot);
        swCodmDamage  = v.findViewById(R.id.sw_codm_damage);
        swCodmSpeed   = v.findViewById(R.id.sw_codm_speed);
        swCodmAttack  = v.findViewById(R.id.sw_codm_attack);
        swCodmDefense = v.findViewById(R.id.sw_codm_defense);
        swCodmHealth  = v.findViewById(R.id.sw_codm_health);
        swCodmCrit    = v.findViewById(R.id.sw_codm_crit);
        swCodmRecoil  = v.findViewById(R.id.sw_codm_recoil);
        swCodmSpread  = v.findViewById(R.id.sw_codm_spread);
        swCodmFps     = v.findViewById(R.id.sw_codm_fps);
        swCodmAntiBan = v.findViewById(R.id.sw_codm_antiban);

        sbCodmDamage  = v.findViewById(R.id.sb_codm_damage);
        sbCodmSpeed   = v.findViewById(R.id.sb_codm_speed);
        sbCodmAttack  = v.findViewById(R.id.sb_codm_attack);
        sbCodmDefense = v.findViewById(R.id.sb_codm_defense);
        sbCodmHealth  = v.findViewById(R.id.sb_codm_health);
        sbCodmCrit    = v.findViewById(R.id.sb_codm_crit);
        sbCodmFps     = v.findViewById(R.id.sb_codm_fps);

        tvCodmDmgVal    = v.findViewById(R.id.tv_codm_dmg_value);
        tvCodmSpeedVal  = v.findViewById(R.id.tv_codm_speed_value);
        tvCodmAttackVal = v.findViewById(R.id.tv_codm_attack_value);
        tvCodmDefVal    = v.findViewById(R.id.tv_codm_def_value);
        tvCodmHpVal     = v.findViewById(R.id.tv_codm_hp_value);
        tvCodmCritVal   = v.findViewById(R.id.tv_codm_crit_value);
        tvCodmFpsVal    = v.findViewById(R.id.tv_codm_fps_value);

        btnCodmPull   = v.findViewById(R.id.btn_codm_pull);
        btnCodmInject = v.findViewById(R.id.btn_codm_inject);
    }

    // ─── Populate from profile ────────────────────────────────────────────────

    private void populateFromProfile() {
        // MLBB
        swMlbbDamage.setChecked(profile.mlbbDamageEnabled);
        swMlbbAspd.setChecked(profile.mlbbAttackSpeedEnabled);
        if (swMlbbDef != null) swMlbbDef.setChecked(profile.mlbbDefenseEnabled);
        if (swMlbbCdr != null) swMlbbCdr.setChecked(profile.mlbbCooldownReductionEnabled);
        if (swMlbbMana != null) swMlbbMana.setChecked(profile.mlbbManaEnabled);
        if (swMlbbEnergy != null) swMlbbEnergy.setChecked(profile.mlbbEnergyEnabled);
        if (swMlbbDrone != null) swMlbbDrone.setChecked(profile.mlbbDroneViewEnabled);
        swMlbbNoCool.setChecked(profile.mlbbNoCooldown);
        swMlbbMap.setChecked(profile.mlbbMapHack);
        if (swMlbbSpread != null) swMlbbSpread.setChecked(profile.mlbbNoSpread);
        swMlbbFps.setChecked(profile.mlbbFpsUnlock);
        swMlbbAntiBan.setChecked(profile.mlbbAntiBan);

        sbMlbbDamage.setProgress(multToIndex(DMG_VALUES, profile.mlbbDamageMult));
        sbMlbbAspd.setProgress(multToIndex(ASPD_VALUES, profile.mlbbAttackSpeedMult));
        if (sbMlbbDef != null) sbMlbbDef.setProgress(multToIndex(DEF_VALUES, profile.mlbbDefenseMult));
        if (sbMlbbCdr != null) sbMlbbCdr.setProgress(cdrToIndex(profile.mlbbCooldownReductionPct));
        if (sbMlbbDrone != null) sbMlbbDrone.setProgress(droneTierToIndex(profile.mlbbDroneTier));
        if (sbMlbbFps != null) sbMlbbFps.setProgress(fpsToIndex(profile.mlbbTargetFps));

        tvMlbbDmgVal.setText(formatMult(profile.mlbbDamageMult));
        tvMlbbAspdVal.setText(formatMult(profile.mlbbAttackSpeedMult));
        if (tvMlbbDefVal != null) tvMlbbDefVal.setText(formatMult(profile.mlbbDefenseMult));
        if (tvMlbbCdrVal != null) tvMlbbCdrVal.setText(profile.mlbbCooldownReductionPct + "%");
        if (tvMlbbManaVal != null) tvMlbbManaVal.setText(profile.mlbbInfiniteMana ? "INFINITE" : "BOOST");
        if (tvMlbbEnergyVal != null) tvMlbbEnergyVal.setText(profile.mlbbInfiniteEnergy ? "INFINITE" : "BOOST");
        if (tvMlbbDroneVal != null) tvMlbbDroneVal.setText(droneTierToLabel(profile.mlbbDroneTier));
        tvMlbbFpsVal.setText(profile.mlbbTargetFps + "fps");

        // CODM
        swCodmAimbot.setChecked(profile.codmAimbot);
        swCodmDamage.setChecked(profile.codmDamageEnabled);
        swCodmSpeed.setChecked(profile.codmSpeedEnabled);
        if (swCodmAttack != null) swCodmAttack.setChecked(profile.codmAttackEnabled);
        if (swCodmDefense != null) swCodmDefense.setChecked(profile.codmDefenseEnabled);
        if (swCodmHealth != null) swCodmHealth.setChecked(profile.codmHealthEnabled);
        if (swCodmCrit != null) swCodmCrit.setChecked(profile.codmCriticalEnabled);
        swCodmRecoil.setChecked(profile.codmNoRecoil);
        if (swCodmSpread != null) swCodmSpread.setChecked(profile.codmNoSpread);
        swCodmFps.setChecked(profile.codmFpsUnlock);
        swCodmAntiBan.setChecked(profile.codmAntiBan);

        sbCodmDamage.setProgress(multToIndex(DMG_VALUES, profile.codmDamageMult));
        sbCodmSpeed.setProgress(multToIndex(SPEED_VALUES, profile.codmSpeedMult));
        if (sbCodmAttack != null) sbCodmAttack.setProgress(multToIndex(ASPD_VALUES, profile.codmAttackMult));
        if (sbCodmDefense != null) sbCodmDefense.setProgress(multToIndex(DEF_VALUES, profile.codmDefenseMult));
        if (sbCodmHealth != null) sbCodmHealth.setProgress(multToIndex(DMG_VALUES, profile.codmHealthMult));
        if (sbCodmCrit != null) sbCodmCrit.setProgress(multToIndex(DMG_VALUES, profile.codmCriticalMult));
        if (sbCodmFps != null) sbCodmFps.setProgress(fpsToIndex(profile.codmTargetFps));

        tvCodmDmgVal.setText(formatMult(profile.codmDamageMult));
        tvCodmSpeedVal.setText(formatMult(profile.codmSpeedMult));
        if (tvCodmAttackVal != null) tvCodmAttackVal.setText(formatMult(profile.codmAttackMult));
        if (tvCodmDefVal != null) tvCodmDefVal.setText(formatMult(profile.codmDefenseMult));
        if (tvCodmHpVal != null) tvCodmHpVal.setText(formatMult(profile.codmHealthMult));
        if (tvCodmCritVal != null) tvCodmCritVal.setText(formatMult(profile.codmCriticalMult));
        tvCodmFpsVal.setText(profile.codmTargetFps + "fps");
    }


    // ─── Wire Listeners ───────────────────────────────────────────────────────

    private void wireListeners() {
        // MLBB sliders
        sbMlbbDamage.setOnSeekBarChangeListener(sliderListener(sbMlbbDamage, DMG_VALUES, tvMlbbDmgVal,
            v -> profile.mlbbDamageMult = v));
        sbMlbbAspd.setOnSeekBarChangeListener(sliderListener(sbMlbbAspd, ASPD_VALUES, tvMlbbAspdVal,
            v -> profile.mlbbAttackSpeedMult = v));
        if (sbMlbbDef != null) {
            sbMlbbDef.setOnSeekBarChangeListener(sliderListener(sbMlbbDef, DEF_VALUES, tvMlbbDefVal,
                v -> profile.mlbbDefenseMult = v));
        }
        if (sbMlbbCdr != null) {
            sbMlbbCdr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                    int cdr = CDR_VALUES[Math.min(p, CDR_VALUES.length - 1)];
                    profile.mlbbCooldownReductionPct = cdr;
                    if (tvMlbbCdrVal != null) tvMlbbCdrVal.setText(cdr + "%");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }
        if (sbMlbbDrone != null) {
            sbMlbbDrone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                    int tier = DRONE_TIERS[Math.min(p, DRONE_TIERS.length - 1)];
                    profile.mlbbDroneTier = tier;
                    if (tvMlbbDroneVal != null) tvMlbbDroneVal.setText(droneTierToLabel(tier));
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }
        if (sbMlbbFps != null) {
            sbMlbbFps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                    int fps = FPS_VALUES[Math.min(p, FPS_VALUES.length - 1)];
                    profile.mlbbTargetFps = fps;
                    tvMlbbFpsVal.setText(fps + "fps");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }

        // CODM sliders
        sbCodmDamage.setOnSeekBarChangeListener(sliderListener(sbCodmDamage, DMG_VALUES, tvCodmDmgVal,
            v -> profile.codmDamageMult = v));
        sbCodmSpeed.setOnSeekBarChangeListener(sliderListener(sbCodmSpeed, SPEED_VALUES, tvCodmSpeedVal,
            v -> profile.codmSpeedMult = v));
        if (sbCodmAttack != null) {
            sbCodmAttack.setOnSeekBarChangeListener(sliderListener(sbCodmAttack, ASPD_VALUES, tvCodmAttackVal,
                v -> profile.codmAttackMult = v));
        }
        if (sbCodmDefense != null) {
            sbCodmDefense.setOnSeekBarChangeListener(sliderListener(sbCodmDefense, DEF_VALUES, tvCodmDefVal,
                v -> profile.codmDefenseMult = v));
        }
        if (sbCodmHealth != null) {
            sbCodmHealth.setOnSeekBarChangeListener(sliderListener(sbCodmHealth, DMG_VALUES, tvCodmHpVal,
                v -> profile.codmHealthMult = v));
        }
        if (sbCodmCrit != null) {
            sbCodmCrit.setOnSeekBarChangeListener(sliderListener(sbCodmCrit, DMG_VALUES, tvCodmCritVal,
                v -> profile.codmCriticalMult = v));
        }
        if (sbCodmFps != null) {
            sbCodmFps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                    int fps = FPS_VALUES[Math.min(p, FPS_VALUES.length - 1)];
                    profile.codmTargetFps = fps;
                    tvCodmFpsVal.setText(fps + "fps");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }

        // MLBB switches
        swMlbbDamage.setOnCheckedChangeListener((b, c) -> profile.mlbbDamageEnabled = c);
        swMlbbAspd.setOnCheckedChangeListener((b, c) -> profile.mlbbAttackSpeedEnabled = c);
        if (swMlbbDef != null) swMlbbDef.setOnCheckedChangeListener((b, c) -> profile.mlbbDefenseEnabled = c);
        if (swMlbbCdr != null) swMlbbCdr.setOnCheckedChangeListener((b, c) -> profile.mlbbCooldownReductionEnabled = c);
        if (swMlbbMana != null) swMlbbMana.setOnCheckedChangeListener((b, c) -> {
            profile.mlbbManaEnabled = c;
            if (tvMlbbManaVal != null) tvMlbbManaVal.setText(profile.mlbbInfiniteMana ? "INFINITE" : "BOOST");
        });
        if (swMlbbEnergy != null) swMlbbEnergy.setOnCheckedChangeListener((b, c) -> {
            profile.mlbbEnergyEnabled = c;
            if (tvMlbbEnergyVal != null) tvMlbbEnergyVal.setText(profile.mlbbInfiniteEnergy ? "INFINITE" : "BOOST");
        });
        if (swMlbbDrone != null) swMlbbDrone.setOnCheckedChangeListener((b, c) -> profile.mlbbDroneViewEnabled = c);
        swMlbbNoCool.setOnCheckedChangeListener((b, c) -> profile.mlbbNoCooldown = c);
        swMlbbMap.setOnCheckedChangeListener((b, c) -> profile.mlbbMapHack = c);
        if (swMlbbSpread != null) swMlbbSpread.setOnCheckedChangeListener((b, c) -> profile.mlbbNoSpread = c);
        swMlbbFps.setOnCheckedChangeListener((b, c) -> profile.mlbbFpsUnlock = c);
        swMlbbAntiBan.setOnCheckedChangeListener((b, c) -> profile.mlbbAntiBan = c);

        // CODM switches
        swCodmAimbot.setOnCheckedChangeListener((b, c) -> profile.codmAimbot = c);
        swCodmDamage.setOnCheckedChangeListener((b, c) -> profile.codmDamageEnabled = c);
        swCodmSpeed.setOnCheckedChangeListener((b, c) -> profile.codmSpeedEnabled = c);
        if (swCodmAttack != null) swCodmAttack.setOnCheckedChangeListener((b, c) -> profile.codmAttackEnabled = c);
        if (swCodmDefense != null) swCodmDefense.setOnCheckedChangeListener((b, c) -> profile.codmDefenseEnabled = c);
        if (swCodmHealth != null) swCodmHealth.setOnCheckedChangeListener((b, c) -> profile.codmHealthEnabled = c);
        if (swCodmCrit != null) swCodmCrit.setOnCheckedChangeListener((b, c) -> profile.codmCriticalEnabled = c);
        swCodmRecoil.setOnCheckedChangeListener((b, c) -> profile.codmNoRecoil = c);
        if (swCodmSpread != null) swCodmSpread.setOnCheckedChangeListener((b, c) -> profile.codmNoSpread = c);
        swCodmFps.setOnCheckedChangeListener((b, c) -> profile.codmFpsUnlock = c);
        swCodmAntiBan.setOnCheckedChangeListener((b, c) -> profile.codmAntiBan = c);

        // Pull APK buttons
        btnMlbbPull.setOnClickListener(v -> pullApk(GameModEngine.PKG_MLBB, btnMlbbPull));
        btnCodmPull.setOnClickListener(v -> pullApk(GameModEngine.PKG_CODM, btnCodmPull));

        // Inject NOW buttons
        btnMlbbInject.setOnClickListener(v -> injectNow(GameModEngine.PKG_MLBB, btnMlbbInject));
        btnCodmInject.setOnClickListener(v -> injectNow(GameModEngine.PKG_CODM, btnCodmInject));

        // Frida server toggle
        btnFridaToggle.setOnClickListener(v -> toggleFridaServer());

        // Save
        btnModSave.setOnClickListener(v -> {
            collectFromUI();
            profile.save(requireContext());
            toast("✅ Mod profile saved");
            setStatus("Profile saved — will auto-apply on next game launch");
        });
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private void pullApk(String pkg, Button btn) {
        String outputPath = "/storage/emulated/0/Download/gb_mods/"
            + (GameModEngine.PKG_MLBB.equals(pkg) ? "mlbb" : "codm") + "_base.apk";
        btn.setEnabled(false);
        btn.setText("PULLING...");
        setStatus("Pulling base.apk for " + pkg + "...");

        GameModEngine.pullBaseApk(pkg, outputPath, (success, msg) ->
            UI.post(() -> {
                btn.setEnabled(true);
                btn.setText("PULL APK");
                if (success) {
                    setStatus("✅ Pulled → " + outputPath);
                    toast("APK pulled to Downloads/gb_mods/");
                } else {
                    setStatus("❌ Pull failed: " + msg);
                    toast("Pull failed: " + msg);
                }
            })
        );
    }

    private void injectNow(String pkg, Button btn) {
        collectFromUI();
        btn.setEnabled(false);
        btn.setText("INJECTING...");
        setStatus("Starting Frida + injecting into " + pkg + "...");

        AppExecutors.getInstance().executeCommand(() -> {
            GameModEngine.startFridaServer(requireContext());
            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
            GameModEngine.applyMods(requireContext(), pkg, profile);

            UI.post(() -> {
                btn.setEnabled(true);
                btn.setText("INJECT NOW");
                setStatus("💉 Injected into " + pkg + " — check logcat for hook status");
                toast("Frida injected into " + (GameModEngine.PKG_MLBB.equals(pkg) ? "MLBB" : "CODM"));
                updateFridaStatus(true);
            });
        });
    }

    private void toggleFridaServer() {
        if (fridaRunning) {
            GameModEngine.stopFridaServer();
            updateFridaStatus(false);
            setStatus("Frida server stopped");
        } else {
            setStatus("Starting Frida server...");
            GameModEngine.startFridaServer(requireContext());
            UI.postDelayed(() -> {
                updateFridaStatus(true);
                setStatus("Frida server running");
            }, 1200);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void collectFromUI() {
        profile.mlbbDamageEnabled     = swMlbbDamage.isChecked();
        profile.mlbbAttackSpeedEnabled = swMlbbAspd.isChecked();
        if (swMlbbDef != null) profile.mlbbDefenseEnabled = swMlbbDef.isChecked();
        if (swMlbbCdr != null) profile.mlbbCooldownReductionEnabled = swMlbbCdr.isChecked();
        if (swMlbbMana != null) profile.mlbbManaEnabled = swMlbbMana.isChecked();
        if (swMlbbEnergy != null) profile.mlbbEnergyEnabled = swMlbbEnergy.isChecked();
        if (swMlbbDrone != null) profile.mlbbDroneViewEnabled = swMlbbDrone.isChecked();
        profile.mlbbNoCooldown         = swMlbbNoCool.isChecked();
        profile.mlbbMapHack            = swMlbbMap.isChecked();
        profile.mlbbFpsUnlock          = swMlbbFps.isChecked();
        profile.mlbbAntiBan            = swMlbbAntiBan.isChecked();
        profile.mlbbDamageMult         = DMG_VALUES[sbMlbbDamage.getProgress()];
        profile.mlbbAttackSpeedMult    = ASPD_VALUES[sbMlbbAspd.getProgress()];
        if (sbMlbbDef != null) profile.mlbbDefenseMult = DEF_VALUES[sbMlbbDef.getProgress()];
        if (sbMlbbCdr != null) profile.mlbbCooldownReductionPct = CDR_VALUES[sbMlbbCdr.getProgress()];
        if (sbMlbbDrone != null) profile.mlbbDroneTier = DRONE_TIERS[sbMlbbDrone.getProgress()];

        profile.codmAimbot        = swCodmAimbot.isChecked();
        profile.codmDamageEnabled = swCodmDamage.isChecked();
        profile.codmSpeedEnabled  = swCodmSpeed.isChecked();
        if (swCodmAttack != null) profile.codmAttackEnabled = swCodmAttack.isChecked();
        if (swCodmDefense != null) profile.codmDefenseEnabled = swCodmDefense.isChecked();
        if (swCodmHealth != null) profile.codmHealthEnabled = swCodmHealth.isChecked();
        if (swCodmCrit != null) profile.codmCriticalEnabled = swCodmCrit.isChecked();
        profile.codmNoRecoil      = swCodmRecoil.isChecked();
        if (swCodmSpread != null) profile.codmNoSpread = swCodmSpread.isChecked();
        profile.codmFpsUnlock     = swCodmFps.isChecked();
        profile.codmAntiBan       = swCodmAntiBan.isChecked();
        profile.codmDamageMult    = DMG_VALUES[sbCodmDamage.getProgress()];
        profile.codmSpeedMult     = SPEED_VALUES[sbCodmSpeed.getProgress()];
        if (sbCodmAttack != null) profile.codmAttackMult = ASPD_VALUES[sbCodmAttack.getProgress()];
        if (sbCodmDefense != null) profile.codmDefenseMult = DEF_VALUES[sbCodmDefense.getProgress()];
        if (sbCodmHealth != null) profile.codmHealthMult = DMG_VALUES[sbCodmHealth.getProgress()];
        if (sbCodmCrit != null) profile.codmCriticalMult = DMG_VALUES[sbCodmCrit.getProgress()];
        if (sbMlbbFps != null) {
            profile.mlbbTargetFps = FPS_VALUES[Math.min(sbMlbbFps.getProgress(), FPS_VALUES.length - 1)];
        }
        if (sbCodmFps != null) {
            profile.codmTargetFps = FPS_VALUES[Math.min(sbCodmFps.getProgress(), FPS_VALUES.length - 1)];
        }
    }


    private static int cdrToIndex(int pct) {
        for (int i = 0; i < CDR_VALUES.length; i++) {
            if (CDR_VALUES[i] == pct) return i;
        }
        return 0;
    }

    private static int droneTierToIndex(int tier) {
        for (int i = 0; i < DRONE_TIERS.length; i++) {
            if (DRONE_TIERS[i] == tier) return i;
        }
        return 1; // default 20 (2.0X)
    }

    private static String droneTierToLabel(int tier) {
        int idx = droneTierToIndex(tier);
        if (idx >= 0 && idx < DRONE_LABELS.length) return DRONE_LABELS[idx];
        return "2.0X";
    }

    private static int fpsToIndex(int fps) {
        for (int i = 0; i < FPS_VALUES.length; i++) {
            if (FPS_VALUES[i] == fps) return i;
        }
        return 4; // default 165fps
    }

    private void setStatus(String msg) {
        UI.post(() -> { if (tvModStatus != null) tvModStatus.setText("STATUS: " + msg); });
    }

    private void updateFridaStatus(boolean running) {
        fridaRunning = running;
        if (tvFridaStatus != null) {
            tvFridaStatus.setText(running ? "● ONLINE" : "● OFFLINE");
            tvFridaStatus.setTextColor(running ? 0xFF44FF44 : 0xFFFF4444);
        }
        if (btnFridaToggle != null) {
            btnFridaToggle.setText(running ? "STOP" : "START");
        }
    }

    private void toast(String msg) {
        UI.post(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private String formatMult(float v) {
        return (v == Math.floor(v)) ? ((int) v) + "x" : v + "x";
    }

    private int multToIndex(float[] arr, float val) {
        for (int i = 0; i < arr.length; i++) {
            if (Math.abs(arr[i] - val) < 0.01f) return i;
        }
        return 0;
    }

    private interface FloatConsumer { void accept(float v); }

    private SeekBar.OnSeekBarChangeListener sliderListener(
            SeekBar sb, float[] values, TextView label, FloatConsumer onValue) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                float v = values[Math.min(p, values.length - 1)];
                label.setText(formatMult(v));
                onValue.accept(v);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        };
    }
}
