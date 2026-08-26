package com.gamebooster.app.focus;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.config.ManualSettingsPreferences;
import com.gamebooster.app.core.AppExecutors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FocusAppSelectorDialog {

    public interface OnWhitelistUpdatedListener {
        void onWhitelistUpdated(int whitelistCount);
    }

    public static void show(Context context, OnWhitelistUpdatedListener listener) {
        if (context == null) return;

        AppExecutors.getInstance().executeScan(() -> {
            List<FocusAppModel> apps = FocusModeEngine.getFreezableApps(context);
            Set<String> currentWhitelist = new HashSet<>(ManualSettingsPreferences.getFocusWhitelist(context));

            AppExecutors.getInstance().postToMainThread(() -> {
                if (apps.isEmpty()) {
                    Toast.makeText(context, "No 3rd-party background apps found to freeze.", Toast.LENGTH_SHORT).show();
                    return;
                }

                RecyclerView recyclerView = new RecyclerView(context);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setPadding(16, 16, 16, 16);

                FocusAppAdapter adapter = new FocusAppAdapter(apps, currentWhitelist);
                recyclerView.setAdapter(adapter);

                new AlertDialog.Builder(context)
                        .setTitle("🎯 FOCUS MODE APPS (Check to Exclude)")
                        .setMessage("Unchecked apps will be suspended & frozen during gaming to maximize FPS and CPU/RAM allocation. Checked apps are whitelisted and will stay active.")
                        .setView(recyclerView)
                        .setPositiveButton("SAVE WHITELIST", (dialog, which) -> {
                            ManualSettingsPreferences.setFocusWhitelist(context, adapter.getWhitelistedPackages());
                            Toast.makeText(context, "✅ Whitelist Saved: " + adapter.getWhitelistedPackages().size() + " apps excluded from freeze", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onWhitelistUpdated(adapter.getWhitelistedPackages().size());
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            });
        });
    }

    private static class FocusAppAdapter extends RecyclerView.Adapter<FocusAppAdapter.ViewHolder> {
        private final List<FocusAppModel> apps;
        private final Set<String> whitelisted;

        FocusAppAdapter(List<FocusAppModel> apps, Set<String> whitelisted) {
            this.apps = apps;
            this.whitelisted = whitelisted;
        }

        Set<String> getWhitelistedPackages() {
            return whitelisted;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.widget.LinearLayout row = new android.widget.LinearLayout(parent.getContext());
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(12, 12, 12, 12);
            row.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            ImageView ivIcon = new ImageView(parent.getContext());
            ivIcon.setId(View.generateViewId());
            android.widget.LinearLayout.LayoutParams lpIcon = new android.widget.LinearLayout.LayoutParams(80, 80);
            lpIcon.setMarginEnd(16);
            ivIcon.setLayoutParams(lpIcon);
            row.addView(ivIcon);

            android.widget.LinearLayout textLayout = new android.widget.LinearLayout(parent.getContext());
            textLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams lpText = new android.widget.LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            textLayout.setLayoutParams(lpText);

            TextView tvTitle = new TextView(parent.getContext());
            tvTitle.setId(View.generateViewId());
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(14f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            textLayout.addView(tvTitle);

            TextView tvSubtitle = new TextView(parent.getContext());
            tvSubtitle.setId(View.generateViewId());
            tvSubtitle.setTextColor(0xFF94A3B8);
            tvSubtitle.setTextSize(11f);
            textLayout.addView(tvSubtitle);

            row.addView(textLayout);

            CheckBox cbWhitelist = new CheckBox(parent.getContext());
            cbWhitelist.setId(View.generateViewId());
            row.addView(cbWhitelist);

            return new ViewHolder(row, ivIcon, tvTitle, tvSubtitle, cbWhitelist);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FocusAppModel app = apps.get(position);
            holder.tvTitle.setText(app.appLabel);
            holder.tvSubtitle.setText(app.packageName);
            if (app.appIcon != null) {
                holder.ivIcon.setImageDrawable(app.appIcon);
            }
            holder.cbWhitelist.setOnCheckedChangeListener(null);
            holder.cbWhitelist.setChecked(whitelisted.contains(app.packageName));
            holder.cbWhitelist.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    whitelisted.add(app.packageName);
                } else {
                    whitelisted.remove(app.packageName);
                }
            });

            holder.itemView.setOnClickListener(v -> {
                boolean newState = !holder.cbWhitelist.isChecked();
                holder.cbWhitelist.setChecked(newState);
            });
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivIcon;
            final TextView tvTitle;
            final TextView tvSubtitle;
            final CheckBox cbWhitelist;

            ViewHolder(View itemView, ImageView ivIcon, TextView tvTitle, TextView tvSubtitle, CheckBox cbWhitelist) {
                super(itemView);
                this.ivIcon = ivIcon;
                this.tvTitle = tvTitle;
                this.tvSubtitle = tvSubtitle;
                this.cbWhitelist = cbWhitelist;
            }
        }
    }
}
