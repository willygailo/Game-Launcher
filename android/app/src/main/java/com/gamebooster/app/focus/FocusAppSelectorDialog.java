package com.gamebooster.app.focus;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.config.ManualSettingsPreferences;
import com.gamebooster.app.core.AppExecutors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FocusAppSelectorDialog — Interactive Cyberpunk Focus Freeze & Whitelist Selector.
 *
 * Allows users to choose which applications to freeze (or exempt), search apps,
 * select batch categories (Social, Media, All, None), and execute instant background
 * freezing or unfreezing.
 */
public class FocusAppSelectorDialog {

    public interface OnFocusFreezeFinishedListener {
        void onFocusFreezeFinished(int frozenCount, boolean isFrozen);
    }

    private static final Set<String> KNOWN_SOCIAL_PACKAGES = new HashSet<>(Arrays.asList(
            "com.facebook.katana", "com.facebook.orca", "com.facebook.lite",
            "com.instagram.android", "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",
            "com.lemon.lvoverseas", "org.telegram.messenger", "org.thunderdog.challegram",
            "com.whatsapp", "com.whatsapp.w4b", "com.discord", "com.reddit.frontpage",
            "com.twitter.android", "com.android.chrome", "com.brave.browser", "com.opera.browser",
            "com.sec.android.app.sbrowser", "com.microsoft.emmx", "org.mozilla.firefox",
            "com.google.android.youtube", "com.spotify.music", "com.netflix.mediaclient",
            "com.snapchat.android", "com.pinterest", "com.shopee.ph", "com.lazada.android"
    ));

    private static Dialog activeDialog;

    public static void show(Context context, OnFocusFreezeFinishedListener listener) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_focus_freeze, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        TextView tvStatusBadge = view.findViewById(R.id.tv_focus_status_badge);
        TextView tvHeaderDesc = view.findViewById(R.id.tv_focus_header_desc);
        ProgressBar pbProgress = view.findViewById(R.id.pb_focus_freeze_progress);
        TextView tvProgressText = view.findViewById(R.id.tv_focus_freeze_progress_text);
        EditText etSearch = view.findViewById(R.id.et_search_focus_apps);
        Button btnSelectAll = view.findViewById(R.id.btn_select_all_focus);
        Button btnSelectSocial = view.findViewById(R.id.btn_select_social_focus);
        Button btnDeselectAll = view.findViewById(R.id.btn_deselect_all_focus);
        RecyclerView rvApps = view.findViewById(R.id.rv_focus_apps);
        Button btnUnfreezeAll = view.findViewById(R.id.btn_unfreeze_all_apps);
        Button btnFreezeDone = view.findViewById(R.id.btn_freeze_done);

        rvApps.setLayoutManager(new LinearLayoutManager(context));

        int frozenCount = FocusModeEngine.getFrozenCount(context);
        tvStatusBadge.setText("[" + frozenCount + " FROZEN]");
        tvStatusBadge.setTextColor(frozenCount > 0 ? Color.parseColor("#00FF66") : Color.parseColor("#00F0FF"));

        final List<FocusAppModel> allApps = new ArrayList<>();
        final FocusAppAdapter adapter = new FocusAppAdapter(context, allApps);
        rvApps.setAdapter(adapter);

