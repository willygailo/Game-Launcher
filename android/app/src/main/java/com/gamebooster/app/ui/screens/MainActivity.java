package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

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

        // Register Shizuku binder lifecycle listeners and subscribe state change listener
        ShizukuManager.registerBinderListeners();
        ShizukuManager.addStateListener(this);

        // Initialize saved tweak states and restore active tweaks
        TweakManagerRepository.initializeStates(this);
        TweakManagerRepository.restoreAppliedTweaksAsync(this);

        // Request runtime notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Initialize RishManager 13.5 Shell Engine in background
        AppExecutors.getInstance().executeCommand(() -> {
            com.gamebooster.app.shizuku.RishManager.initialize(getApplicationContext());
        });

        // Bind Shizuku AIDL UserService & Auto-Grant Privileges
        try {
            com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().bindService();
            if (ShizukuExecutor.isShizukuAvailable() && !ShizukuExecutor.hasShizukuPermission()) {
                ShizukuManager.requestShizukuPermission();
            } else if (ShizukuExecutor.hasShizukuPermission()) {
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.shizuku.ShizukuPermissionEnforcer.enforceAllPermissions(getApplicationContext());
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                    com.gamebooster.app.shizuku.ShizukuFileManager.grantAllStoragePermissions(getApplicationContext());
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
                if (savedInstanceState == null) {
                    showFragmentForTab(currentTabIndex);
                }
            }
        }
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

    @Override
    public void onBinderStateChanged(boolean alive) {
        runOnUiThread(() -> {
            if (!alive) {
                Toast.makeText(this, "⚠️ Shizuku disconnected — reconnect to continue privileged tweaks", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "⚡ Shizuku Connected — Auto-Granting All System & Storage Privileges...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.shizuku.ShizukuPermissionEnforcer.enforceAllPermissions(getApplicationContext());
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                    TweakManagerRepository.restoreAppliedTweaksAsync(getApplicationContext());
                    AppExecutors.getInstance().postToMainThread(() ->
                            Toast.makeText(getApplicationContext(), "⚡ All Storage & System Permissions Active!", Toast.LENGTH_SHORT).show());
                });
            }
        });
    }

    private void showFragmentForTab(int position) {
        Fragment selectedFragment = (position == 1) ? new SettingsFragment() : new HomeFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
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
