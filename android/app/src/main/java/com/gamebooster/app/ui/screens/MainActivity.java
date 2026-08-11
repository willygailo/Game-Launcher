package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.tweaks.TweakManagerRepository;
import com.gamebooster.app.ui.screens.PermissionsFragment;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity implements ShizukuManager.ShizukuStateListener {

    private static final String KEY_SELECTED_TAB = "SELECTED_TAB_INDEX";
    private int currentTabIndex = 0;

    private static final String[] TAB_TITLES = {
            "Home",
            "Settings"
    };

    public void selectTab(int position) {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        if (tabLayout != null && position >= 0 && position < TAB_TITLES.length) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                tab.select();
            }
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

        // Register Shizuku binder lifecycle listeners and subscribe state change listener
        ShizukuManager.registerBinderListeners();
        ShizukuManager.addStateListener(this);
        ShizukuManager.attemptAutoStartShizuku(getApplicationContext());

        // Initialize saved tweak states and restore active tweaks
        TweakManagerRepository.initializeStates(this);
        TweakManagerRepository.restoreAppliedTweaksAsync(this);

        // Auto-start background game detection service on app startup
        com.gamebooster.app.gamespace.AutoGameMonitorService.start(getApplicationContext());

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

        // Bind Shizuku AIDL UserService & Auto-Grant Privileges
        try {
            com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().bindService();
            if (ShizukuExecutor.isShizukuAvailable() && !ShizukuExecutor.hasShizukuPermission()) {
                ShizukuManager.requestShizukuPermission();
            } else if (ShizukuExecutor.hasShizukuPermission()) {
                AppExecutors.getInstance().executeCommand(() -> {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                    com.gamebooster.app.engine.RefreshRateOverrideEngine.restorePersistedRefreshRate(getApplicationContext());
                    if (com.gamebooster.app.spoofer.SpoofPreferences.isSpoofEnabled(getApplicationContext())) {
                        String activeId = com.gamebooster.app.spoofer.SpoofPreferences.getActiveProfileId(getApplicationContext());
                        if (activeId != null) {
                            com.gamebooster.app.spoofer.SpoofProfile activeProfile = com.gamebooster.app.spoofer.DeviceSpooferEngine.getProfileById(activeId);
                            if (activeProfile != null) {
                                com.gamebooster.app.spoofer.DeviceSpooferEngine.applyProfile(getApplicationContext(), activeProfile, null);
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

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        for (String title : TAB_TITLES) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabIndex = tab.getPosition();
                showFragmentForTab(currentTabIndex);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                clearBackStackIfAny();
            }
        });

        TabLayout.Tab initialTab = tabLayout.getTabAt(currentTabIndex);
        if (initialTab != null) {
            if (tabLayout.getSelectedTabPosition() != currentTabIndex) {
                initialTab.select();
            } else {
                if (savedInstanceState == null) {
                    showFragmentForTab(currentTabIndex);
                }
            }
        }
    }

    @Override
    public void onBinderStateChanged(boolean alive) {
        runOnUiThread(() -> {
            if (!alive) {
                Toast.makeText(this, "⚠️ Shizuku disconnected — reconnect to continue privileged tweaks", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "⚡ Shizuku Connected — Auto-Granting All System Permissions...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                    com.gamebooster.app.engine.RefreshRateOverrideEngine.restorePersistedRefreshRate(getApplicationContext());
                    TweakManagerRepository.restoreAppliedTweaksAsync(getApplicationContext());
                    AppExecutors.getInstance().postToMainThread(() ->
                            Toast.makeText(getApplicationContext(), "⚡ All System Permissions Auto-Configured!", Toast.LENGTH_SHORT).show());
                });
            }
        });
    }

    private HomeFragment mHomeFragment;
    private SettingsFragment mSettingsFragment;
    private Fragment mActiveFragment;

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

        // Retrieve existing instances if restoring from state
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
            com.gamebooster.app.shizuku.ShizukuHealthMonitor.getInstance().forceCheck();
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
