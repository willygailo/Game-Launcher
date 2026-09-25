package com.gamebooster.app.ui.activities;
import com.gamebooster.app.config.*;

import android.content.Intent;
import java.io.File;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.gamebooster.app.ui.fragments.HomeFragment;
import com.gamebooster.app.ui.fragments.SettingsFragment;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.tweaks.TweakManagerRepository;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity implements ShizukuManager.ShizukuStateListener {

    private static final String KEY_SELECTED_TAB = "SELECTED_TAB_INDEX";
    private int currentTabIndex = 0;

    private static final String[] TAB_TITLES = {
            "HOME",
            "SETTINGS"
    };

    private static final String[] TAB_ICONS = {
            "🏠",
            "⚙️"
    };

    public static final int REQUEST_CODE_PICK_APK = 1003;

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
        
        // Edge-to-edge support for Android 13, 14, 15, 16
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Enforce maximum hardware refresh rate on MainActivity window
        com.gamebooster.app.device.HardwareDisplayController.applyMaxRefreshRateToWindow(this);
        // Fire all 6 command layers — never fall back to 60Hz
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() ->
                com.gamebooster.app.device.HardwareDisplayController.forceMaxHzNeverFallback(getApplicationContext()));

        // Phase 0.2: enable the config backup safety net (app-private storage)
        com.gamebooster.app.config.ConfigBackupManager.setAppContext(getApplicationContext());

        // Handle System Insets (Status bar, Camera Notch & Gesture Navigation Bar across all 4 edges)
        View rootLayout = findViewById(R.id.main_root_layout);
        View bottomNavWrapper = findViewById(R.id.bottom_nav_wrapper);
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                );

                // Apply status bar & camera notch insets so headers never get cut off
                if (fragmentContainer != null) {
                    fragmentContainer.setPadding(insets.left, insets.top, insets.right, 0);
                }

                // Adjust bottom navigation padding so gesture navigation bar never overlaps
                if (bottomNavWrapper != null) {
                    bottomNavWrapper.setPadding(
                            bottomNavWrapper.getPaddingLeft(),
                            bottomNavWrapper.getPaddingTop(),
                            bottomNavWrapper.getPaddingRight(),
                            Math.max(14, insets.bottom + 6)
                    );
                }
                return windowInsets;
            });
        }

        // Initialize Shizuku lifecycle manager and register listeners
        com.gamebooster.app.shizuku.ShizukuLifecycleManager.getInstance(getApplicationContext()).init();
        ShizukuManager.registerBinderListeners();
        ShizukuManager.addStateListener(this);

        // Request runtime notification permission
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        // Android 13-16 Predictive Back / OnBackPressed Handling
        // If on Settings tab (or other tab != 0), Back returns to Home (Tab 0) instead of abruptly exiting the app.
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTabIndex != 0) {
                    selectTab(0);
                } else {
                    finish();
                }
            }
        });

        // Asynchronous Cold-Start Engine Initialization (Zero Main-Thread Latency)
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().start();
                TweakManagerRepository.initializeStates(getApplicationContext());
                com.gamebooster.app.shizuku.RishManager.initialize(getApplicationContext());

                // Zero-Touch Auto-Connect: Fast multi-pulse binder scan + foreground permission dispatch
                com.gamebooster.app.shizuku.ShizukuAutoConnectEngine.pulseConnect(MainActivity.this);

                // Bind Shizuku AIDL UserService & Auto-Grant Privileges
                com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().bindService();
                if (ShizukuExecutor.hasShizukuPermission()) {
                    com.gamebooster.app.shizuku.ShizukuPermissionEnforcer.enforceAllPermissions(getApplicationContext());
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                    com.gamebooster.app.shizuku.ShizukuFileManager.grantAllStoragePermissions(getApplicationContext());

                    // Enforce Device & Hardware Spoofing across MLBB, CODM, PUBGM, and registered games
                    try {
                        com.gamebooster.app.spoofer.DeviceSpooferEngine.applyActiveProfileToAllGames(getApplicationContext());
                        com.gamebooster.app.spoofer.HardwareMaskEngine.maskAllInstalledApplications(getApplicationContext());
                    } catch (Throwable t) {
                        Log.w("MainActivity", "Startup device spoofing warning: " + t.getMessage());
                    }
                } else if (!ShizukuExecutor.isShizukuAvailable()) {
                    // Auto-Active: Shizuku not running — auto-prompt activation once per install
                    android.content.SharedPreferences prefs =
                            getSharedPreferences("game_booster_prefs", MODE_PRIVATE);
                    if (!prefs.getBoolean("shizuku_auto_prompt_shown", false)) {
                        prefs.edit().putBoolean("shizuku_auto_prompt_shown", true).apply();
                        if (rootLayout != null) {
                            rootLayout.post(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    ShizukuManager.showShizukuPermissionDialog(MainActivity.this, "Auto-Active Engine");
                                }
                            });
                        }
                    }
                }
            } catch (Throwable t) {
                Log.w("MainActivity", "Background engine warm-up warning: " + t.getMessage());
            }
        });

        if (savedInstanceState != null) {
            currentTabIndex = savedInstanceState.getInt(KEY_SELECTED_TAB, 0);
            Log.i("TabPersist", "restored index=" + currentTabIndex);
        } else {
            Log.i("TabPersist", "fresh start index=0");
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.removeAllTabs();

        for (int i = 0; i < TAB_TITLES.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            View customView = LayoutInflater.from(this).inflate(R.layout.item_custom_tab, tabLayout, false);
            TextView tvIcon = customView.findViewById(R.id.tab_icon);
            TextView tvLabel = customView.findViewById(R.id.tab_label);
            tvIcon.setText(TAB_ICONS[i]);
            tvLabel.setText(TAB_TITLES[i]);
            tab.setCustomView(customView);
            tabLayout.addTab(tab);
        }

        updateTabStyles(tabLayout, currentTabIndex);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabIndex = tab.getPosition();
                updateTabStyles(tabLayout, currentTabIndex);
                showFragmentForTab(currentTabIndex);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        TabLayout.Tab initialTab = tabLayout.getTabAt(currentTabIndex);
        if (initialTab != null) {
            if (tabLayout.getSelectedTabPosition() != currentTabIndex) {
                initialTab.select();
            } else {
                showFragmentForTab(currentTabIndex);
            }
        }

        checkAndShowPostGameReport(getIntent());
    }

    private void updateTabStyles(TabLayout tabLayout, int selectedIndex) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                View customView = tab.getCustomView();
                TextView tvLabel = customView.findViewById(R.id.tab_label);
                boolean isSelected = (i == selectedIndex);
                customView.setSelected(isSelected);
                if (tvLabel != null) {
                    tvLabel.setTextColor(isSelected ? Color.parseColor("#00F0FF") : Color.parseColor("#94A3B8"));
                    tvLabel.setTextSize(isSelected ? 13f : 12f);
                }
            }
        }
    }

    private boolean lastReportedAlive = false;

    @Override
    public void onBinderStateChanged(boolean alive) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            boolean reallyAlive = alive || com.gamebooster.app.engine.PrivilegeBridgeEngine.isShizukuVirtualRootReady();
            if (!reallyAlive) {
                lastReportedAlive = false;
                Log.d("MainActivity", "Shizuku disconnected");
            } else {
                if (!lastReportedAlive) {
                    lastReportedAlive = true;
                    Toast.makeText(getApplicationContext(), "⚡ Shizuku API Connected — Full Access Active!", Toast.LENGTH_SHORT).show();
                    ShizukuManager.triggerThrottledPostConnectionSync();
                }
            }
            com.gamebooster.app.shizuku.ShizukuLifecycleManager.getInstance(getApplicationContext()).refreshStatus();
        });
    }

    private static final String TAG_HOME = "tab_home";
    private static final String TAG_SETTINGS = "tab_settings";

    private void showFragmentForTab(int position) {
        if (isFinishing() || isDestroyed()) return;
        try {
            androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
            androidx.fragment.app.FragmentTransaction transaction = fm.beginTransaction();

            Fragment homeFrag = fm.findFragmentByTag(TAG_HOME);
            Fragment settingsFrag = fm.findFragmentByTag(TAG_SETTINGS);

            // Clean up any legacy stats fragment if still attached
            Fragment legacyStatsFrag = fm.findFragmentByTag("tab_stats");
            if (legacyStatsFrag != null && legacyStatsFrag.isAdded()) {
                transaction.remove(legacyStatsFrag);
            }

            if (position == 0) {
                if (settingsFrag != null && settingsFrag.isAdded()) transaction.hide(settingsFrag);
                if (homeFrag == null) {
                    homeFrag = new HomeFragment();
                    transaction.add(R.id.fragment_container, homeFrag, TAG_HOME);
                } else {
                    transaction.show(homeFrag);
                }
            } else if (position == 1) {
                if (homeFrag != null && homeFrag.isAdded()) transaction.hide(homeFrag);
                if (settingsFrag == null) {
                    settingsFrag = new SettingsFragment();
                    transaction.add(R.id.fragment_container, settingsFrag, TAG_SETTINGS);
                } else {
                    transaction.show(settingsFrag);
                }
            }
            transaction.commitAllowingStateLoss();
        } catch (Throwable t) {
            Log.e("MainActivity", "Error switching tab fragment", t);
        }
    }

    public void reloadHomeGames() {
        try {
            Fragment homeFrag = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
            if (homeFrag instanceof HomeFragment && homeFrag.isAdded()) {
                ((HomeFragment) homeFrag).loadAndScanGames(true);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == com.gamebooster.app.saf.SafStorageManager.REQUEST_CODE_SAF_TREE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                android.net.Uri treeUri = data.getData();
                boolean persisted = com.gamebooster.app.saf.SafStorageManager.saveAndPersistUri(this, "com.mobile.legends", treeUri);
                if (persisted) {
                    Toast.makeText(this, "📂 Storage Access Granted! Permanent SAF Active.", Toast.LENGTH_LONG).show();
                    Fragment settingsFrag = getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);
                    if (settingsFrag instanceof SettingsFragment && settingsFrag.isAdded()) {
                        ((SettingsFragment) settingsFrag).refreshSafStatus();
                    }
                } else {
                    Toast.makeText(this, "⚠️ Failed to persist storage permission", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Storage access was not selected", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_PICK_APK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            android.net.Uri apkUri = data.getData();
            AppExecutors.getInstance().executeCommand(() -> {
                try {
                    File cacheApk = new File(getCacheDir(), "picked_install.apk");
                    try (java.io.InputStream in = getContentResolver().openInputStream(apkUri);
                         java.io.FileOutputStream out = new java.io.FileOutputStream(cacheApk)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        Toast.makeText(this, "⚡ Installing selected APK package...", Toast.LENGTH_SHORT).show();
                        com.gamebooster.app.apk.ApkInstallerEngine.installApk(this, cacheApk.getAbsolutePath(), (success, message) -> {
                            if (success) {
                                reloadHomeGames();
                            }
                        });
                    });
                } catch (Throwable t) {
                    AppExecutors.getInstance().postToMainThread(() -> {
                        Toast.makeText(this, "Failed to read APK file: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        com.gamebooster.app.shizuku.ShizukuLifecycleManager.getInstance(getApplicationContext()).onResumeCheck();
        checkAndShowPostGameReport(intent);
    }

    private void checkAndShowPostGameReport(Intent intent) {
        if (intent != null && intent.hasExtra("EXTRA_SHOW_POST_GAME_REPORT")) {
            com.gamebooster.app.overlay.GameSessionReport report =
                    intent.getSerializableExtra("EXTRA_SHOW_POST_GAME_REPORT", com.gamebooster.app.overlay.GameSessionReport.class);
            if (report != null) {
                com.gamebooster.app.ui.dialogs.PostGameReportDialog.show(this, report);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.gamebooster.app.device.HardwareDisplayController.applyMaxRefreshRateToWindow(this);
        // Re-enforce on every resume — OS adaptive VRR resets can silently drop to 60Hz
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() ->
                com.gamebooster.app.device.HardwareDisplayController.forceMaxHzNeverFallback(getApplicationContext()));
        com.gamebooster.app.shizuku.ShizukuAutoConnectEngine.pulseConnect(this);
        com.gamebooster.app.shizuku.ShizukuLifecycleManager.getInstance(getApplicationContext()).onResumeCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        com.gamebooster.app.shizuku.ShizukuAutoConnectEngine.clearForegroundActivity(this);
        ShizukuManager.removeStateListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, currentTabIndex);
        Log.i("TabPersist", "saved index=" + currentTabIndex);
    }
}
