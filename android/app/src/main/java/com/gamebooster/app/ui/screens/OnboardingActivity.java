package com.gamebooster.app.ui.screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;

/**
 * OnboardingActivity — First-time registration & setup wizard for Game Launcher Pro V2.0.
 *
 * Directs users through developer identity verification, Shizuku ADB binding,
 * WRITE_SETTINGS, and Overlay permissions.
 */
public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "game_launcher_prefs";
    public static final String KEY_ONBOARDING_COMPLETE = "onboarding_completed";

    private TextView tvStepTitle;
    private TextView tvStepDescription;
    private TextView tvStatusBadge;
    private Button btnPrimaryAction;
    private Button btnSecondaryAction;

    private int currentStep = 0;

    public static boolean isOnboardingCompleted(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    public static void setOnboardingCompleted(Context context, boolean completed) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_ONBOARDING_COMPLETE, completed).apply();
    }

    private final ShizukuManager.ShizukuStateListener shizukuStateListener = alive -> {
        runOnUiThread(this::updateStepUi);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isOnboardingCompleted(this)) {
            launchMainActivity();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        ShizukuManager.registerBinderListeners();
        ShizukuManager.addStateListener(shizukuStateListener);

        tvStepTitle = findViewById(R.id.tv_onboarding_step_title);
        tvStepDescription = findViewById(R.id.tv_onboarding_step_desc);
        tvStatusBadge = findViewById(R.id.tv_onboarding_status_badge);
        btnPrimaryAction = findViewById(R.id.btn_onboarding_primary);
        btnSecondaryAction = findViewById(R.id.btn_onboarding_secondary);

        updateStepUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStepUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShizukuManager.removeStateListener(shizukuStateListener);
    }

    private void updateStepUi() {
        switch (currentStep) {
            case 0:
                tvStepTitle.setText("🎮 Welcome to Game Launcher Pro V2.0");
                tvStepDescription.setText("Engineered by WILLY JR CARNASA GAILO.\n\n" +
                        "Unlocks 0ms Touch Delay, 1000Hz Digitizer Sampling, Per-Game Hardware Identity Spoofer, and 165Hz Game Patcher for Mobile eSports.\n\n" +
                        "Let's configure system permissions for zero-latency gameplay!");
                tvStatusBadge.setText("STEP 1 / 4");
                btnPrimaryAction.setText("BEGIN SETUP 🚀");
                btnSecondaryAction.setVisibility(android.view.View.GONE);

                btnPrimaryAction.setOnClickListener(v -> {
                    currentStep = 1;
                    updateStepUi();
                });
                break;

            case 1:
                boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
                tvStepTitle.setText("⚡ Step 1: Shizuku Service Privilege");
                tvStepDescription.setText("Game Launcher Pro V2.0 uses Shizuku ADB Binder IPC (uid 2000) to safely apply 165Hz display locks and device spoofer profiles without root.\n\n" +
                        "Please launch Shizuku and grant permission to Game Launcher Pro V2.0.");
                tvStatusBadge.setText(hasShizuku ? "✅ SHIZUKU CONNECTED" : "⚠️ SHIZUKU REQUIRED");
                tvStatusBadge.setTextColor(hasShizuku ? 0xFF00FF66 : 0xFFFFB800);
                btnPrimaryAction.setText(hasShizuku ? "NEXT STEP ➔" : "GRANT SHIZUKU PERMISSION 🔑");
                btnSecondaryAction.setVisibility(android.view.View.VISIBLE);
                btnSecondaryAction.setText("SKIP / TRY WITHOUT SHIZUKU");

                btnPrimaryAction.setOnClickListener(v -> {
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        Toast.makeText(this, "⚡ Connecting the Shizuku service...", Toast.LENGTH_SHORT).show();
                        btnPrimaryAction.setEnabled(false);
                        AppExecutors.getInstance().executeCommand(() -> {
                            ShizukuExecutor.GrantResult grantResult = ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                            AppExecutors.getInstance().postToMainThread(() -> {
                                btnPrimaryAction.setEnabled(true);
                                if (grantResult.success) {
                                    Toast.makeText(this, "✅ Shizuku connected. Native display and supported Game Mode requests are ready.", Toast.LENGTH_LONG).show();
                                }
                                currentStep = 2;
                                updateStepUi();
                            });
                        });
                    } else {
                        if (ShizukuExecutor.isShizukuAvailable()) {
                            ShizukuManager.requestShizukuPermission();
                        } else {
                            ShizukuManager.openOrInstallShizukuManager(this);
                        }
                    }
                });

                btnSecondaryAction.setOnClickListener(v -> {
                    currentStep = 2;
                    updateStepUi();
                });
                break;

            case 2:
                boolean canWriteSettings = Settings.System.canWrite(this);
                tvStepTitle.setText("⚙️ Step 2: Write System Settings");
                tvStepDescription.setText("Required to override device refresh rates (60Hz / 90Hz / 120Hz / 144Hz / 165Hz) and system performance profiles.");
                tvStatusBadge.setText(canWriteSettings ? "✅ WRITE_SETTINGS GRANTED" : "⚠️ PERMISSION NEEDED");
                tvStatusBadge.setTextColor(canWriteSettings ? 0xFF00FF66 : 0xFFFFB800);
                btnPrimaryAction.setText(canWriteSettings ? "NEXT STEP ➔" : "OPEN SYSTEM SETTINGS ⚙️");
                btnSecondaryAction.setVisibility(android.view.View.VISIBLE);
                btnSecondaryAction.setText("SKIP");

                btnPrimaryAction.setOnClickListener(v -> {
                    if (Settings.System.canWrite(this)) {
                        currentStep = 3;
                        updateStepUi();
                    } else {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Unable to open write settings screen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnSecondaryAction.setOnClickListener(v -> {
                    currentStep = 3;
                    updateStepUi();
                });
                break;

            case 3:
                boolean canDrawOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
                tvStepTitle.setText("🎯 Step 3: Performance HUD & Crosshair Overlay");
                tvStepDescription.setText("Allows rendering real-time Floating FPS/RAM HUD overlay and tactical aim reticle over eSports titles.");
                tvStatusBadge.setText(canDrawOverlay ? "✅ OVERLAY GRANTED" : "⚠️ OVERLAY PERMISSION NEEDED");
                tvStatusBadge.setTextColor(canDrawOverlay ? 0xFF00FF66 : 0xFFFFB800);
                btnPrimaryAction.setText("FINISH & LAUNCH 🎮");
                btnSecondaryAction.setVisibility(android.view.View.VISIBLE);
                btnSecondaryAction.setText("GRANT OVERLAY PERMISSION");

                btnPrimaryAction.setOnClickListener(v -> {
                    setOnboardingCompleted(this, true);
                    launchMainActivity();
                });

                btnSecondaryAction.setOnClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Overlay permission already granted!", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
