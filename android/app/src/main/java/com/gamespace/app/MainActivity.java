package com.gamespace.app;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gamespace.app.ui.GamesFragment;
import com.gamespace.app.ui.HomeFragment;
import com.gamespace.app.ui.HzFpsFragment;
import com.gamespace.app.ui.PermissionsFragment;
import com.gamespace.app.ui.ProfilesFragment;
import com.gamespace.app.ui.TweaksFragment;
import com.gamespace.app.utils.ShizukuExecutor;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, currentTabIndex);
        Log.i("TabPersist", "saved index=" + currentTabIndex);
    }
}
