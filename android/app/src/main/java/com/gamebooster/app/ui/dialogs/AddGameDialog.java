package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameLauncherHelper;
import com.gamebooster.app.ui.adapters.AddGameAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AddGameDialog {

    public interface OnGameAddedListener {
        void onGamesUpdated();
    }

    private static Dialog activeDialog;

    public static void show(Context context, OnGameAddedListener listener) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_game, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        TextView btnClose = view.findViewById(R.id.btn_dialog_add_game_close);
        EditText etSearch = view.findViewById(R.id.et_search_add_game);
        RecyclerView rvList = view.findViewById(R.id.rv_add_game_list);
        Button btnDone = view.findViewById(R.id.btn_add_game_done);

        rvList.setLayoutManager(new LinearLayoutManager(context));
        rvList.setHasFixedSize(true);

        List<AddGameAdapter.AppPickerItem> items = new ArrayList<>();
        AddGameAdapter adapter = new AddGameAdapter(context, items, () -> {
            if (listener != null) listener.onGamesUpdated();
        });
        rvList.setAdapter(adapter);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int count, int after) {
                    adapter.filter(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) listener.onGamesUpdated();
            });
        }

        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) listener.onGamesUpdated();
            });
        }

        // Load installed packages asynchronously
        AppExecutors.getInstance().executeScan(() -> {
            List<AddGameAdapter.AppPickerItem> loaded = loadAllApps(context);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!dialog.isShowing()) return;
                adapter.updateList(loaded);
            });
        });

        activeDialog = dialog;
        dialog.show();
    }

    private static List<AddGameAdapter.AppPickerItem> loadAllApps(Context context) {
        List<AddGameAdapter.AppPickerItem> list = new ArrayList<>();
        if (context == null) return list;

        PackageManager pm = context.getPackageManager();
        if (pm == null) return list;

        Set<String> customAdded = GameLauncherHelper.getCustomPackages(context);

        try {
            List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo ai : installed) {
                if (ai == null || ai.packageName == null) continue;
                if (ai.packageName.equals(context.getPackageName())) continue;

                boolean isUserApp = (ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
                boolean isGameCategory = (ai.category == ApplicationInfo.CATEGORY_GAME);
                boolean isCustom = customAdded.contains(ai.packageName);

                if (isUserApp || isGameCategory || isCustom) {
                    CharSequence label = pm.getApplicationLabel(ai);
                    Drawable icon = null;
                    try {
                        icon = pm.getApplicationIcon(ai);
                    } catch (Throwable ignored) {}

                    list.add(new AddGameAdapter.AppPickerItem(
                            label != null ? label.toString() : ai.packageName,
                            ai.packageName,
                            icon,
                            isCustom
                    ));
                }
            }

            Collections.sort(list, (a, b) -> a.appName.compareToIgnoreCase(b.appName));
        } catch (Throwable ignored) {}

        return list;
    }

    public static void dismissCurrent() {
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
            } catch (Throwable ignored) {}
            activeDialog = null;
        }
    }
}
