package com.gamebooster.app.shizuku;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

/**
 * ShizukuStatusViewModel — ViewModel providing reactive Shizuku status for UI components.
 */
public class ShizukuStatusViewModel extends AndroidViewModel {

    private final ShizukuLifecycleManager lifecycleManager;
    private final LiveData<ShizukuStatus> status;

    public ShizukuStatusViewModel(@NonNull Application application) {
        super(application);
        this.lifecycleManager = ShizukuLifecycleManager.getInstance(application);
        this.status = lifecycleManager.getStatusLiveData();
        this.lifecycleManager.init();
    }

    public LiveData<ShizukuStatus> getStatus() {
        return status;
    }

    public void refresh() {
        lifecycleManager.refreshStatus();
    }

    public void requestPermission() {
        lifecycleManager.requestPermission();
    }

    public void openShizukuApp() {
        lifecycleManager.openShizukuApp(getApplication());
    }
}
