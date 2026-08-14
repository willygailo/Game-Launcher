package com.gamebooster.app.app;

import com.gamebooster.app.feature.gameprofiles.automation.GameProfileAutoConfigurator;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.feature.performance.tweaks.TweakManagerRepository;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuManager;
import com.gamebooster.app.feature.home.ui.HomeFragment;
import com.gamebooster.app.feature.onboarding.ui.OnboardingActivity;
import com.gamebooster.app.feature.settings.ui.SettingsFragment;

public class MainActivity extends AppCompatActivity implements ShizukuManager.ShizukuStateListener {

    private static final String KEY_SELECTED_TAB = "SELECTED_TAB_INDEX";
    private int currentTabIndex = 0;

    private LinearLayout navBtnHome;
    private ImageView navIconHome;
    private TextView navTextHome;

    private LinearLayout navBtnSettings;
    private ImageView navIconSettings;
    private TextView navTextSettings;

    private HomeFragment mHomeFragment;
    private SettingsFragment mSettingsFragment;
    private Fragment mActiveFragment;

    public void selectTab(int position) {
        if (position != 0 && position != 1) position = 0;
        currentTabIndex = position;
        updateNavButtonStates(currentTabIndex);
        showFragmentForTab(currentTabIndex);
    }

    private void updateNavButtonStates(int position) {
        if (navBtnHome == null || navBtnSettings == null) return;

        if (position == 0) {
            // Home is Active
            navBtnHome.setBackgroundResource(R.drawable.bg_nav_button_active);
            if (navIconHome != null) navIconHome.setColorFilter(Color.parseColor("#00F0FF"));
            if (navTextHome != null) {
                navTextHome.setTextColor(Color.parseColor("#00F0FF"));
                navTextHome.setTypeface(null, Typeface.BOLD);
            }
            navBtnHome.animate().scaleX(1.02f).scaleY(1.02f).setDuration(150).start();

            // Settings is Inactive
            navBtnSettings.setBackgroundResource(R.drawable.bg_nav_button_inactive);
            if (navIconSettings != null) navIconSettings.setColorFilter(Color.parseColor("#8094A3B8"));
            if (navTextSettings != null) {
                navTextSettings.setTextColor(Color.parseColor("#8094A3B8"));
                navTextSettings.setTypeface(null, Typeface.NORMAL);
            }
            navBtnSettings.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
        } else {
            // Settings is Active
            navBtnSettings.setBackgroundResource(R.drawable.bg_nav_button_active);
            if (navIconSettings != null) navIconSettings.setColorFilter(Color.parseColor("#00F0FF"));
            if (navTextSettings != null) {
                navTextSettings.setTextColor(Color.parseColor("#00F0FF"));
                navTextSettings.setTypeface(null, Typeface.BOLD);
            }
            navBtnSettings.animate().scaleX(1.02f).scaleY(1.02f).setDuration(150).start();

            // Home is Inactive
            navBtnHome.setBackgroundResource(R.drawable.bg_nav_button_inactive);
            if (navIconHome != null) navIconHome.setColorFilter(Color.parseColor("#8094A3B8"));
            if (navTextHome != null) {
                navTextHome.setTextColor(Color.parseColor("#8094A3B8"));
                navTextHome.setTypeface(null, Typeface.NORMAL);
            }
            navBtnHome.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!OnboardingActivity.isOnboardingCompleted(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        View rootLayout = findViewById(R.id.main_root_layout);
        View bottomNavContainer = findViewById(R.id.bottom_nav_container);

        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                if (bottomNavContainer != null) {
                    bottomNavContainer.setPadding(
                            bottomNavContainer.getPaddingLeft(),
                            bottomNavContainer.getPaddingTop(),
                            bottomNavContainer.getPaddingRight(),
                            systemBars.bottom + 12
                    );
                }
                return insets;
            });
        }

        // Initialize Navigation Views
        navBtnHome = findViewById(R.id.nav_btn_home);
        navIconHome = findViewById(R.id.nav_icon_home);
        navTextHome = findViewById(R.id.nav_text_home);

        navBtnSettings = findViewById(R.id.nav_btn_settings);
        navIconSettings = findViewById(R.id.nav_icon_settings);
        navTextSettings = findViewById(R.id.nav_text_settings);

        if (navBtnHome != null) {
            navBtnHome.setOnClickListener(v -> selectTab(0));
        }

        if (navBtnSettings != null) {
            navBtnSettings.setOnClickListener(v -> selectTab(1));
        }

        // Register Shizuku binder lifecycle listeners and subscribe state change listener
        ShizukuManager.registerBinderListeners();
        ShizukuManager.addStateListener(this);
        ShizukuManager.attemptAutoStartShizuku(getApplicationContext());

