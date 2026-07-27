package com.gamespace.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gamespace.app.ui.GamesFragment;
import com.gamespace.app.ui.HomeFragment;
import com.gamespace.app.ui.HzFpsFragment;
import com.gamespace.app.ui.PermissionsFragment;
import com.gamespace.app.ui.ProfilesFragment;
import com.gamespace.app.utils.ShizukuExecutor;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Auto-grant permissions if Shizuku binder is active
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.grantAppPermissionsViaShizuku(this);
        }

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_games) {
                selectedFragment = new GamesFragment();
            } else if (itemId == R.id.nav_hz_fps) {
                selectedFragment = new HzFpsFragment();
            } else if (itemId == R.id.nav_profiles) {
                selectedFragment = new ProfilesFragment();
            } else if (itemId == R.id.nav_permissions) {
                selectedFragment = new PermissionsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
}
