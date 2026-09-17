package com.gamebooster.app.shizuku;

import java.util.Objects;

/**
 * ShizukuStatus — Comprehensive immutable snapshot of Shizuku binder,
 * permission, connection lifecycle, network mode, and engine state.
 */
public final class ShizukuStatus {

    public enum ConnectionState {
        CONNECTED("Connected"),
        ACTIVE("Active"),
        CONNECTING("Connecting"),
        DEAD("Dead"),
        DISCONNECTED("Disconnected");

        private final String label;

        ConnectionState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum AppStatus {
        RUNNING("Running"),
        BOOSTING("Boosting Active"),
        PAUSED("Paused"),
        STANDBY("Standby");

        private final String label;

        AppStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final boolean installed;
    private final boolean binderAlive;
    private final boolean permissionGranted;
    private final ConnectionState connectionState;
    private final boolean online;
    private final AppStatus appStatus;

    public ShizukuStatus() {
        this(false, false, false, ConnectionState.DISCONNECTED, false, AppStatus.RUNNING);
    }

    public ShizukuStatus(
            boolean installed,
            boolean binderAlive,
            boolean permissionGranted,
            ConnectionState connectionState,
            boolean online,
            AppStatus appStatus) {
        this.installed = installed;
        this.binderAlive = binderAlive;
        this.permissionGranted = permissionGranted;
        this.connectionState = connectionState != null ? connectionState : ConnectionState.DISCONNECTED;
        this.online = online;
        this.appStatus = appStatus != null ? appStatus : AppStatus.RUNNING;
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isBinderAlive() {
        return binderAlive;
    }

    public boolean isPermissionGranted() {
        return permissionGranted;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public boolean isOnline() {
        return online;
    }

    public AppStatus getAppStatus() {
        return appStatus;
    }

    public boolean isServiceRunning() {
        return binderAlive;
    }

    public String getStatusSummary() {
        if (binderAlive && permissionGranted) {
            return "Active";
        } else if (binderAlive) {
            return "Connected (Permission Needed)";
        } else if (installed) {
            return "Installed (Service Stopped)";
        } else {
            return "Not Installed";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShizukuStatus)) return false;
        ShizukuStatus that = (ShizukuStatus) o;
        return installed == that.installed &&
                binderAlive == that.binderAlive &&
                permissionGranted == that.permissionGranted &&
                online == that.online &&
                connectionState == that.connectionState &&
                appStatus == that.appStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(installed, binderAlive, permissionGranted, connectionState, online, appStatus);
    }

    @Override
    public String toString() {
        return "ShizukuStatus{" +
                "installed=" + installed +
                ", binderAlive=" + binderAlive +
                ", permissionGranted=" + permissionGranted +
                ", connectionState=" + connectionState +
                ", online=" + online +
                ", appStatus=" + appStatus +
                '}';
    }
}
