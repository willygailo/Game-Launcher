package com.gamebooster.app.spoofer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.spoofer.SpoofPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PerAppSpoofDialog — User interface for configuring per-application spoof device profiles.
 */
public class PerAppSpoofDialog {

    private static Dialog activeDialog;

    public static void show(Context context) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_per_app_spoof, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.6f);
        }

        TextView tvAppCount = view.findViewById(R.id.tv_per_app_count);
        SwitchCompat switchAllApps = view.findViewById(R.id.switch_spoof_all_apps);
        RecyclerView rvGames = view.findViewById(R.id.rv_per_app_games);
        Button btnInspector = view.findViewById(R.id.btn_per_app_inspector);
        Button btnClose = view.findViewById(R.id.btn_per_app_close);

        rvGames.setLayoutManager(new LinearLayoutManager(context));

        switchAllApps.setChecked(SpoofPreferences.isSpoofAllApps(context));
        switchAllApps.setOnCheckedChangeListener((btn, isChecked) -> {
            SpoofPreferences.setSpoofAllApps(context, isChecked);
            Toast.makeText(context, isChecked ? "Spoof All Apps: ENABLED" : "Spoof All Apps: DISABLED (Games Only)", Toast.LENGTH_SHORT).show();
        });

        btnInspector.setOnClickListener(v -> SpoofInspectorDialog.show(context));
        btnClose.setOnClickListener(v -> dismissCurrent());

        // Load installed games and apps asynchronously
        AppExecutors.getInstance().executeCommand(() -> {
            List<PerAppSpoofAdapter.AppItem> appList = loadInstalledApps(context);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!dialog.isShowing()) return;

                tvAppCount.setText(appList.size() + " APPS");
                PerAppSpoofAdapter adapter = new PerAppSpoofAdapter(context, appList, (pkg, profileId) -> {
                    Toast.makeText(context, "Saved profile for " + pkg, Toast.LENGTH_SHORT).show();
                });
                rvGames.setAdapter(adapter);
            });
        });

        activeDialog = dialog;
        dialog.show();
    }

    private static List<PerAppSpoofAdapter.AppItem> loadInstalledApps(Context context) {
        List<PerAppSpoofAdapter.AppItem> list = new ArrayList<>();
        if (context == null) return list;

        PackageManager pm = context.getPackageManager();
        if (pm == null) return list;

        try {
            List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo ai : installed) {
                if (ai == null || ai.packageName == null) continue;
                if (ai.packageName.equals(context.getPackageName())) continue;

                // Check if known game or non-system app
                boolean isKnownGame = GamePackageRegistry.isKnownGame(ai.packageName);
                boolean isUserApp = (ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
                boolean isGameCategory = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    isGameCategory = (ai.category == ApplicationInfo.CATEGORY_GAME);
                }

                if (isKnownGame || isUserApp || isGameCategory) {
                    CharSequence label = pm.getApplicationLabel(ai);
                    Drawable icon = null;
                    try {
                        icon = pm.getApplicationIcon(ai);
                    } catch (Throwable ignored) {}

                    list.add(new PerAppSpoofAdapter.AppItem(
                            ai.packageName,
                            label != null ? label.toString() : ai.packageName,
                            icon
                    ));
                }
            }

            Collections.sort(list, (a, b) -> a.appName.compareToIgnoreCase(b.appName));
        } catch (Throwable ignored) {}

        return list;
    }

    public static void dismissCurrent() {
        if (activeDialog != null) {
            try {
                if (activeDialog.isShowing()) {
                    activeDialog.dismiss();
                }
            } catch (Exception ignored) {}
            activeDialog = null;
        }
    }
}
