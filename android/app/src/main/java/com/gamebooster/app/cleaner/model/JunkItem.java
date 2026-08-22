package com.gamebooster.app.cleaner.model;

import java.io.File;
import java.util.Objects;

public class JunkItem {

    private final String path;
    private final String displayName;
    private final String packageName;
    private final long sizeBytes;
    private final JunkCategory category;
    private final boolean isDirectory;
    private boolean selected;

    public JunkItem(String path, String displayName, long sizeBytes, JunkCategory category, boolean isDirectory) {
        this(path, displayName, null, sizeBytes, category, isDirectory);
    }

    public JunkItem(String path, String displayName, String packageName, long sizeBytes, JunkCategory category, boolean isDirectory) {
        this.path = path;
        this.displayName = displayName != null ? displayName : new File(path).getName();
        this.packageName = packageName;
        this.sizeBytes = sizeBytes;
        this.category = category;
        this.isDirectory = isDirectory;
        this.selected = category != null && category.isDefaultSelected();
    }

    public String getPath() {
        return path;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public JunkCategory getCategory() {
        return category;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JunkItem junkItem = (JunkItem) o;
        return Objects.equals(path, junkItem.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
