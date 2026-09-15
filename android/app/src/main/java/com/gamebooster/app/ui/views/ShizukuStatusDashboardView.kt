package com.gamebooster.app.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.gamebooster.app.R
import com.gamebooster.app.shizuku.ShizukuConnectionManager
import com.gamebooster.app.shizuku.ShizukuLifecycleManager
import com.gamebooster.app.shizuku.ShizukuStatus

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
class ShizukuStatusDashboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val indicatorView: View
    private val tvService: TextView
    private val tvPermission: TextView
    private val tvConnection: TextView
    private val tvNetwork: TextView
    private val tvAppStatus: TextView
    private val btnAction: TextView

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_shizuku_dashboard, this, true)

        indicatorView = findViewById(R.id.view_status_indicator)
        tvService = findViewById(R.id.tv_status_service)
        tvPermission = findViewById(R.id.tv_status_permission)
        tvConnection = findViewById(R.id.tv_status_connection)
        tvNetwork = findViewById(R.id.tv_status_network)
        tvAppStatus = findViewById(R.id.tv_status_app)
        btnAction = findViewById(R.id.btn_action_shizuku)

        btnAction.setOnClickListener {
            handleActionClick()
        }
    }

    /**
     * Binds this dashboard to live updates from ShizukuLifecycleManager.
     */
    fun bindLifecycle(lifecycleOwner: LifecycleOwner) {
        val manager = ShizukuLifecycleManager.getInstance(context)
        manager.statusLiveData.observe(lifecycleOwner) { status ->
            updateStatus(status)
        }
    }

    /**
     * Updates view elements with live real-time values.
     */
    fun updateStatus(status: ShizukuStatus) {
        // Service
        if (status.isBinderAlive) {
            tvService.text = "Connected"
            tvService.setTextColor(COLOR_ACTIVE)
        } else {
            tvService.text = "Disconnected"
            tvService.setTextColor(COLOR_INACTIVE)
        }

        // Permission
        if (status.isPermissionGranted) {
            tvPermission.text = "Granted"
            tvPermission.setTextColor(COLOR_ACTIVE)
        } else if (status.isBinderAlive) {
            tvPermission.text = "Not Granted"
            tvPermission.setTextColor(COLOR_WARN)
        } else {
            tvPermission.text = "Denied / Unknown"
            tvPermission.setTextColor(COLOR_INACTIVE)
        }

        // Connection
        val connLabel = when (status.connectionState) {
            ShizukuStatus.ConnectionState.ACTIVE -> "Active"
            ShizukuStatus.ConnectionState.CONNECTED -> "Connected"
            ShizukuStatus.ConnectionState.CONNECTING -> "Connecting..."
            ShizukuStatus.ConnectionState.DEAD -> "Dead"
            ShizukuStatus.ConnectionState.DISCONNECTED -> "Inactive"
        }
        tvConnection.text = connLabel
        tvConnection.setTextColor(
            if (status.connectionState == ShizukuStatus.ConnectionState.ACTIVE) COLOR_ACTIVE
            else if (status.connectionState == ShizukuStatus.ConnectionState.CONNECTING) COLOR_WARN
            else COLOR_INACTIVE
        )

        // Network
        if (status.isOnline) {
            tvNetwork.text = "Online"
            tvNetwork.setTextColor(COLOR_CYAN)
        } else {
            tvNetwork.text = "Offline"
            tvNetwork.setTextColor(COLOR_OFFLINE)
        }

        // App Status
        tvAppStatus.text = status.appStatus.label
        tvAppStatus.setTextColor(COLOR_ACTIVE)

        // Action button text and indicator badge
        if (status.isBinderAlive && status.isPermissionGranted) {
            btnAction.text = "SYNC TWEAKS"
            btnAction.setTextColor(COLOR_CYAN)
            indicatorView.setBackgroundResource(R.drawable.badge_neon_cyan)
        } else if (status.isBinderAlive) {
            btnAction.text = "GRANT PERMISSION"
            btnAction.setTextColor(COLOR_WARN)
            indicatorView.setBackgroundResource(R.drawable.badge_neon_cyan)
        } else {
            btnAction.text = "START SHIZUKU"
            btnAction.setTextColor(COLOR_INACTIVE)
            indicatorView.setBackgroundColor(COLOR_INACTIVE)
        }
    }

    private fun handleActionClick() {
        val manager = ShizukuLifecycleManager.getInstance(context)
        var status = manager.getCurrentStatus()
        
        // If not reported alive, attempt immediate proactive reconnection first
        if (!status.isBinderAlive) {
            ShizukuConnectionManager.getInstance().forceReconnectCheck()
            status = manager.refreshStatus()
        }

        if (status.isBinderAlive && status.isPermissionGranted) {
            // Already active, trigger sync
            com.gamebooster.app.shizuku.ShizukuManager.handleShizukuCardClick(context)
        } else if (status.isBinderAlive) {
            // Service is up, request permission
            manager.requestPermission()
        } else {
            // Not running, show dialog / open Shizuku
            manager.showRequiredDialog(context, "Game Booster Pro")
        }
    }

    companion object {
        private val COLOR_ACTIVE = Color.parseColor("#00FF66")
        private val COLOR_CYAN = Color.parseColor("#00FFCC")
        private val COLOR_WARN = Color.parseColor("#FFAA00")
        private val COLOR_INACTIVE = Color.parseColor("#FF3366")
        private val COLOR_OFFLINE = Color.parseColor("#8899A6")
    }
}
