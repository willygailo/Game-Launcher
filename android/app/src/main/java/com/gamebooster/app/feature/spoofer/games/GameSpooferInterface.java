package com.gamebooster.app.feature.spoofer.games;

import android.content.Context;
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
     * Resets spoofing state.
     */
    void resetGameSpoof();

    /**
     * Gets the associated flagship spoof profile for this game strategy.
     */
    SpoofProfile getSpoofProfile();

    /**
     * Gets the strategy name.
     */
    String getStrategyName();
}
