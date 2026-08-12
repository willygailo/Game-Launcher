package com.gamebooster.app.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility holder for the former blanket Shizuku permission-grant flow.
 *
 * <p>Shizuku already runs the app's approved operations under the user-selected shell or root
 * identity. A game launcher must not use that access to grant its own package or unrelated games
 * broad permissions such as phone state, task control, overlays, or storage access.
 *
 * <p>Feature-specific operations are instead executed through the capability-gated controllers
 * and verified after the request. These compatibility methods intentionally return empty lists.
 */
public final class PermissionBatchBuilder {

    private PermissionBatchBuilder() { }

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------

    /** @deprecated Shizuku authorization replaces blanket {@code pm grant}/{@code appops} calls. */
    @Deprecated
    public static List<String> buildGrantBatch(String packageName) {
        return new ArrayList<>();
    }

    /** @deprecated Use {@link DisplayOverrideController#applyGameProfile} for one selected game. */
    @Deprecated
    public static List<String> buildPerGameBatch(String gamePkg, int targetHz) {
        return new ArrayList<>();
    }
}