        // Load apps asynchronously
        AppExecutors.getInstance().executeScan(() -> {
            List<FocusAppModel> loaded = FocusModeEngine.getFreezableApps(context);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!dialog.isShowing()) return;
                allApps.clear();
                allApps.addAll(loaded);
                adapter.filter(etSearch.getText().toString());
                updateFreezeButtonText(btnFreezeDone, adapter.getSelectedToFreezeCount());
            });
        });

        // Search text watcher
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

        // Batch Selection: ALL
        btnSelectAll.setOnClickListener(v -> {
            for (FocusAppModel app : allApps) {
                app.isSelectedToFreeze = true;
            }
            adapter.notifyDataSetChanged();
            updateFreezeButtonText(btnFreezeDone, adapter.getSelectedToFreezeCount());
        });

        // Batch Selection: SOCIAL & BROWSERS
        btnSelectSocial.setOnClickListener(v -> {
            for (FocusAppModel app : allApps) {
                if (KNOWN_SOCIAL_PACKAGES.contains(app.packageName.toLowerCase())) {
                    app.isSelectedToFreeze = true;
                }
            }
            adapter.notifyDataSetChanged();
            updateFreezeButtonText(btnFreezeDone, adapter.getSelectedToFreezeCount());
        });

        // Batch Selection: NONE
        btnDeselectAll.setOnClickListener(v -> {
            for (FocusAppModel app : allApps) {
                app.isSelectedToFreeze = false;
            }
            adapter.notifyDataSetChanged();
            updateFreezeButtonText(btnFreezeDone, adapter.getSelectedToFreezeCount());
        });

        adapter.setOnItemSelectionChangedListener(() -> {
            updateFreezeButtonText(btnFreezeDone, adapter.getSelectedToFreezeCount());
        });

        // ACTION 1: UNFREEZE ALL APPS
        btnUnfreezeAll.setOnClickListener(v -> {
            btnUnfreezeAll.setEnabled(false);
            btnFreezeDone.setEnabled(false);
            pbProgress.setVisibility(View.VISIBLE);
            tvProgressText.setVisibility(View.VISIBLE);
            tvProgressText.setText("Restoring all background apps...");

            FocusModeEngine.unfreezeAllAppsAsync(context, new FocusModeEngine.OnFreezeOperationListener() {
                @Override
                public void onProgress(int current, int total, String appName) {
                    if (!dialog.isShowing()) return;
                    int pct = total > 0 ? (int) ((current / (float) total) * 100) : 0;
                    pbProgress.setProgress(pct);
                    tvProgressText.setText("Unsuspending: " + appName);
                }

                @Override
                public void onComplete(int totalProcessed, boolean isFrozen) {
                    if (!dialog.isShowing()) return;
                    btnUnfreezeAll.setEnabled(true);
                    btnFreezeDone.setEnabled(true);
                    pbProgress.setVisibility(View.GONE);
                    tvProgressText.setVisibility(View.GONE);

                    tvStatusBadge.setText("[0 FROZEN]");
                    tvStatusBadge.setTextColor(Color.parseColor("#00F0FF"));

                    for (FocusAppModel app : allApps) {
                        app.isCurrentlyFrozen = false;
                    }
                    adapter.notifyDataSetChanged();

                    Toast.makeText(context.getApplicationContext(), "🔄 Restored " + totalProcessed + " apps to active state!", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onFocusFreezeFinished(0, false);
                    }
                }
            });
        });

        // ACTION 2: DONE & FREEZE NOW ("FREEZE AGAD")
        btnFreezeDone.setOnClickListener(v -> {
            Set<String> packagesToFreeze = new HashSet<>();
            Set<String> whitelistedPackages = new HashSet<>();

            for (FocusAppModel app : allApps) {
                if (app.isSelectedToFreeze) {
                    packagesToFreeze.add(app.packageName);
                } else {
                    whitelistedPackages.add(app.packageName);
                }
            }

            // Save whitelist preference
            ManualSettingsPreferences.setFocusWhitelist(context, whitelistedPackages);

            if (packagesToFreeze.isEmpty()) {
                Toast.makeText(context, "No apps selected to freeze (all whitelisted).", Toast.LENGTH_SHORT).show();
                dismissCurrent();
                if (listener != null) {
                    listener.onFocusFreezeFinished(0, false);
                }
                return;
            }

            btnUnfreezeAll.setEnabled(false);
            btnFreezeDone.setEnabled(false);
            pbProgress.setVisibility(View.VISIBLE);
            tvProgressText.setVisibility(View.VISIBLE);
            tvProgressText.setText("Freezing selected apps...");

            FocusModeEngine.freezeSpecificAppsAsync(context, packagesToFreeze, new FocusModeEngine.OnFreezeOperationListener() {
                @Override
                public void onProgress(int current, int total, String appName) {
                    if (!dialog.isShowing()) return;
                    int pct = total > 0 ? (int) ((current / (float) total) * 100) : 0;
                    pbProgress.setProgress(pct);
                    tvProgressText.setText("Freezing (" + current + "/" + total + "): " + appName);
                }

                @Override
                public void onComplete(int totalProcessed, boolean isFrozen) {
                    if (!dialog.isShowing()) return;

                    btnUnfreezeAll.setEnabled(true);
                    btnFreezeDone.setEnabled(true);
                    pbProgress.setVisibility(View.GONE);
                    tvProgressText.setVisibility(View.GONE);

                    tvStatusBadge.setText("[" + totalProcessed + " FROZEN]");
                    tvStatusBadge.setTextColor(Color.parseColor("#00FF66"));

                    for (FocusAppModel app : allApps) {
                        app.isCurrentlyFrozen = packagesToFreeze.contains(app.packageName);
                    }
                    adapter.notifyDataSetChanged();

                    Toast.makeText(context.getApplicationContext(), "🎯 Focus Mode: " + totalProcessed + " Background Apps Frozen! (100% CPU & RAM Dedicated)", Toast.LENGTH_LONG).show();

                    if (listener != null) {
                        listener.onFocusFreezeFinished(totalProcessed, true);
                    }

                    // Auto dismiss after a brief confirmation delay
                    AppExecutors.getInstance().postDelayed(() -> dismissCurrent(), 700);
                }
            });
        });

        dialog.setCanceledOnTouchOutside(true);
        activeDialog = dialog;
        dialog.show();
    }

    private static void updateFreezeButtonText(Button btnFreezeDone, int selectedCount) {
        if (btnFreezeDone != null) {
            btnFreezeDone.setText("❄️ DONE & FREEZE (" + selectedCount + ")");
        }
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

    private static class FocusAppAdapter extends RecyclerView.Adapter<FocusAppAdapter.ViewHolder> {
        private final Context context;
        private final List<FocusAppModel> masterList;
        private final List<FocusAppModel> filteredList = new ArrayList<>();
        private Runnable onItemSelectionChanged;

        FocusAppAdapter(Context context, List<FocusAppModel> masterList) {
            this.context = context;
            this.masterList = masterList;
            this.filteredList.addAll(masterList);
        }

        void setOnItemSelectionChangedListener(Runnable runnable) {
            this.onItemSelectionChanged = runnable;
        }

        void filter(String query) {
            filteredList.clear();
            if (query == null || query.trim().isEmpty()) {
                filteredList.addAll(masterList);
            } else {
                for (FocusAppModel app : masterList) {
                    if (app.matchesQuery(query)) {
                        filteredList.add(app);
                    }
                }
            }
            notifyDataSetChanged();
        }

        int getSelectedToFreezeCount() {
            int count = 0;
            for (FocusAppModel app : masterList) {
                if (app.isSelectedToFreeze) count++;
            }
            return count;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.widget.LinearLayout row = new android.widget.LinearLayout(parent.getContext());
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(10, 10, 10, 10);
            row.setBackgroundResource(R.drawable.card_glass_shape);
            android.widget.LinearLayout.LayoutParams lpRow = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpRow.setMargins(0, 4, 0, 4);
            row.setLayoutParams(lpRow);

            ImageView ivIcon = new ImageView(parent.getContext());
            android.widget.LinearLayout.LayoutParams lpIcon = new android.widget.LinearLayout.LayoutParams(72, 72);
            lpIcon.setMarginEnd(12);
            ivIcon.setLayoutParams(lpIcon);
            row.addView(ivIcon);

            android.widget.LinearLayout textLayout = new android.widget.LinearLayout(parent.getContext());
            textLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams lpText = new android.widget.LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            textLayout.setLayoutParams(lpText);

            android.widget.LinearLayout titleRow = new android.widget.LinearLayout(parent.getContext());
            titleRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            titleRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvTitle = new TextView(parent.getContext());
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(13f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            titleRow.addView(tvTitle);

            TextView tvStatus = new TextView(parent.getContext());
            tvStatus.setTextSize(9f);
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
            android.widget.LinearLayout.LayoutParams lpStatus = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpStatus.setMarginStart(8);
            tvStatus.setLayoutParams(lpStatus);
            titleRow.addView(tvStatus);

            textLayout.addView(titleRow);

            TextView tvSubtitle = new TextView(parent.getContext());
            tvSubtitle.setTextColor(0xFF94A3B8);
            tvSubtitle.setTextSize(10f);
            textLayout.addView(tvSubtitle);

            row.addView(textLayout);

            CheckBox cbFreeze = new CheckBox(parent.getContext());
            row.addView(cbFreeze);

            return new ViewHolder(row, ivIcon, tvTitle, tvStatus, tvSubtitle, cbFreeze);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FocusAppModel app = filteredList.get(position);
            holder.tvTitle.setText(app.appLabel);
            holder.tvSubtitle.setText(app.packageName);

            if (app.appIcon != null) {
                holder.ivIcon.setImageDrawable(app.appIcon);
            } else {
                holder.ivIcon.setImageResource(R.mipmap.ic_launcher);
            }

            if (app.isCurrentlyFrozen) {
                holder.tvStatus.setText("❄️ FROZEN");
                holder.tvStatus.setTextColor(Color.parseColor("#00FF66"));
            } else {
                holder.tvStatus.setText("⚡ ACTIVE");
                holder.tvStatus.setTextColor(Color.parseColor("#94A3B8"));
            }

            holder.cbFreeze.setOnCheckedChangeListener(null);
            holder.cbFreeze.setChecked(app.isSelectedToFreeze);
            holder.cbFreeze.setOnCheckedChangeListener((buttonView, isChecked) -> {
                app.isSelectedToFreeze = isChecked;
                if (onItemSelectionChanged != null) {
                    onItemSelectionChanged.run();
                }
            });

            holder.itemView.setOnClickListener(v -> {
                boolean newState = !holder.cbFreeze.isChecked();
                holder.cbFreeze.setChecked(newState);
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivIcon;
            final TextView tvTitle;
            final TextView tvStatus;
            final TextView tvSubtitle;
            final CheckBox cbFreeze;

            ViewHolder(View itemView, ImageView ivIcon, TextView tvTitle, TextView tvStatus, TextView tvSubtitle, CheckBox cbFreeze) {
                super(itemView);
                this.ivIcon = ivIcon;
                this.tvTitle = tvTitle;
                this.tvStatus = tvStatus;
                this.tvSubtitle = tvSubtitle;
                this.cbFreeze = cbFreeze;
            }
        }
    }
}
