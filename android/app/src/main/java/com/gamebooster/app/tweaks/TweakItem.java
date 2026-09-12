package com.gamebooster.app.tweaks;
import com.gamebooster.app.config.*;

public class TweakItem {
    private final String id;
    private final String title;
    private final String description;
    private final String applyCommand;
    private final String revertCommand;
    private final TweakCategory category;
    private final boolean requiresShizuku;
    private boolean isApplied;

    public TweakItem(String id, String title, String description, String applyCommand,
                     String revertCommand, TweakCategory category, boolean requiresShizuku) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.applyCommand = applyCommand;
        this.revertCommand = revertCommand;
        this.category = category;
        this.requiresShizuku = requiresShizuku;
        this.isApplied = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getApplyCommand() {
        return applyCommand;
    }

    public String getRevertCommand() {
        return revertCommand;
    }

    public TweakCategory getCategory() {
        return category;
    }

    public boolean isRequiresShizuku() {
        return requiresShizuku;
    }

    public boolean isApplied() {
        return isApplied;
    }

    public void setApplied(boolean applied) {
        isApplied = applied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TweakItem tweakItem = (TweakItem) o;
        return id != null && id.equals(tweakItem.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TweakItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", isApplied=" + isApplied +
                '}';
    }
}
