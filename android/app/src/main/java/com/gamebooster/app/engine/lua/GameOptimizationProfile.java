package com.gamebooster.app.engine.lua;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * GameOptimizationProfile — Parsed configuration profile for a target game.
 */
public final class GameOptimizationProfile {

    private final String gameId;
    private final String packageName;
    private final int targetFps;
    private final String graphicsTier;
    private final boolean forceVulkan;
    private final int touchBoostHz;
    private final String cpuGovernor;
    private final Map<String, String> rawProperties;

    public GameOptimizationProfile(
            String gameId,
            String packageName,
            int targetFps,
            String graphicsTier,
            boolean forceVulkan,
            int touchBoostHz,
            String cpuGovernor,
            Map<String, String> rawProperties) {
        this.gameId = gameId != null ? gameId : "";
        this.packageName = packageName != null ? packageName : "";
        this.targetFps = targetFps;
        this.graphicsTier = graphicsTier != null ? graphicsTier : "ULTRA";
        this.forceVulkan = forceVulkan;
        this.touchBoostHz = touchBoostHz;
        this.cpuGovernor = cpuGovernor != null ? cpuGovernor : "performance";
        this.rawProperties = rawProperties != null ? Collections.unmodifiableMap(rawProperties) : Collections.emptyMap();
    }

    public String getGameId() {
        return gameId;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getTargetFps() {
        return targetFps;
    }

    public String getGraphicsTier() {
        return graphicsTier;
    }

    public boolean isForceVulkan() {
        return forceVulkan;
    }

    public boolean getForceVulkan() {
        return forceVulkan;
    }

    public int getTouchBoostHz() {
        return touchBoostHz;
    }

    public String getCpuGovernor() {
        return cpuGovernor;
    }

    public Map<String, String> getRawProperties() {
        return rawProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameOptimizationProfile)) return false;
        GameOptimizationProfile that = (GameOptimizationProfile) o;
        return targetFps == that.targetFps &&
                forceVulkan == that.forceVulkan &&
                touchBoostHz == that.touchBoostHz &&
                Objects.equals(gameId, that.gameId) &&
                Objects.equals(packageName, that.packageName) &&
                Objects.equals(graphicsTier, that.graphicsTier) &&
                Objects.equals(cpuGovernor, that.cpuGovernor) &&
                Objects.equals(rawProperties, that.rawProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, packageName, targetFps, graphicsTier, forceVulkan, touchBoostHz, cpuGovernor, rawProperties);
    }

    @Override
    public String toString() {
        return "GameOptimizationProfile{" +
                "gameId='" + gameId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", targetFps=" + targetFps +
                ", graphicsTier='" + graphicsTier + '\'' +
                ", forceVulkan=" + forceVulkan +
                ", touchBoostHz=" + touchBoostHz +
                ", cpuGovernor='" + cpuGovernor + '\'' +
                ", rawProperties=" + rawProperties.size() +
                '}';
    }
}
