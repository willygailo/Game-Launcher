package com.gamebooster.app.feature.games.search;

import android.graphics.drawable.Drawable;

public class DiscoveredGameItem {

    public enum EngineType {
        UNREAL("UNREAL ENGINE", "#00FFCC"),
        UNITY("UNITY 3D", "#FF00FF"),
        GODOT("GODOT ENGINE", "#00AAFF"),
        COCOS("COCOS2D", "#FFAA00"),
        CUSTOM_VULKAN("NATIVE VULKAN", "#FF4444"),
        EMULATOR("EMULATOR", "#FFFF00"),
        STANDARD_GAME("MOBILE GAME", "#00FFCC");

        public final String label;
        public final String colorHex;

        EngineType(String label, String colorHex) {
            this.label = label;
            this.colorHex = colorHex;
        }
    }

    private final String packageName;
    private final String label;
    private final Drawable icon;
    private final EngineType engineType;
    private final String discoverySource;
    private boolean isAddedToLibrary;

    public DiscoveredGameItem(String packageName, String label, Drawable icon, EngineType engineType, String discoverySource) {
        this.packageName = packageName;
        this.label = label != null ? label : packageName;
        this.icon = icon;
        this.engineType = engineType != null ? engineType : EngineType.STANDARD_GAME;
        this.discoverySource = discoverySource != null ? discoverySource : "SYSTEM PM";
        this.isAddedToLibrary = false;
    }

    public String getPackageName() { return packageName; }
    public String getLabel() { return label; }
    public Drawable getIcon() { return icon; }
    public EngineType getEngineType() { return engineType; }
    public String getDiscoverySource() { return discoverySource; }
    public boolean isAddedToLibrary() { return isAddedToLibrary; }
    public void setAddedToLibrary(boolean added) { this.isAddedToLibrary = added; }
}
