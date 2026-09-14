package com.gamebooster.app.shizuku

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

/**
 * ShizukuStatusViewModel — ViewModel providing reactive Shizuku status for UI components.
 */
class ShizukuStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val lifecycleManager = ShizukuLifecycleManager.getInstance(application)

    val status: LiveData<ShizukuStatus> = lifecycleManager.statusLiveData

    init {
        lifecycleManager.init()
    }

    fun refresh() {
        lifecycleManager.refreshStatus()
    }

    fun requestPermission() {
        lifecycleManager.requestPermission()
    }

    fun openShizukuApp() {
        lifecycleManager.openShizukuApp(getApplication())
    }
}
