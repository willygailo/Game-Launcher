package com.gamebooster.app.ui.layout;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gamebooster.app.R;
import com.gamebooster.app.functions.TweakManagerRepository;
import com.gamebooster.app.settings.PermissionsFragment;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity implements ShizukuManager.ShizukuStateListener {

    private static final String KEY_SELECTED_TAB = "SELECTED_TAB_INDEX";
    private int currentTabIndex = 0;

    private static final String[] TAB_TITLES = {
            "Home HUD",
            "Hz & FPS",
            "Tweaks",
            "Profiles",
            "Games",
            "Access"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register Shizuku binder lifecycle listeners and subscribe state change listener
        ShizukuManager.registerBinderListeners();
        ShizukuManager.addStateListener(this);

        // Initialize saved tweak states and restore active tweaks
        TweakManagerRepository.initializeStates(this);
        TweakManagerRepository.restoreAppliedTweaksAsync(this);

        // Auto-grant permissions if Shizuku binder is active
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.grantAppPermissionsViaShizuku(this);
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

    @Override
    public void onBinderStateChanged(boolean alive) {
        runOnUiThread(() -> {
            if (!alive) {
                Toast.makeText(this, "⚠️ Shizuku disconnected — reconnect to continue privileged tweaks", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "⚡ Shizuku connected cleanly", Toast.LENGTH_SHORT).show();
                TweakManagerRepository.restoreAppliedTweaksAsync(this);
            }
        });
    }

    private void showFragmentForTab(int position) {
        Fragment selectedFragment;
        switch (position) {
            case 0:
                selectedFragment = new HomeFragment();
                break;
            case 1:
                selectedFragment = new HzFpsFragment();
                break;
            case 2:
                selectedFragment = new TweaksFragment();
                break;
            case 3:
                selectedFragment = new ProfilesFragment();
                break;
            case 4:
                selectedFragment = new GamesFragment();
                break;
            case 5:
                selectedFragment = new PermissionsFragment();
                break;
            default:
                selectedFragment = new HomeFragment();
                break;
        }

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
