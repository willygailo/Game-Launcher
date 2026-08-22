package com.gamebooster.app.cleaner.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class JunkScanResult {

    private final List<JunkItem> items = new ArrayList<>();
    private final Map<JunkCategory, Long> categorySizes = new EnumMap<>(JunkCategory.class);
    private final Map<JunkCategory, Integer> categoryCounts = new EnumMap<>(JunkCategory.class);
    private long totalBytes = 0;
    private long scanDurationMs = 0;

    public JunkScanResult() {
        for (JunkCategory category : JunkCategory.values()) {
            categorySizes.put(category, 0L);
            categoryCounts.put(category, 0);
        }
    }

    public synchronized void addItem(JunkItem item) {
        if (item == null) return;
        items.add(item);
        totalBytes += item.getSizeBytes();

        JunkCategory category = item.getCategory();
        if (category != null) {
            long currentSize = categorySizes.getOrDefault(category, 0L);
            categorySizes.put(category, currentSize + item.getSizeBytes());

            int currentCount = categoryCounts.getOrDefault(category, 0);
            categoryCounts.put(category, currentCount + 1);
        }
    }

    public synchronized List<JunkItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized List<JunkItem> getItemsForCategory(JunkCategory category) {
        List<JunkItem> filtered = new ArrayList<>();
        for (JunkItem item : items) {
            if (item.getCategory() == category) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public synchronized long getTotalBytes() {
        return totalBytes;
    }

    public synchronized long getSelectedBytes() {
        long selectedBytes = 0;
        for (JunkItem item : items) {
            if (item.isSelected()) {
                selectedBytes += item.getSizeBytes();
            }
        }
        return selectedBytes;
    }

    public synchronized int getSelectedCount() {
        int count = 0;
        for (JunkItem item : items) {
            if (item.isSelected()) {
                count++;
            }
        }
        return count;
    }

    public synchronized long getCategorySize(JunkCategory category) {
        return categorySizes.getOrDefault(category, 0L);
    }

    public synchronized int getCategoryCount(JunkCategory category) {
        return categoryCounts.getOrDefault(category, 0);
    }

    public synchronized void setCategorySelected(JunkCategory category, boolean selected) {
        for (JunkItem item : items) {
            if (item.getCategory() == category) {
                item.setSelected(selected);
            }
        }
    }

    public synchronized boolean isCategorySelected(JunkCategory category) {
        boolean hasItems = false;
        for (JunkItem item : items) {
            if (item.getCategory() == category) {
                hasItems = true;
                if (!item.isSelected()) return false;
            }
        }
        return hasItems;
    }

    public long getScanDurationMs() {
        return scanDurationMs;
    }

    public void setScanDurationMs(long scanDurationMs) {
        this.scanDurationMs = scanDurationMs;
    }

    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "0.0 MB";
        if (bytes < 1024) return bytes + " B";
        
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return new DecimalFormat("#,##0.0").format(kb) + " KB";
        }
        
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return new DecimalFormat("#,##0.0").format(mb) + " MB";
        }
        
        double gb = mb / 1024.0;
        return new DecimalFormat("#,##0.00").format(gb) + " GB";
    }

    public String getFormattedTotalSize() {
        return formatBytes(totalBytes);
    }

    public String getFormattedSelectedSize() {
        return formatBytes(getSelectedBytes());
    }
}
