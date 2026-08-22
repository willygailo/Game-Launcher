package com.gamebooster.app.cleaner.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CleanResult {

    private final boolean success;
    private final long bytesFreed;
    private final int filesDeletedCount;
    private final long durationMs;
    private final List<String> logs;

    public CleanResult(boolean success, long bytesFreed, int filesDeletedCount, long durationMs, List<String> logs) {
        this.success = success;
        this.bytesFreed = bytesFreed;
        this.filesDeletedCount = filesDeletedCount;
        this.durationMs = durationMs;
        this.logs = logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public long getBytesFreed() {
        return bytesFreed;
    }

    public String getFormattedBytesFreed() {
        return JunkScanResult.formatBytes(bytesFreed);
    }

    public int getFilesDeletedCount() {
        return filesDeletedCount;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public static CleanResult empty() {
        return new CleanResult(true, 0, 0, 0, Collections.singletonList("No junk files selected"));
    }
}