        // Initialize saved tweak states and restore active tweaks
        TweakManagerRepository.initializeStates(this);
        TweakManagerRepository.restoreAppliedTweaksAsync(this);

        // Auto-start background game detection service on app startup
        com.gamebooster.app.feature.games.space.AutoGameMonitorService.start(getApplicationContext());

        // Auto-configure game profiles & patches for all installed games on app startup
        GameProfileAutoConfigurator.autoConfigAllInstalledGamesAsync(getApplicationContext(), (count, hz) -> {
            Log.i("MainActivity", "Auto-configured " + count + " games @ " + hz + "Hz on app startup");
        });

        // Request runtime notification permission on Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Bind Shizuku UserService
        try {
            com.gamebooster.app.platform.shizuku.ShizukuUserServiceConnector.getInstance().bindService();
            if (ShizukuExecutor.isShizukuAvailable() && !ShizukuExecutor.hasShizukuPermission()) {
                ShizukuManager.requestShizukuPermission();
            } else if (ShizukuExecutor.hasShizukuPermission()) {
                AppExecutors.getInstance().executeCommand(() -> {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                    com.gamebooster.app.feature.performance.display.RefreshRateOverrideEngine.restorePersistedRefreshRate(getApplicationContext());
                    if (com.gamebooster.app.feature.spoofer.SpoofPreferences.isSpoofEnabled(getApplicationContext())) {
                        String activeId = com.gamebooster.app.feature.spoofer.SpoofPreferences.getActiveProfileId(getApplicationContext());
                        if (activeId != null) {
                            com.gamebooster.app.feature.spoofer.SpoofProfile activeProfile = com.gamebooster.app.feature.spoofer.DeviceSpooferEngine.getProfileById(activeId);
                            if (activeProfile != null) {
                                com.gamebooster.app.feature.spoofer.DeviceSpooferEngine.applyProfile(getApplicationContext(), activeProfile, null);
                            }
                        }
                    }
                });
            }
        } catch (Throwable ignored) {}

        if (savedInstanceState != null) {
            currentTabIndex = savedInstanceState.getInt(KEY_SELECTED_TAB, 0);
            Log.i("TabPersist", "restored index=" + currentTabIndex);
        } else {
            Log.i("TabPersist", "fresh start index=0");
        }

        updateNavButtonStates(currentTabIndex);
        showFragmentForTab(currentTabIndex);
    }

    @Override
    public void onBinderStateChanged(boolean alive) {
        runOnUiThread(() -> {
            if (!alive) {
                Toast.makeText(this, "⚠️ Shizuku disconnected — reconnect to continue privileged tweaks", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "⚡ Shizuku connected — verifying the service…", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                    com.gamebooster.app.feature.performance.display.RefreshRateOverrideEngine.restorePersistedRefreshRate(getApplicationContext());
                    TweakManagerRepository.restoreAppliedTweaksAsync(getApplicationContext());
                    AppExecutors.getInstance().postToMainThread(() ->
                            Toast.makeText(getApplicationContext(), "⚡ Shizuku service ready for supported requests.", Toast.LENGTH_SHORT).show());
                });
            }
        });
    }

    private void clearBackStackIfAny() {
        androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void showFragmentForTab(int position) {
        clearBackStackIfAny();

        androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction ft = fm.beginTransaction();

        if (mHomeFragment == null) {
            mHomeFragment = (HomeFragment) fm.findFragmentByTag("TAG_HOME");
            if (mHomeFragment == null) {
                mHomeFragment = new HomeFragment();
                ft.add(R.id.fragment_container, mHomeFragment, "TAG_HOME");
            }
        }

        if (mSettingsFragment == null) {
            mSettingsFragment = (SettingsFragment) fm.findFragmentByTag("TAG_SETTINGS");
            if (mSettingsFragment == null) {
                mSettingsFragment = new SettingsFragment();
                ft.add(R.id.fragment_container, mSettingsFragment, "TAG_SETTINGS");
                ft.hide(mSettingsFragment);
            }
        }

        Fragment targetFragment = (position == 1) ? mSettingsFragment : mHomeFragment;

        if (mActiveFragment != targetFragment) {
            if (mActiveFragment != null) {
                ft.hide(mActiveFragment);
            }
            ft.show(targetFragment);
            mActiveFragment = targetFragment;
            ft.commitNow();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ShizukuManager.isShizukuConnectedAndGranted()) {
            com.gamebooster.app.platform.shizuku.ShizukuHealthMonitor.getInstance().forceCheck();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShizukuManager.removeStateListener(this);
        ShizukuManager.unregisterBinderListeners();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, currentTabIndex);
        Log.i("TabPersist", "saved index=" + currentTabIndex);
    }
}
