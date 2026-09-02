package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameLauncherHelper;
import com.gamebooster.app.games.HomeGameScanner;
import com.gamebooster.app.ui.adapters.ClearGamesAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClearGameDialog {

    public interface OnGamesClearedListener {
        void onGamesUpdated();
    }

    private static Dialog activeDialog;

    public static void show(Context context, OnGamesClearedListener listener) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_clear_games, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        TextView btnClose = view.findViewById(R.id.btn_dialog_clear_close);
        TextView tvCount = view.findViewById(R.id.tv_clear_games_count);
        Button btnClearAllCustom = view.findViewById(R.id.btn_clear_all_custom);
        Button btnRestoreAllDefaults = view.findViewById(R.id.btn_restore_all_defaults);
        EditText etSearch = view.findViewById(R.id.et_search_clear_game);
        RecyclerView rvList = view.findViewById(R.id.rv_clear_game_list);
        LinearLayout layoutEmpty = view.findViewById(R.id.layout_clear_empty_state);
        Button btnDone = view.findViewById(R.id.btn_clear_game_done);

        rvList.setLayoutManager(new LinearLayoutManager(context));
        rvList.setHasFixedSize(true);

        List<GameAppInfo> initialList = new ArrayList<>();
        ClearGamesAdapter adapter = new ClearGamesAdapter(context, initialList, (removedGame, remainingCount) -> {
            if (tvCount != null) {
                tvCount.setText(remainingCount + " games currently on Home. Tap Remove to hide.");
            }
            if (remainingCount == 0) {
                if (rvList != null) rvList.setVisibility(View.GONE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                if (rvList != null) rvList.setVisibility(View.VISIBLE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            }
            if (listener != null) listener.onGamesUpdated();
        });
        rvList.setAdapter(adapter);

        Runnable refreshGamesList = () -> {
            AppExecutors.getInstance().executeScan(() -> {
                List<GameAppInfo> scanned = HomeGameScanner.scanTargetGames(context);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (!dialog.isShowing()) return;
                    adapter.updateList(scanned);
                    int count = (scanned != null) ? scanned.size() : 0;
                    if (tvCount != null) {
                        tvCount.setText(count + " games currently on Home. Tap Remove to hide.");
                    }
                    if (count == 0) {
                        if (rvList != null) rvList.setVisibility(View.GONE);
                        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        if (rvList != null) rvList.setVisibility(View.VISIBLE);
                        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                    }
                });
            });
        };

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearAllCustom != null) {
            btnClearAllCustom.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("🗑️ CLEAR ALL CUSTOM APPS")
                        .setMessage("Remove all manually added games and apps from the Home Launcher?")
                        .setPositiveButton("CLEAR ALL", (d, w) -> {
                            int count = GameLauncherHelper.clearAllCustomPackages(context);
                            Toast.makeText(context, "🗑️ Cleared " + count + " custom games from Home", Toast.LENGTH_SHORT).show();
                            refreshGamesList.run();
                            if (listener != null) listener.onGamesUpdated();
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            });
        }

        if (btnRestoreAllDefaults != null) {
            btnRestoreAllDefaults.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("🔄 RESTORE DEFAULT GAMES")
                        .setMessage("Restore all hidden or removed default detected games back to the Home Launcher?")
                        .setPositiveButton("RESTORE ALL", (d, w) -> {
                            GameLauncherHelper.resetAllExcludedPackages(context);
                            Toast.makeText(context, "🔄 Restored all default detected games!", Toast.LENGTH_SHORT).show();
                            refreshGamesList.run();
                            if (listener != null) listener.onGamesUpdated();
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
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

        dialog.setOnDismissListener(d -> {
            if (listener != null) listener.onGamesUpdated();
        });

        // Load initial games
        refreshGamesList.run();

        activeDialog = dialog;
        dialog.show();
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
