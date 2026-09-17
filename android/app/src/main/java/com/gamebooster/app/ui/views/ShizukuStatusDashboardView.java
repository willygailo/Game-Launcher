package com.gamebooster.app.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.gamebooster.app.R;
import com.gamebooster.app.shizuku.ShizukuConnectionManager;
import com.gamebooster.app.shizuku.ShizukuLifecycleManager;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuStatus;

/**
 * ShizukuStatusDashboardView — Real-time telemetry dashboard view.
 *
 * Displays exact specifications:
 * SHIZUKU STATUS
 * ━━━━━━━━━━━━━━━━
 * Service:       Connected / Disconnected
 * Permission:    Granted / Denied
 * Connection:    Active / Inactive
 * Network:       Offline / Online
 * App Status:    Running
 */
public class ShizukuStatusDashboardView extends LinearLayout {

    private static final int COLOR_ACTIVE = Color.parseColor("#00FF66");
    private static final int COLOR_CYAN = Color.parseColor("#00FFCC");
    private static final int COLOR_WARN = Color.parseColor("#FFAA00");
    private static final int COLOR_INACTIVE = Color.parseColor("#FF3366");
    private static final int COLOR_OFFLINE = Color.parseColor("#8899A6");

    private final View indicatorView;
    private final TextView tvService;
    private final TextView tvPermission;
    private final TextView tvConnection;
    private final TextView tvNetwork;
    private final TextView tvAppStatus;
    private final TextView btnAction;

    public ShizukuStatusDashboardView(Context context) {
        this(context, null);
    }

    public ShizukuStatusDashboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShizukuStatusDashboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_shizuku_dashboard, this, true);

        indicatorView = findViewById(R.id.view_status_indicator);
        tvService = findViewById(R.id.tv_status_service);
        tvPermission = findViewById(R.id.tv_status_permission);
        tvConnection = findViewById(R.id.tv_status_connection);
        tvNetwork = findViewById(R.id.tv_status_network);
        tvAppStatus = findViewById(R.id.tv_status_app);
        btnAction = findViewById(R.id.btn_action_shizuku);

        btnAction.setOnClickListener(v -> handleActionClick());
    }

    /**
     * Binds this dashboard to live updates from ShizukuLifecycleManager.
     */
    public void bindLifecycle(LifecycleOwner lifecycleOwner) {
        if (lifecycleOwner == null) return;
        ShizukuLifecycleManager manager = ShizukuLifecycleManager.getInstance(getContext());
        manager.getStatusLiveData().observe(lifecycleOwner, this::updateStatus);
    }

    /**
     * Updates view elements with live real-time values.
     */
    public void updateStatus(ShizukuStatus status) {
        if (status == null) return;

        // Service
        if (status.isBinderAlive()) {
            tvService.setText("Connected");
            tvService.setTextColor(COLOR_ACTIVE);
        } else {
            tvService.setText("Disconnected");
            tvService.setTextColor(COLOR_INACTIVE);
        }

        // Permission
        if (status.isPermissionGranted()) {
            tvPermission.setText("Granted");
            tvPermission.setTextColor(COLOR_ACTIVE);
        } else if (status.isBinderAlive()) {
            tvPermission.setText("Not Granted");
            tvPermission.setTextColor(COLOR_WARN);
        } else {
            tvPermission.setText("Denied / Unknown");
            tvPermission.setTextColor(COLOR_INACTIVE);
        }

        // Connection
        String connLabel;
        ShizukuStatus.ConnectionState connState = status.getConnectionState();
        if (connState == ShizukuStatus.ConnectionState.ACTIVE) {
            connLabel = "Active";
        } else if (connState == ShizukuStatus.ConnectionState.CONNECTED) {
            connLabel = "Connected";
        } else if (connState == ShizukuStatus.ConnectionState.CONNECTING) {
            connLabel = "Connecting...";
        } else if (connState == ShizukuStatus.ConnectionState.DEAD) {
            connLabel = "Dead";
        } else {
            connLabel = "Inactive";
        }
        tvConnection.setText(connLabel);
        if (connState == ShizukuStatus.ConnectionState.ACTIVE) {
            tvConnection.setTextColor(COLOR_ACTIVE);
        } else if (connState == ShizukuStatus.ConnectionState.CONNECTING) {
            tvConnection.setTextColor(COLOR_WARN);
        } else {
            tvConnection.setTextColor(COLOR_INACTIVE);
        }

        // Network
        if (status.isOnline()) {
            tvNetwork.setText("Online");
            tvNetwork.setTextColor(COLOR_CYAN);
        } else {
            if (status.isBinderAlive()) {
                tvNetwork.setText("Offline (IPC Active ⚡)");
                tvNetwork.setTextColor(COLOR_ACTIVE);
            } else {
                tvNetwork.setText("Offline");
                tvNetwork.setTextColor(COLOR_OFFLINE);
            }
        }

        // App Status
        tvAppStatus.setText(status.getAppStatus().getLabel());
        tvAppStatus.setTextColor(COLOR_ACTIVE);

        // Action button text and indicator badge
        if (status.isBinderAlive() && status.isPermissionGranted()) {
            btnAction.setText("SYNC TWEAKS");
            btnAction.setTextColor(COLOR_CYAN);
            indicatorView.setBackgroundResource(R.drawable.badge_neon_cyan);
        } else if (status.isBinderAlive()) {
            btnAction.setText("GRANT PERMISSION");
            btnAction.setTextColor(COLOR_WARN);
            indicatorView.setBackgroundResource(R.drawable.badge_neon_cyan);
        } else {
            btnAction.setText("START SHIZUKU");
            btnAction.setTextColor(COLOR_INACTIVE);
            indicatorView.setBackgroundColor(COLOR_INACTIVE);
        }
    }

    private void handleActionClick() {
        ShizukuLifecycleManager manager = ShizukuLifecycleManager.getInstance(getContext());
        ShizukuStatus status = manager.getCurrentStatus();

        // If not reported alive, attempt immediate proactive reconnection first
        if (!status.isBinderAlive()) {
            ShizukuConnectionManager.getInstance().forceReconnectCheck();
            status = manager.refreshStatus();
        }

        if (status.isBinderAlive() && status.isPermissionGranted()) {
            // Already active, trigger sync
            ShizukuManager.handleShizukuCardClick(getContext());
        } else if (status.isBinderAlive()) {
            // Service is up, request permission
            manager.requestPermission();
        } else {
            // Not running, show dialog / open Shizuku
            manager.showRequiredDialog(getContext(), "Game Booster Pro");
        }
    }
}
