package com.gamebooster.app.services;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.booster.RamZramChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.core.AppExecutors;

/**
 * QuickBoostTileService provides a 1-tap Android Quick Settings Tile
 * for instantly activating EXTREME Gaming Boost from the system shade.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickBoostTileService extends TileService {

    private static final String TAG = "QuickBoostTile";
    private boolean isBoostActive = false;

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) return;

        isBoostActive = !isBoostActive;

        if (isBoostActive) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setSubtitle("EXTREME Active ⚡");
            tile.updateTile();

            AppExecutors.getInstance().executeCommand(() -> {
                MaxHzForceChannel.forceApply(getApplicationContext(),
                        com.gamebooster.app.engine.DisplayOverrideController.highestSupportedRate(getApplicationContext()), null);
                PerformanceChannel.applyProfileWithResult(getApplicationContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                TouchLatencyChannel.enableUltraTouchResponse();
                RamZramChannel.trimMemoryAndCleanCache(getApplicationContext());

                AppExecutors.getInstance().postToMainThread(() -> {
                    Toast.makeText(getApplicationContext(), "⚡ 1-Tap EXTREME Boost Activated!", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setSubtitle("Tap to Boost");
            tile.updateTile();

            AppExecutors.getInstance().executeCommand(() -> {
                com.gamebooster.app.engine.DisplayOverrideController.restore(getApplicationContext());
                PerformanceChannel.applyProfileWithResult(getApplicationContext(), PerformanceChannel.Profile.BALANCED);
                AppExecutors.getInstance().postToMainThread(() -> {
                    Toast.makeText(getApplicationContext(), "Balanced Mode Reverted", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(isBoostActive ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel("Game Booster");
            tile.setSubtitle(isBoostActive ? "EXTREME Active ⚡" : "Tap to Boost");
            tile.updateTile();
        }
    }
}
