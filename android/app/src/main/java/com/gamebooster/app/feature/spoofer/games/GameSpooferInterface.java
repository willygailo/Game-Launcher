package com.gamebooster.app.feature.spoofer.games;

import android.content.Context;
import com.gamebooster.app.feature.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.feature.spoofer.SpoofProfile;

public interface GameSpooferInterface {
    /**
     * Applies the game-package-specific spoof profile.
     * @param context Application context.
     * @param packageName Target game package name.
     * @return true if spoof applied successfully.
     */
    boolean applyGameSpoof(Context context, String packageName);

    /**
     * Resets spoofing state globally.
     */
    void resetGameSpoof();

    /**
     * Resets spoofing and Game Mode state for the specific package.
     * @param context Application context.
     * @param packageName Target game package name.
     */
    default void resetGameSpoof(Context context, String packageName) {
        DeviceSpooferEngine.resetSpoofing(context, packageName);
    }

    /**
     * Gets the associated flagship spoof profile for this game strategy.
     */
    SpoofProfile getSpoofProfile();

    /**
     * Gets the strategy name.
     */
    String getStrategyName();
}

